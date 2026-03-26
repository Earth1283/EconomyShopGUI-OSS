package io.github.Earth1283.economyShopGUIOSS.api.events

import io.github.Earth1283.economyShopGUIOSS.model.ShopItem
import io.github.Earth1283.economyShopGUIOSS.transaction.TransactionResult
import io.github.Earth1283.economyShopGUIOSS.transaction.TransactionType
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Fired after a transaction completes (successfully or not).
 *
 * The [result] indicates whether the transaction succeeded and, if so, the
 * final price paid or received.  This event is **not cancellable** — use
 * [PreTransactionEvent] to cancel transactions before they happen.
 */
class PostTransactionEvent(
    val player: Player,
    val item: ShopItem,
    val type: TransactionType,
    val quantity: Int,
    val result: TransactionResult,
) : Event() {

    companion object {
        @JvmStatic private val HANDLERS = HandlerList()
        @JvmStatic fun getHandlerList(): HandlerList = HANDLERS
    }

    override fun getHandlers(): HandlerList = HANDLERS
}
