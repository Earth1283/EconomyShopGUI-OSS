package io.github.Earth1283.economyShopGUIOSS.gui.screens

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import io.github.Earth1283.economyShopGUIOSS.gui.GuiScreen
import io.github.Earth1283.economyShopGUIOSS.gui.components.ItemBuilder
import io.github.Earth1283.economyShopGUIOSS.gui.components.NavBar
import io.github.Earth1283.economyShopGUIOSS.lang.Lang
import io.github.Earth1283.economyShopGUIOSS.model.*
import io.github.Earth1283.economyShopGUIOSS.shop.price.PriceModifierEngine
import io.github.Earth1283.economyShopGUIOSS.transaction.TransactionProcessor
import io.github.Earth1283.economyShopGUIOSS.transaction.TransactionResult
import io.github.Earth1283.economyShopGUIOSS.transaction.TransactionType
import io.github.Earth1283.economyShopGUIOSS.util.ColorUtils
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

/**
 * A paginated shop section screen showing all [ShopItem]s on a given [page].
 *
 * Items are placed in the inventory according to [ShopItem.slot].  The nav bar
 * is rendered over a separate row and handled by [NavBar].  Clicking an item
 * dispatches the action configured by [ShopSection.clickMapping].
 */
class ShopScreen(
    private val plugin: EconomyShopGUIOSS,
    private val section: ShopSection,
    private val page: Int,
) : GuiScreen {

    private val navBar     = NavBar(plugin)
    private val modEngine  = PriceModifierEngine(plugin)
    private lateinit var inventory: Inventory

    private val shopPage: ShopPage? = section.getPage(page)
    private val totalPages: Int     = section.pages.size.coerceAtLeast(1)

    override fun build(): Inventory {
        val rows   = shopPage?.rows ?: 6
        val title  = shopPage?.title?.takeUnless { it == Component.empty() }
            ?: section.displayName
        inventory  = Bukkit.createInventory(null, rows * 9, title)

        // Fill background
        section.fillItem?.let { fill ->
            val bg = ItemBuilder.from(fill).name(Component.empty()).hideFlags().build()
            for (i in 0 until rows * 9) inventory.setItem(i, bg)
        }

        // Place items
        shopPage?.items?.values?.forEach { item -> placeItem(item) }

        // Nav bar
        navBar.render(inventory, section.navBarConfig, page, totalPages, hasParent = true)

        return inventory
    }

    private fun placeItem(item: ShopItem) {
        val baseMeta   = item.itemStack.clone()
        val loreLines  = buildLore(item)
        val displayName = item.displayName.takeUnless { it == Component.empty() }
            ?: baseMeta.itemMeta?.displayName()

        val stack = ItemBuilder.from(baseMeta)
            .name(displayName)
            .lore(if (item.hidePricingLore) item.lore else item.lore + loreLines)
            .build()

        inventory.setItem(item.slot, stack)
    }

    private fun buildLore(item: ShopItem): List<Component> {
        if (item.hidePricingLore) return emptyList()
        val lang   = plugin.langRegistry
        val fmt    = plugin.priceFormatter
        val lines  = mutableListOf<Component>()

        item.buyPrice?.let { base ->
            val eff = item.effectiveBuyPrice(null) ?: base
            lines += Lang.LORE_BUY_PRICE.resolve(lang,
                Placeholder.parsed("price", fmt.format(eff)))
        }
        item.sellPrice?.let { base ->
            val eff = item.effectiveSellPrice(null) ?: base
            lines += Lang.LORE_SELL_PRICE.resolve(lang,
                Placeholder.parsed("price", fmt.format(eff)))
        }
        return lines
    }

    override fun handleClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val slot   = event.rawSlot

        // Check nav bar first
        val action = navBar.actionAt(slot, section.navBarConfig, page, totalPages)
        if (action != null) {
            handleNavAction(player, action)
            return
        }

        // Find the shop item at this slot
        val item = shopPage?.items?.get(slot) ?: return

        // Resolve click action
        val clickAction = section.clickMapping.resolve(event.click)
        handleItemAction(player, item, clickAction)
    }

    private fun handleNavAction(player: Player, action: NavBarAction) {
        when (action) {
            NavBarAction.PREVIOUS_PAGE -> ShopScreen(plugin, section, page - 1).open(player)
            NavBarAction.NEXT_PAGE     -> ShopScreen(plugin, section, page + 1).open(player)
            NavBarAction.MAIN_MENU     -> MainMenuScreen(plugin).open(player)
            NavBarAction.BACK          -> MainMenuScreen(plugin).open(player)
            NavBarAction.CLOSE         -> player.closeInventory()
            NavBarAction.CURRENT_PAGE  -> {}
            NavBarAction.DECORATION    -> {}
        }
    }

    private fun handleItemAction(player: Player, item: ShopItem, action: ClickAction) {
        // If this item links to another section, open that section
        item.linkedSectionId?.let { id ->
            val linked = plugin.shopManager.getSection(id)
            if (linked != null) {
                ShopScreen(plugin, linked, 1).open(player)
                return
            }
        }

        when (action) {
            ClickAction.BUY         -> executeBuy(player, item, 1, TransactionType.Buy)
            ClickAction.SELL        -> executeSell(player, item, item.stackSize, TransactionType.Sell)
            ClickAction.SELL_ALL    -> executeSellAll(player, item)
            ClickAction.BUY_SCREEN  -> BuyScreen(plugin, section, item, this).open(player)
            ClickAction.SELL_SCREEN -> SellScreen(plugin, section, item, this).open(player)
            ClickAction.BUY_STACKS_SCREEN -> BuyScreen(plugin, section, item, this).open(player)
            ClickAction.NONE        -> {}
        }
    }

    private fun executeBuy(player: Player, item: ShopItem, qty: Int, type: TransactionType) {
        val result = plugin.transactionProcessor.buy(player, item, section, qty, type)
        sendTransactionFeedback(player, item, qty, result, isBuy = true)
        if (result.isSuccess && item.closeOnPurchase) {
            player.closeInventory()
        }
    }

    private fun executeSell(player: Player, item: ShopItem, qty: Int, type: TransactionType) {
        val result = plugin.transactionProcessor.sell(player, item, section, qty, type)
        sendTransactionFeedback(player, item, qty, result, isBuy = false)
    }

    private fun executeSellAll(player: Player, item: ShopItem) {
        val held = io.github.Earth1283.economyShopGUIOSS.util.ItemUtils.countItems(
            player, item.itemStack, item.matchMeta)
        if (held <= 0) {
            player.sendMessage(Lang.NO_ITEMS_TO_SELL.resolve(plugin.langRegistry))
            return
        }
        executeSell(player, item, held, TransactionType.SellAll)
    }

    private fun sendTransactionFeedback(
        player: Player,
        item: ShopItem,
        qty: Int,
        result: TransactionResult,
        isBuy: Boolean,
    ) {
        val lang = plugin.langRegistry
        val fmt  = plugin.priceFormatter
        when (result) {
            is TransactionResult.Success -> {
                val key = if (isBuy) Lang.BUY_SUCCESS else Lang.SELL_SUCCESS
                player.sendMessage(key.resolve(lang,
                    Placeholder.parsed("qty",   qty.toString()),
                    Placeholder.parsed("item",  item.id),
                    Placeholder.parsed("price", fmt.format(result.price)),
                ))
            }
            is TransactionResult.Failure.NotEnoughMoney ->
                player.sendMessage(Lang.NOT_ENOUGH_MONEY.resolve(lang,
                    Placeholder.parsed("amount", fmt.format(result.required)),
                    Placeholder.parsed("balance", fmt.format(result.balance)),
                ))
            is TransactionResult.Failure.InventoryFull ->
                player.sendMessage(Lang.INVENTORY_FULL.resolve(lang))
            is TransactionResult.Failure.NotEnoughItems ->
                player.sendMessage(Lang.NOT_ENOUGH_ITEMS.resolve(lang,
                    Placeholder.parsed("required", result.required.toString()),
                    Placeholder.parsed("held",     result.held.toString()),
                ))
            is TransactionResult.Failure.NoBuyPrice ->
                player.sendMessage(Lang.CANNOT_BUY.resolve(lang))
            is TransactionResult.Failure.NoSellPrice ->
                player.sendMessage(Lang.CANNOT_SELL.resolve(lang))
            is TransactionResult.Failure.NoPermission ->
                player.sendMessage(Lang.NO_PERMISSION.resolve(lang))
            is TransactionResult.Failure.RequirementNotMet ->
                player.sendMessage(Lang.REQUIREMENT_NOT_MET.resolve(lang,
                    Placeholder.parsed("requirement", result.description)))
            is TransactionResult.Failure.EconomyUnavailable ->
                player.sendMessage(Lang.ECONOMY_UNAVAILABLE.resolve(lang))
            is TransactionResult.Failure.InvalidQuantity ->
                player.sendMessage(Lang.INVALID_QUANTITY.resolve(lang,
                    Placeholder.parsed("min", result.min.toString()),
                    Placeholder.parsed("max", result.max.toString()),
                ))
            is TransactionResult.Failure.Cancelled -> {} // listener handles feedback
            is TransactionResult.Failure.Unknown ->
                player.sendMessage(Lang.GENERIC_ERROR.resolve(lang))
        }
    }
}
