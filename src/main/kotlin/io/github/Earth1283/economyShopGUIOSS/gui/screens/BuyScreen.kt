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
import org.bukkit.inventory.Inventory

/**
 * Quantity-selection screen for buying items.
 *
 * The screen presents preset quantity buttons (1, 8, 16, 32, 64, stack,
 * half-stack) and a confirm button.  Players increment/decrement the
 * quantity using the buttons, then confirm.
 *
 * Layout (single row = 9 slots, 3 rows total):
 * ```
 * [  ] [ -32 ] [ -8 ] [ -1 ] [item] [ +1 ] [ +8 ] [ +32 ] [  ]
 * [  ] [     ] [    ] [ CONFIRM ] [    ] [ CANCEL ] [     ] [  ]
 * ```
 */
class BuyScreen(
    private val plugin: EconomyShopGUIOSS,
    private val section: ShopSection,
    private val item: ShopItem,
    private val parent: ShopScreen,
) : GuiScreen {

    private var quantity: Int = item.stackSize.coerceAtLeast(1)
    private lateinit var inventory: Inventory

    private val SLOT_ITEM    = 13
    private val SLOT_DEC_32  = 10
    private val SLOT_DEC_8   = 11
    private val SLOT_DEC_1   = 12
    private val SLOT_INC_1   = 14
    private val SLOT_INC_8   = 15
    private val SLOT_INC_32  = 16
    private val SLOT_CONFIRM = 11 + 9
    private val SLOT_CANCEL  = 15 + 9

    override fun build(): Inventory {
        val lang  = plugin.langRegistry
        val fmt   = plugin.priceFormatter
        val title = Lang.BUY_SCREEN_TITLE.resolve(lang,
            Placeholder.parsed("item", item.id))
        inventory = Bukkit.createInventory(null, 27, title)

        // Background fill
        val glass = ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
            .name(ColorUtils.parse(" ")).hideFlags().build()
        for (i in 0 until 27) inventory.setItem(i, glass)

        // Item preview
        val price = item.buyPrice?.let { fmt.format(it) } ?: "N/A"
        val preview = ItemBuilder.from(item.itemStack)
            .name(item.displayName)
            .lore(listOf(
                Lang.BUY_SCREEN_PRICE.resolve(lang, Placeholder.parsed("price", price)),
                Lang.BUY_SCREEN_QTY.resolve(lang,   Placeholder.parsed("qty", quantity.toString())),
            ))
            .build()
        inventory.setItem(SLOT_ITEM, preview)

        // Decrement buttons
        inventory.setItem(SLOT_DEC_32, buildAdjustButton(-32, lang))
        inventory.setItem(SLOT_DEC_8,  buildAdjustButton(-8,  lang))
        inventory.setItem(SLOT_DEC_1,  buildAdjustButton(-1,  lang))

        // Increment buttons
        inventory.setItem(SLOT_INC_1,  buildAdjustButton(+1,  lang))
        inventory.setItem(SLOT_INC_8,  buildAdjustButton(+8,  lang))
        inventory.setItem(SLOT_INC_32, buildAdjustButton(+32, lang))

        // Confirm
        val totalPrice = (item.buyPrice ?: 0.0) * quantity
        val confirm = ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
            .name(Lang.CONFIRM.resolve(lang,
                Placeholder.parsed("price", fmt.format(totalPrice)),
                Placeholder.parsed("qty",   quantity.toString()),
            ))
            .hideFlags().build()
        inventory.setItem(SLOT_CONFIRM, confirm)

        // Cancel
        val cancel = ItemBuilder(Material.RED_STAINED_GLASS_PANE)
            .name(Lang.CANCEL.resolve(lang))
            .hideFlags().build()
        inventory.setItem(SLOT_CANCEL, cancel)

        return inventory
    }

    private fun buildAdjustButton(delta: Int, lang: io.github.Earth1283.economyShopGUIOSS.lang.LangRegistry): org.bukkit.inventory.ItemStack {
        val sign = if (delta > 0) "+" else ""
        val key  = if (delta > 0) Lang.BUY_SCREEN_ADD else Lang.BUY_SCREEN_REMOVE
        return ItemBuilder(if (delta > 0) Material.LIME_DYE else Material.RED_DYE)
            .name(key.resolve(lang, Placeholder.parsed("amount", "$sign${Math.abs(delta)}")))
            .hideFlags().build()
    }

    override fun handleClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        when (event.rawSlot) {
            SLOT_DEC_32  -> adjust(player, -32)
            SLOT_DEC_8   -> adjust(player, -8)
            SLOT_DEC_1   -> adjust(player, -1)
            SLOT_INC_1   -> adjust(player, +1)
            SLOT_INC_8   -> adjust(player, +8)
            SLOT_INC_32  -> adjust(player, +32)
            SLOT_CONFIRM -> confirm(player)
            SLOT_CANCEL  -> parent.open(player)
        }
    }

    private fun adjust(player: Player, delta: Int) {
        val min = item.minBuyQty
        val max = if (item.maxBuyQty > 0) item.maxBuyQty else Int.MAX_VALUE
        quantity = (quantity + delta).coerceIn(min, max)
        open(player)
    }

    private fun confirm(player: Player) {
        player.closeInventory()
        val result = plugin.transactionProcessor.buy(
            player, item, section, quantity, TransactionType.BuyScreen)
        // Feedback is handled by ShopScreen's helper — reopen shop after
        parent.open(player)
    }
}
