package io.github.Earth1283.economyShopGUIOSS.transaction

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

/**
 * SQLite-backed persistence for transaction history.
 *
 * Uses the Exposed ORM (shadowed in the jar) so callers never touch raw SQL.
 * All writes happen on the plugin's async executor; reads can be called from
 * any thread but block the caller until the query completes.
 */
class TransactionDatabase(private val plugin: EconomyShopGUIOSS) {

    // ── Schema ────────────────────────────────────────────────────────────────

    object Transactions : LongIdTable("transactions") {
        val playerUuid  = varchar("player_uuid", 36)
        val playerName  = varchar("player_name", 64)
        val sectionId   = varchar("section_id", 64)
        val itemId      = varchar("item_id", 64)
        val type        = varchar("type", 32)
        val quantity    = integer("quantity")
        val price       = double("price")
        val economy     = varchar("economy", 32)
        val timestamp   = long("timestamp")
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    fun connect() {
        val dbFile = File(plugin.dataFolder, "transactions.db")
        Database.connect("jdbc:sqlite:${dbFile.absolutePath}", driver = "org.sqlite.JDBC")
        transaction {
            SchemaUtils.createMissingTablesAndColumns(Transactions)
        }
    }

    // ── Writes ────────────────────────────────────────────────────────────────

    fun insert(record: TransactionRecord) {
        transaction {
            Transactions.insert {
                it[playerUuid] = record.playerUuid
                it[playerName] = record.playerName
                it[sectionId]  = record.sectionId
                it[itemId]     = record.itemId
                it[type]       = record.type
                it[quantity]   = record.quantity
                it[price]      = record.price
                it[economy]    = record.economy
                it[timestamp]  = record.timestamp
            }
        }
    }

    // ── Reads ─────────────────────────────────────────────────────────────────

    fun queryByPlayer(uuid: String, limit: Int = 50): List<TransactionRecord> = transaction {
        Transactions
            .select { Transactions.playerUuid eq uuid }
            .orderBy(Transactions.timestamp, SortOrder.DESC)
            .limit(limit)
            .map { it.toRecord() }
    }

    fun queryAll(limit: Int = 200): List<TransactionRecord> = transaction {
        Transactions
            .selectAll()
            .orderBy(Transactions.timestamp, SortOrder.DESC)
            .limit(limit)
            .map { it.toRecord() }
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private fun ResultRow.toRecord() = TransactionRecord(
        playerUuid = this[Transactions.playerUuid],
        playerName = this[Transactions.playerName],
        sectionId  = this[Transactions.sectionId],
        itemId     = this[Transactions.itemId],
        type       = this[Transactions.type],
        quantity   = this[Transactions.quantity],
        price      = this[Transactions.price],
        economy    = this[Transactions.economy],
        timestamp  = this[Transactions.timestamp],
    )
}

/** Immutable record of a completed transaction. */
data class TransactionRecord(
    val playerUuid: String,
    val playerName: String,
    val sectionId:  String,
    val itemId:     String,
    val type:       String,
    val quantity:   Int,
    val price:      Double,
    val economy:    String,
    val timestamp:  Long,
)
