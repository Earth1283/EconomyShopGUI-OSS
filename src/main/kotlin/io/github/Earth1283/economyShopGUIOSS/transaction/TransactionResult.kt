package io.github.Earth1283.economyShopGUIOSS.transaction

/**
 * The outcome of a single transaction attempt.
 *
 * Callers should pattern-match on this sealed hierarchy rather than checking
 * boolean flags:
 * ```kotlin
 * when (result) {
 *     is TransactionResult.Success  -> player.sendMessage(Lang.BUY_SUCCESS.resolve(...))
 *     is TransactionResult.Failure.NotEnoughMoney -> player.sendMessage(...)
 *     ...
 * }
 * ```
 */
sealed class TransactionResult {

    /** The transaction completed successfully. */
    data class Success(
        /** Total price paid (buy) or received (sell). */
        val price: Double,
        /** Quantity transferred. */
        val quantity: Int,
    ) : TransactionResult()

    /** Base class for all failure reasons. */
    sealed class Failure : TransactionResult() {

        /** Player does not have the required permission. */
        data object NoPermission : Failure()

        /** Player's balance is insufficient for a buy transaction. */
        data class NotEnoughMoney(val required: Double, val balance: Double) : Failure()

        /** Player's inventory is full; items cannot be given. */
        data object InventoryFull : Failure()

        /** Player does not have enough items to sell. */
        data class NotEnoughItems(val required: Int, val held: Int) : Failure()

        /** The item has no buy price configured (buy-only). */
        data object NoBuyPrice : Failure()

        /** The item has no sell price configured (sell-only). */
        data object NoSellPrice : Failure()

        /** A pre-condition requirement (quest, region, time) was not met. */
        data class RequirementNotMet(val description: String) : Failure()

        /** A [PreTransactionEvent] listener cancelled the transaction. */
        data object Cancelled : Failure()

        /** Economy provider is unavailable. */
        data object EconomyUnavailable : Failure()

        /** Requested quantity is below the item's minimum or above its maximum. */
        data class InvalidQuantity(val requested: Int, val min: Int, val max: Int) : Failure()

        /** A catch-all for unexpected failures. */
        data class Unknown(val reason: String) : Failure()
    }

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure
}
