package io.github.Earth1283.economyShopGUIOSS.model

/**
 * Defines which inventory slots are available for shop items within a page.
 *
 * The [slots] list is ordered: the first item in the page occupies [slots][0],
 * the second occupies [slots][1], and so on.  Slots not in the list are
 * treated as filler or nav-bar slots.
 *
 * A layout can be built from:
 * - An explicit list of slot indices.
 * - A pattern string like `"000000000/011111110/011111110"` where `1` = item slot.
 * - The [default] factory that fills all slots except the last (nav-bar) row.
 */
data class ItemLayout(val slots: List<Int>) {

    /** Number of item slots available on each page. */
    val capacity: Int get() = slots.size

    /** True if [slot] is an item slot (not filler / nav-bar). */
    fun isItemSlot(slot: Int): Boolean = slot in slots

    companion object {

        /**
         * All slots in the top [rows]-1 rows; the last row is reserved for the
         * navigation bar.
         */
        fun default(rows: Int): ItemLayout {
            val contentRows = (rows - 1).coerceAtLeast(1)
            return ItemLayout((0 until contentRows * 9).toList())
        }

        /**
         * Parse a `"/"` separated pattern where `1` marks an item slot and
         * any other character marks filler.
         *
         * Example (3-row inventory, border-only):
         * ```
         * "111111111/100000001/111111111"
         * ```
         */
        fun fromPattern(pattern: String): ItemLayout {
            val slots = mutableListOf<Int>()
            pattern.split("/").forEachIndexed { row, line ->
                line.forEachIndexed { col, ch ->
                    if (ch == '1') slots += row * 9 + col
                }
            }
            return ItemLayout(slots)
        }

        /**
         * Build a layout from an explicit list of slot indices provided in config.
         * Duplicate and out-of-range (≥ 54) entries are silently dropped.
         */
        fun fromSlots(slots: List<Int>): ItemLayout =
            ItemLayout(slots.filter { it in 0..53 }.distinct())
    }
}
