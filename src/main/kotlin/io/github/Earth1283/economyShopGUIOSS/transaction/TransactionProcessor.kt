package io.github.Earth1283.economyShopGUIOSS.transaction

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import io.github.Earth1283.economyShopGUIOSS.api.events.PostTransactionEvent
import io.github.Earth1283.economyShopGUIOSS.api.events.PreTransactionEvent
import io.github.Earth1283.economyShopGUIOSS.economy.EconomyProvider
import io.github.Earth1283.economyShopGUIOSS.model.ShopItem
import io.github.Earth1283.economyShopGUIOSS.model.ShopSection
import io.github.Earth1283.economyShopGUIOSS.shop.price.PriceModifierEngine
import io.github.Earth1283.economyShopGUIOSS.shop.requirements.RequirementChecker
import io.github.Earth1283.economyShopGUIOSS.util.ItemUtils
import org.bukkit.entity.Player

/**
 * Orchestrates the full buy/sell lifecycle for a single transaction:
 *
 * 1. Permission check
 * 2. Quantity bounds check
 * 3. Requirement evaluation ([RequirementChecker])
 * 4. Effective price computation ([PriceModifierEngine])
 * 5. [PreTransactionEvent] — cancellable, price-mutable
 * 6. Economy operation (withdraw or deposit)
 * 7. Inventory operation (give or take items)
 * 8. [PostTransactionEvent]
 * 9. Transaction logging ([TransactionLogger])
 *
 * All methods must be called from the **server main thread**.
 */
class TransactionProcessor(private val plugin: EconomyShopGUIOSS) {

    private val requirementChecker = RequirementChecker(plugin)
    private val modifierEngine     = PriceModifierEngine(plugin)
    val logger                     = TransactionLogger(plugin)

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Execute a buy transaction.
     *
     * @param player  The buying player.
     * @param item    The shop item being purchased.
     * @param section The section the item belongs to (for section modifiers).
     * @param qty     Number of [ShopItem.stackSize]-unit stacks to buy.
     * @return [TransactionResult] indicating success or the failure reason.
     */
    fun buy(
        player: Player,
        item: ShopItem,
        section: ShopSection,
        qty: Int,
        type: TransactionType = TransactionType.Buy,
    ): TransactionResult {
        // 1 — Permission
        item.permission?.let { perm ->
            if (!player.hasPermission(perm)) return TransactionResult.Failure.NoPermission
        }

        // 2 — Quantity bounds
        val qtyResult = validateBuyQty(item, qty) ?: return TransactionResult.Failure.InvalidQuantity(
            qty, item.minBuyQty, item.maxBuyQty
        )

        // 3 — Requirements
        requirementChecker.check(player, item)?.let { return it }

        // 4 — Price
        val unitPrice = modifierEngine.effectiveBuyPrice(item, section, player)
            ?: return TransactionResult.Failure.NoBuyPrice
        val totalPrice = unitPrice * qty

        // 5 — Pre-event
        val pre = PreTransactionEvent(player, item, type, qty, totalPrice)
        plugin.server.pluginManager.callEvent(pre)
        if (pre.isCancelled) return TransactionResult.Failure.Cancelled
        val finalPrice = pre.finalPrice

        // 6 — Economy check + withdraw
        val economy = resolveEconomy(item) ?: return TransactionResult.Failure.EconomyUnavailable
        val balance = economy.getBalance(player)
        if (balance < finalPrice) return TransactionResult.Failure.NotEnoughMoney(finalPrice, balance)
        economy.withdraw(player, finalPrice)

        // 7 — Give items
        val totalItems = item.stackSize * qtyResult
        val overflow = ItemUtils.giveItems(player, item.itemStack, totalItems)
        if (overflow > 0) {
            // Refund proportionally if inventory filled partway
            val given = totalItems - overflow
            val refundQty = overflow.toDouble() / item.stackSize
            val refund = unitPrice * refundQty
            economy.deposit(player, refund)
            if (given == 0) return TransactionResult.Failure.InventoryFull
            // Partial success: fall through with given count
            val result = TransactionResult.Success(finalPrice - refund, given)
            firePost(player, item, type, given, result)
            logger.log(player, item, type, given, finalPrice - refund)
            return result
        }

        // 8 — Post-event + log
        val result = TransactionResult.Success(finalPrice, qty)
        firePost(player, item, type, qty, result)
        logger.log(player, item, type, qty, finalPrice)
        return result
    }

    /**
     * Execute a sell transaction.
     *
     * @param player  The selling player.
     * @param item    The shop item being sold.
     * @param section The section the item belongs to (for section modifiers).
     * @param qty     Number of items to sell (total, not stacks).
     */
    fun sell(
        player: Player,
        item: ShopItem,
        section: ShopSection,
        qty: Int,
        type: TransactionType = TransactionType.Sell,
    ): TransactionResult {
        // 1 — Permission
        item.permission?.let { perm ->
            if (!player.hasPermission(perm)) return TransactionResult.Failure.NoPermission
        }

        // 2 — Quantity bounds
        validateSellQty(item, qty) ?: return TransactionResult.Failure.InvalidQuantity(
            qty, item.minSellQty, item.maxSellQty
        )

        // 3 — Requirements
        requirementChecker.check(player, item)?.let { return it }

        // 4 — Price
        val unitPrice = modifierEngine.effectiveSellPrice(item, section, player)
            ?: return TransactionResult.Failure.NoSellPrice
        val totalPrice = unitPrice * qty

        // 5 — Check player has items
        val held = ItemUtils.countItems(player, item.itemStack, item.matchMeta)
        if (held < qty) return TransactionResult.Failure.NotEnoughItems(qty, held)

        // 6 — Pre-event
        val pre = PreTransactionEvent(player, item, type, qty, totalPrice)
        plugin.server.pluginManager.callEvent(pre)
        if (pre.isCancelled) return TransactionResult.Failure.Cancelled
        val finalPrice = pre.finalPrice

        // 7 — Remove items + deposit
        ItemUtils.removeItems(player, item.itemStack, qty, item.matchMeta)
        val economy = resolveEconomy(item) ?: return TransactionResult.Failure.EconomyUnavailable
        economy.deposit(player, finalPrice)

        // 8 — Post-event + log
        val result = TransactionResult.Success(finalPrice, qty)
        firePost(player, item, type, qty, result)
        logger.log(player, item, type, qty, finalPrice)
        return result
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun validateBuyQty(item: ShopItem, qty: Int): Int? {
        if (qty < item.minBuyQty) return null
        if (item.maxBuyQty > 0 && qty > item.maxBuyQty) return null
        return qty
    }

    private fun validateSellQty(item: ShopItem, qty: Int): Int? {
        if (qty < item.minSellQty) return null
        if (item.maxSellQty > 0 && qty > item.maxSellQty) return null
        return qty
    }

    private fun resolveEconomy(item: ShopItem): EconomyProvider? =
        plugin.economyRegistry.resolve(item.economyType).takeIf { it.isAvailable }

    private fun firePost(
        player: Player,
        item: ShopItem,
        type: TransactionType,
        qty: Int,
        result: TransactionResult,
    ) {
        plugin.server.pluginManager.callEvent(PostTransactionEvent(player, item, type, qty, result))
    }
}
