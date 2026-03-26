package io.github.Earth1283.economyShopGUIOSS.stands

import org.bukkit.Location

/**
 * Represents a placed shop stand — a physical block or armor stand in the
 * world that opens a shop section or item screen when right-clicked.
 *
 * Stands are persisted as JSON in `stands.json` so they survive restarts.
 *
 * @param id        Unique identifier (UUID string).
 * @param sectionId The shop section this stand opens.
 * @param itemId    Optional: if set, the stand opens directly to this item.
 * @param world     World name.
 * @param x         Block X coordinate.
 * @param y         Block Y coordinate.
 * @param z         Block Z coordinate.
 */
data class ShopStand(
    val id: String,
    val sectionId: String,
    val itemId: String?,
    val world: String,
    val x: Int,
    val y: Int,
    val z: Int,
) {
    /** Returns the Bukkit [Location] for this stand, or null if the world is not loaded. */
    fun location(): Location? {
        val w = org.bukkit.Bukkit.getWorld(world) ?: return null
        return Location(w, x.toDouble(), y.toDouble(), z.toDouble())
    }
}
