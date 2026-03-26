package io.github.Earth1283.economyShopGUIOSS.api

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import io.github.Earth1283.economyShopGUIOSS.api.model.ApiShopItem
import io.github.Earth1283.economyShopGUIOSS.api.model.ApiShopSection
import io.github.Earth1283.economyShopGUIOSS.model.ShopItem
import io.github.Earth1283.economyShopGUIOSS.model.ShopSection
import io.github.Earth1283.economyShopGUIOSS.transaction.TransactionResult
import io.github.Earth1283.economyShopGUIOSS.transaction.TransactionType
import org.bukkit.entity.Player

/**
 * Public API for EconomyShopGUI-OSS.
 *
 * External plugins should obtain the singleton instance via:
 * ```kotlin
 * val hook = EconomyShopGUIHook.get()
 *     ?: error("EconomyShopGUI-OSS is not loaded")
 * ```
 *
 * The API surface is intentionally narrow — it exposes read-only views of
 * shop data and lets callers trigger transactions without touching internal
 * classes.  All data objects ([ApiShopSection], [ApiShopItem]) are immutable
 * value types; mutation goes through [reloadShop].
 *
 * ## Stability contract
 * Methods in this interface are stable across patch versions.  Any breaking
 * change will bump the minor version and be announced in the changelog.
 */
interface EconomyShopGUIHook {

    // ── Shop data ─────────────────────────────────────────────────────────────

    /** All currently enabled shop sections. */
    fun allSections(): List<ApiShopSection>

    /** Retrieve a section by [id], or null if absent or disabled. */
    fun getSection(id: String): ApiShopSection?

    /**
     * Find a shop item by its composite key.
     *
     * @param sectionId The section ID (file name without `.yml`).
     * @param itemId    The item key inside the shop YAML.
     */
    fun getItem(sectionId: String, itemId: String): ApiShopItem?

    /**
     * Search for items whose ID or display name contains [query]
     * (case-insensitive) across all enabled sections.
     */
    fun searchItems(query: String): List<ApiShopItem>

    // ── Transactions ──────────────────────────────────────────────────────────

    /**
     * Execute a buy transaction on behalf of [player].
     *
     * This goes through the full transaction pipeline:
     * requirements → price modifiers → [PreTransactionEvent] → economy →
     * inventory → [PostTransactionEvent] → logging.
     *
     * @param player    The buying player.
     * @param sectionId Section containing the item.
     * @param itemId    Item to buy.
     * @param quantity  Number of [ApiShopItem.stackSize]-unit stacks.
     * @return [TransactionResult] — inspect to determine success or failure type.
     */
    fun buy(player: Player, sectionId: String, itemId: String, quantity: Int): TransactionResult

    /**
     * Execute a sell transaction on behalf of [player].
     *
     * @param player    The selling player.
     * @param sectionId Section containing the item.
     * @param itemId    Item to sell.
     * @param quantity  Total number of items to sell (not stacks).
     */
    fun sell(player: Player, sectionId: String, itemId: String, quantity: Int): TransactionResult

    // ── Economy ───────────────────────────────────────────────────────────────

    /**
     * Return the player's balance in the default economy.
     *
     * To query a specific economy type, use [getBalance] with the type string:
     * `"vault"`, `"xp"`, `"playerpoints"`, or `"gems"`.
     */
    fun getBalance(player: Player): Double
    fun getBalance(player: Player, economyType: String): Double

    // ── Shop management ───────────────────────────────────────────────────────

    /**
     * Reload all shop configuration from disk.
     *
     * Equivalent to `/sreload`.  Fires [io.github.Earth1283.economyShopGUIOSS.api.events.ShopItemsLoadEvent]
     * after the reload completes.  Must be called from the server main thread.
     */
    fun reloadShop()

    // ── Version info ──────────────────────────────────────────────────────────

    /** The running plugin version string (e.g. `"1.0.0"`). */
    val version: String

    // ── Singleton access ──────────────────────────────────────────────────────

    companion object {
        /**
         * Return the live [EconomyShopGUIHook] instance, or `null` if the
         * plugin is not loaded.
         */
        @JvmStatic
        fun get(): EconomyShopGUIHook? = EconomyShopGUIHookImpl.INSTANCE
    }
}

/**
 * Internal implementation — not part of the public API surface.
 *
 * External plugins must always use [EconomyShopGUIHook], never this class.
 */
internal class EconomyShopGUIHookImpl(private val plugin: EconomyShopGUIOSS) : EconomyShopGUIHook {

    companion object {
        var INSTANCE: EconomyShopGUIHook? = null
            internal set
    }

    override fun allSections(): List<ApiShopSection> =
        plugin.shopManager.allSections().map { it.toApi() }

    override fun getSection(id: String): ApiShopSection? =
        plugin.shopManager.getSection(id)?.toApi()

    override fun getItem(sectionId: String, itemId: String): ApiShopItem? =
        plugin.shopManager.findItem(sectionId, itemId)?.toApi()

    override fun searchItems(query: String): List<ApiShopItem> =
        plugin.shopManager.searchItems(query).map { it.toApi() }

    override fun buy(player: Player, sectionId: String, itemId: String, quantity: Int): TransactionResult {
        val section = plugin.shopManager.getSection(sectionId)
            ?: return TransactionResult.Failure.Unknown("Section '$sectionId' not found")
        val item = section.findItem(itemId)
            ?: return TransactionResult.Failure.Unknown("Item '$itemId' not found in '$sectionId'")
        return plugin.transactionProcessor.buy(player, item, section, quantity, TransactionType.Buy)
    }

    override fun sell(player: Player, sectionId: String, itemId: String, quantity: Int): TransactionResult {
        val section = plugin.shopManager.getSection(sectionId)
            ?: return TransactionResult.Failure.Unknown("Section '$sectionId' not found")
        val item = section.findItem(itemId)
            ?: return TransactionResult.Failure.Unknown("Item '$itemId' not found in '$sectionId'")
        return plugin.transactionProcessor.sell(player, item, section, quantity, TransactionType.Sell)
    }

    override fun getBalance(player: Player): Double =
        plugin.economyRegistry.resolveDefault().getBalance(player)

    override fun getBalance(player: Player, economyType: String): Double =
        plugin.economyRegistry.resolve(
            io.github.Earth1283.economyShopGUIOSS.economy.EconomyType.fromString(economyType)
        ).getBalance(player)

    override fun reloadShop() {
        plugin.configManager.load()
        plugin.langRegistry.load()
        plugin.priceFormatter.load()
        plugin.shopRepository.load()
    }

    override val version: String get() = plugin.pluginMeta.version

    // ── Mappers ───────────────────────────────────────────────────────────────

    private fun ShopSection.toApi() = ApiShopSection(
        id          = id,
        displayName = displayName,
        enabled     = enabled,
        slot        = slot,
        hidden      = hidden,
        items       = allItems.map { it.toApi() },
    )

    private fun ShopItem.toApi() = ApiShopItem(
        id          = id,
        sectionId   = sectionId,
        page        = page,
        slot        = slot,
        displayName = displayName,
        buyPrice    = buyPrice,
        sellPrice   = sellPrice,
        economyType = economyType.toString(),
        stackSize   = stackSize,
        maxBuyQty   = maxBuyQty,
        maxSellQty  = maxSellQty,
        permission  = permission,
    )
}
