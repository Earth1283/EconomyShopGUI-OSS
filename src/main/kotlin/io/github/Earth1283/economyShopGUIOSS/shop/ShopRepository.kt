package io.github.Earth1283.economyShopGUIOSS.shop

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import io.github.Earth1283.economyShopGUIOSS.api.events.ShopItemsLoadEvent
import io.github.Earth1283.economyShopGUIOSS.config.ShopConfigLoader
import io.github.Earth1283.economyShopGUIOSS.economy.EconomyType
import io.github.Earth1283.economyShopGUIOSS.model.ShopSection

/**
 * Owns the in-memory shop state: loads sections from disk and exposes
 * thread-safe read access to the live data.
 *
 * [load] replaces the entire section list atomically (via `@Volatile`) so
 * concurrent reads always see a consistent snapshot.  The [ShopItemsLoadEvent]
 * is fired synchronously after each successful load.
 */
class ShopRepository(private val plugin: EconomyShopGUIOSS) {

    /**
     * The most recently loaded sections.
     *
     * Marked `@Volatile` so writes from the main thread are immediately
     * visible to any async reader (e.g. PlaceholderAPI lookups).
     */
    @Volatile
    private var sections: List<ShopSection> = emptyList()

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /**
     * Read all section and shop YAML files from disk, build the in-memory
     * state, and fire [ShopItemsLoadEvent].
     *
     * Must be called from the server main thread.
     */
    fun load() {
        val cfg = plugin.configManager
        val defaultEconomy = EconomyType.fromString(cfg.config.defaultEconomy)

        val loaded = ShopConfigLoader.loadAll(
            sectionsDir    = cfg.sectionsDir,
            shopsDir       = cfg.shopsDir,
            defaultEconomy = defaultEconomy,
            logger         = plugin.logger,
        )

        sections = loaded

        val enabled  = loaded.count { it.enabled }
        val disabled = loaded.size - enabled
        val items    = loaded.sumOf { it.allItems.size }
        plugin.logger.info(
            "Loaded ${loaded.size} section(s) ($enabled enabled, $disabled disabled), $items item(s)."
        )

        // Notify other plugins and allow them to augment/validate the shop data
        plugin.server.pluginManager.callEvent(ShopItemsLoadEvent(loaded))
    }

    // ── Read access ───────────────────────────────────────────────────────────

    /** All sections (enabled and disabled) as an immutable snapshot. */
    fun allSections(): List<ShopSection> = sections

    /** All currently enabled sections. */
    fun enabledSections(): List<ShopSection> = sections.filter { it.enabled }

    /** Find a section by its [id], or null if not loaded. */
    fun getSection(id: String): ShopSection? = sections.firstOrNull { it.id == id }

    /** Find any item across all sections by its composite key `"sectionId.itemId"`. */
    fun findItem(sectionId: String, itemId: String): io.github.Earth1283.economyShopGUIOSS.model.ShopItem? =
        getSection(sectionId)?.findItem(itemId)
}
