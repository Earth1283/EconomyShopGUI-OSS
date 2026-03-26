package io.github.Earth1283.economyShopGUIOSS.model

import io.github.Earth1283.economyShopGUIOSS.economy.EconomyType
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

/**
 * An immutable snapshot of a shop section loaded from YAML.
 *
 * A section groups related [ShopPage]s under a common economy type, nav bar,
 * and click mapping.  The main menu displays one icon per section at [slot].
 *
 * Changes made through `/eshop` produce a new instance via [copy] and trigger
 * a repository reload — the live section reference is then replaced atomically.
 */
data class ShopSection(

    // ── Identity ──────────────────────────────────────────────────────────────

    /** Unique identifier matching the YAML file name (without `.yml`). */
    val id: String,

    // ── Main menu display ─────────────────────────────────────────────────────

    /** Whether this section is active.  Disabled sections are never shown. */
    val enabled: Boolean,
    /** Icon label shown in the main menu. */
    val displayName: Component,
    /** Slot index in the main menu inventory (-1 = hidden). */
    val slot: Int,
    /** Icon item shown in the main menu. */
    val displayItem: ItemStack?,

    // ── Section GUI ───────────────────────────────────────────────────────────

    /** Economy type used for all items in this section (unless overridden per-item). */
    val economyType: EconomyType,
    /** Item placed in every unfilled slot (decorative filler pane, etc.). */
    val fillItem: ItemStack?,
    /** Controls which slots are valid item positions within a page. */
    val itemLayout: ItemLayout,
    /** Navigation bar settings. */
    val navBarConfig: NavBarConfig,
    /** Maps click types to transaction actions. */
    val clickMapping: ClickMapping,

    // ── Pages & items ─────────────────────────────────────────────────────────

    /** All pages in this section, sorted by [ShopPage.pageNumber]. */
    val pages: List<ShopPage>,

    // ── Visibility ────────────────────────────────────────────────────────────

    /**
     * When true the section is excluded from the main menu but still
     * accessible via `/shop <id>` or as a linked section.
     */
    val hidden: Boolean,

    /**
     * Marks this section as a sub-section (opened from a parent section's
     * link item rather than the main menu).
     */
    val isSubSection: Boolean,

    // ── Price modifiers ───────────────────────────────────────────────────────

    /** Section-wide modifiers applied on top of per-item modifiers. */
    val priceModifiers: List<PriceModifier>,

) {
    // ── Convenience ───────────────────────────────────────────────────────────

    val totalPages: Int get() = pages.size

    /** Return the page at [pageNumber] (1-indexed), or null if out of range. */
    fun getPage(pageNumber: Int): ShopPage? = pages.getOrNull(pageNumber - 1)

    /** All items across all pages as a flat list. */
    val allItems: List<ShopItem> get() = pages.flatMap { it.itemList }

    /** Find an item by its [ShopItem.id] across all pages. */
    fun findItem(itemId: String): ShopItem? =
        pages.firstNotNullOfOrNull { page -> page.itemList.firstOrNull { it.id == itemId } }

    /** True when the section has at least one enabled page with items. */
    val isEmpty: Boolean get() = pages.all { it.items.isEmpty() }
}
