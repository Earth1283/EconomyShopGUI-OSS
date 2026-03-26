package io.github.Earth1283.economyShopGUIOSS.transaction

/**
 * All discrete ways a player can interact with the shop economy.
 *
 * - [BUY] / [SELL]           — single-quantity click transactions
 * - [BUY_SCREEN] / [SELL_SCREEN] — quantity-input GUI flows
 * - [BUY_STACKS_SCREEN]      — multi-stack purchase GUI
 * - [SELL_ALL]               — sell every matching item in inventory
 * - [SELL_GUI]               — drag-and-drop sell screen
 */
sealed class TransactionType {
    /** Player is buying one stack-size unit from the shop. */
    data object Buy : TransactionType()

    /** Player is selling one stack-size unit to the shop. */
    data object Sell : TransactionType()

    /** Player entered a custom buy quantity via the quantity screen. */
    data object BuyScreen : TransactionType()

    /** Player entered a custom sell quantity via the quantity screen. */
    data object SellScreen : TransactionType()

    /** Player is buying multiple stacks at once. */
    data object BuyStacksScreen : TransactionType()

    /** Player is selling all matching items from their inventory. */
    data object SellAll : TransactionType()

    /** Player sold items via the drag-and-drop sell GUI. */
    data object SellGui : TransactionType()

    val isBuy: Boolean get() = this is Buy || this is BuyScreen || this is BuyStacksScreen
    val isSell: Boolean get() = !isBuy
}
