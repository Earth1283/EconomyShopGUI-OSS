package io.github.Earth1283.economyShopGUIOSS.api.events

import io.github.Earth1283.economyShopGUIOSS.model.ShopItem
import io.github.Earth1283.economyShopGUIOSS.transaction.TransactionType
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Fired immediately before a buy or sell transaction is processed.
 *
 * The event is **cancellable** — if cancelled, the transaction is aborted and
 * the player receives no message from the plugin (the listener is responsible
 * for any denial feedback).
 *
 * The [finalPrice] can be mutated by listeners to adjust the price (e.g. for
 * a loyalty discount or server-wide sale event).  A value of `0.0` means free.
 * Setting a negative price is clamped to `0.0` by the transaction processor.
 */
class PreTransactionEvent(
    val player: Player,
    val item: ShopItem,
    val type: TransactionType,
    val quantity: Int,
    finalPrice: Double,
) : Event(), Cancellable {

    /** The price the player will pay/receive. Listeners may modify this. */
    var finalPrice: Double = finalPrice
        set(value) { field = value.coerceAtLeast(0.0) }

    private var cancelled = false

    companion object {
        @JvmStatic private val HANDLERS = HandlerList()
        @JvmStatic fun getHandlerList(): HandlerList = HANDLERS
    }

    override fun getHandlers(): HandlerList = HANDLERS
    override fun isCancelled(): Boolean = cancelled
    override fun setCancelled(cancel: Boolean) { cancelled = cancel }
}
