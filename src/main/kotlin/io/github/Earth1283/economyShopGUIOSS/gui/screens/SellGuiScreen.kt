package io.github.Earth1283.economyShopGUIOSS.gui.screens

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import io.github.Earth1283.economyShopGUIOSS.gui.GuiScreen
import io.github.Earth1283.economyShopGUIOSS.gui.components.ItemBuilder
import io.github.Earth1283.economyShopGUIOSS.lang.Lang
import io.github.Earth1283.economyShopGUIOSS.model.ShopItem
import io.github.Earth1283.economyShopGUIOSS.model.ShopSection
import io.github.Earth1283.economyShopGUIOSS.transaction.TransactionType
import io.github.Earth1283.economyShopGUIOSS.util.ColorUtils
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * Drag-and-drop sell screen opened by `/sellgui`.
 *
 * The top 4 rows act as a drop zone; players drag items from their inventory
 * into it.  On close, every item placed in the drop zone is evaluated for a
 * sell price and sold automatically — items that cannot be sold are returned.
 *
 * Slot 40 (bottom-right of the top inventory) shows a "Sell & Close" confirm
 * button.
 */
class SellGuiScreen(private val plugin: EconomyShopGUIOSS) : GuiScreen {

    companion object {
        private const val ROWS = 5
        private const val SIZE = ROWS * 9
        private const val CONFIRM_SLOT = 44
    }

    private lateinit var inventory: Inventory

    override fun build(): Inventory {
        val lang  = plugin.langRegistry
        val title = Lang.SELL_GUI_TITLE.resolve(lang)
        inventory = Bukkit.createInventory(null, SIZE, title)

        // Fill bottom row with glass separators
        val sep = ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
            .name(ColorUtils.parse(" ")).hideFlags().build()
        for (i in 36 until SIZE) inventory.setItem(i, sep)

        // Confirm button
        val confirm = ItemBuilder(Material.HOPPER)
            .name(Lang.SELL_GUI_CONFIRM.resolve(lang))
            .lore(listOf(Lang.SELL_GUI_CONFIRM_LORE.resolve(lang)))
            .build()
        inventory.setItem(CONFIRM_SLOT, confirm)

        return inventory
    }

    // Allow players to place items into the top 4 rows (slots 0–35)
    override fun handleClick(event: InventoryClickEvent) {
        val slot   = event.rawSlot
        val player = event.whoClicked as? Player ?: return

        if (slot == CONFIRM_SLOT) {
            sellAndClose(player)
            return
        }

        // Block interactions with the separator row (36–44 except confirm)
        if (slot in 36 until SIZE && slot != CONFIRM_SLOT) {
            event.isCancelled = true
            return
        }

        // Allow normal inventory interaction in top rows and player's inventory
        event.isCancelled = false
    }

    override fun handleDrag(event: InventoryDragEvent) {
        // Allow drag into the drop zone (slots 0–35)
        val inDropZone = event.rawSlots.all { it < 36 }
        if (!inDropZone) event.isCancelled = true
    }

    override fun handleClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        sellAndClose(player)
    }

    // ── Sell logic ────────────────────────────────────────────────────────────

    private fun sellAndClose(player: Player) {
        val dropZone = (0 until 36).mapNotNull { slot ->
            val stack = inventory.getItem(slot)
            if (stack == null || stack.type == Material.AIR) null
            else slot to stack
        }

        if (dropZone.isEmpty()) return

        var totalEarned = 0.0
        val unsellable  = mutableListOf<ItemStack>()

        for ((slot, stack) in dropZone) {
            val match = findSellableItem(stack)
            if (match == null) {
                unsellable += stack.clone()
                inventory.setItem(slot, null)
                continue
            }
            val (item, section) = match
            val result = plugin.transactionProcessor.sell(
                player, item, section, stack.amount, TransactionType.SellGui)
            when (result) {
                is io.github.Earth1283.economyShopGUIOSS.transaction.TransactionResult.Success ->
                    totalEarned += result.price
                else -> unsellable += stack.clone()
            }
            inventory.setItem(slot, null)
        }

        // Return unsellable items
        unsellable.forEach { returned ->
            val leftover = player.inventory.addItem(returned)
            leftover.values.forEach { player.world.dropItemNaturally(player.location, it) }
        }

        if (totalEarned > 0) {
            player.sendMessage(Lang.SELL_ALL_SUCCESS.resolve(plugin.langRegistry,
                Placeholder.parsed("price", plugin.priceFormatter.format(totalEarned))))
        }
    }

    private fun findSellableItem(stack: ItemStack): Pair<ShopItem, ShopSection>? {
        for (section in plugin.shopManager.allSections()) {
            for (item in section.allItems) {
                if (item.sellPrice == null) continue
                if (item.itemStack.type == stack.type) return item to section
            }
        }
        return null
    }
}
