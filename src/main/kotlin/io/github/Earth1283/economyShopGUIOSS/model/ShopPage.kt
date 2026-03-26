package io.github.Earth1283.economyShopGUIOSS.model

import net.kyori.adventure.text.Component

/**
 * A single page within a [ShopSection].
 *
 * Pages are defined explicitly in the shop's YAML file under `pages.<n>`.
 * Each page has its own inventory title and row count, and contains a fixed
 * set of [ShopItem]s at predetermined slots.
 *
 * @param pageNumber  1-indexed page number.
 * @param title       Inventory title rendered with MiniMessage.
 * @param rows        Inventory row count (1–6).  The last row is always the
 *                    navigation bar unless the section disables it.
 * @param items       Items on this page, keyed by their slot index.
 */
data class ShopPage(
    val pageNumber: Int,
    val title: Component,
    val rows: Int,
    val items: Map<Int, ShopItem>,      // slot → item
) {
    /** Inventory size in slots. */
    val size: Int get() = rows * 9

    /** Number of item slots (excludes last nav-bar row). */
    val contentSlots: Int get() = (rows - 1) * 9

    /** Return the item at [slot], or null if the slot is empty. */
    fun itemAt(slot: Int): ShopItem? = items[slot]

    /** All items on this page as a list. */
    val itemList: List<ShopItem> get() = items.values.toList()
}
