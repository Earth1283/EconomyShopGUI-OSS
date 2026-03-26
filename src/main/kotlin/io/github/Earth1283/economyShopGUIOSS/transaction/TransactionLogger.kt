package io.github.Earth1283.economyShopGUIOSS.transaction

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import io.github.Earth1283.economyShopGUIOSS.model.ShopItem
import io.github.Earth1283.economyShopGUIOSS.util.SchedulerUtils
import org.bukkit.entity.Player

/**
 * Writes completed transactions to the [TransactionDatabase] asynchronously.
 *
 * Callers on the main thread fire-and-forget; the write happens on a thread
 * pool managed by [SchedulerUtils] so inventory handling is never blocked.
 */
class TransactionLogger(private val plugin: EconomyShopGUIOSS) {

    private val db = TransactionDatabase(plugin)

    fun init() {
        db.connect()
        plugin.logger.info("Transaction database connected.")
    }

    // ── Write API ─────────────────────────────────────────────────────────────

    fun log(
        player: Player,
        item: ShopItem,
        type: TransactionType,
        quantity: Int,
        price: Double,
    ) {
        val record = TransactionRecord(
            playerUuid = player.uniqueId.toString(),
            playerName = player.name,
            sectionId  = item.sectionId,
            itemId     = item.id,
            type       = type::class.simpleName ?: "Unknown",
            quantity   = quantity,
            price      = price,
            economy    = item.economyType::class.simpleName ?: "Unknown",
            timestamp  = System.currentTimeMillis(),
        )
        SchedulerUtils.runAsync(plugin) { db.insert(record) }
    }

    // ── Read API (for /eshop log) ─────────────────────────────────────────────

    fun queryByPlayer(player: Player, limit: Int = 50): List<TransactionRecord> =
        db.queryByPlayer(player.uniqueId.toString(), limit)

    fun queryAll(limit: Int = 200): List<TransactionRecord> =
        db.queryAll(limit)
}
