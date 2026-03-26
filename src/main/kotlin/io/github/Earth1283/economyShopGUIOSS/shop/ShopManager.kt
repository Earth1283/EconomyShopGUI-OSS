package io.github.Earth1283.economyShopGUIOSS.shop

import io.github.Earth1283.economyShopGUIOSS.model.ShopItem
import io.github.Earth1283.economyShopGUIOSS.model.ShopSection

/**
 * High-level façade over [ShopRepository] for use by commands, the GUI, and
 * the public API.
 *
 * All reads are delegated to the repository's volatile snapshot, so callers
 * always see the latest loaded data without any explicit synchronisation.
 */
class ShopManager(private val repository: ShopRepository) {

    // ── Section access ────────────────────────────────────────────────────────

    /** All enabled sections, in the order they were declared in config. */
    fun allSections(): List<ShopSection> = repository.enabledSections()

    /** Return a section by [id], or null. */
    fun getSection(id: String): ShopSection? = repository.getSection(id)

    /**
     * Sections that appear in the main menu (enabled, not hidden, with a valid
     * slot), sorted by their slot index.
     */
    fun mainMenuSections(): List<ShopSection> =
        allSections()
            .filter { !it.hidden && it.slot >= 0 }
            .sortedBy { it.slot }

    // ── Item access ───────────────────────────────────────────────────────────

    /** Find an item by [sectionId] and [itemId]. */
    fun findItem(sectionId: String, itemId: String): ShopItem? =
        repository.findItem(sectionId, itemId)

    /**
     * Search for items whose [ShopItem.id] contains [query] (case-insensitive)
     * across all enabled sections.
     */
    fun searchItems(query: String): List<ShopItem> {
        val lower = query.lowercase()
        return allSections().flatMap { section ->
            section.allItems.filter { item ->
                item.id.lowercase().contains(lower)
            }
        }
    }

    // ── Validation ────────────────────────────────────────────────────────────

    /** True when the given section ID is known (enabled or disabled). */
    fun sectionExists(id: String): Boolean = repository.getSection(id) != null
}
