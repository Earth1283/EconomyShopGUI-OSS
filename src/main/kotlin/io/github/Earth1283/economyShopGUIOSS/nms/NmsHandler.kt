package io.github.Earth1283.economyShopGUIOSS.nms

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

/**
 * Abstraction over version-specific NMS operations.
 *
 * Paper 1.21 exposes most of what we need through the standard API, so the
 * NMS surface is deliberately minimal.  Use this interface — never call NMS
 * directly from business logic.
 *
 * Obtain the live implementation via [NmsHandlerFactory.create].
 */
interface NmsHandler {

    /**
     * Update the title of an already-open inventory without closing and
     * reopening it.
     *
     * Paper 1.21+ exposes `player.openInventory.title(component)` for this,
     * so the default implementation delegates there.
     */
    fun updateInventoryTitle(player: Player, inventory: Inventory, title: net.kyori.adventure.text.Component)

    /**
     * Return the number of rows the player's client will display for a chest
     * inventory.  On Bedrock clients (via Geyser), the maximum is 4 rows;
     * this returns 4 to allow the GUI layer to cap inventory size accordingly.
     *
     * On Java clients always returns [rows] unchanged.
     */
    fun effectiveRows(player: Player, rows: Int): Int

    /**
     * Send a fake block change to [player] at the given coordinates so that a
     * shop stand's "display block" can be changed client-side without modifying
     * the actual world.
     *
     * @param player The target player.
     * @param x, y, z Block coordinates.
     * @param materialName Bukkit Material name of the fake block (e.g. `"CHEST"`).
     */
    fun sendFakeBlock(player: Player, x: Int, y: Int, z: Int, materialName: String)
}
