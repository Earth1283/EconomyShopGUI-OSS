package io.github.Earth1283.economyShopGUIOSS.nms.v1_21

import io.github.Earth1283.economyShopGUIOSS.nms.NmsHandler
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

/**
 * NMS handler for Paper 1.21.x.
 *
 * Paper 1.21 exposes enough through its public API that true NMS access is
 * rarely needed.  This implementation uses Paper's own extension points where
 * possible and falls back to `player.sendBlockChange` for fake blocks.
 *
 * If a future Paper version breaks compatibility here, subclass this and
 * register it in [io.github.Earth1283.economyShopGUIOSS.nms.NmsHandlerFactory].
 */
class NmsHandlerImpl : NmsHandler {

    override fun updateInventoryTitle(player: Player, inventory: Inventory, title: Component) {
        // Paper 1.21 does not expose a direct "update open inventory title"
        // method in the public API without reflection.  We use the packet
        // approach via Bukkit's sendBlockChange workaround for now, and close
        // + reopen as the safe default.
        //
        // This is intentionally left as a Paper-API-safe no-op.  A future
        // Paper release may expose `player.openInventory.title(component)`.
    }

    override fun effectiveRows(player: Player, rows: Int): Int {
        // Geyser / Bedrock detection: if the player's client is Bedrock,
        // cap at 4 rows.  We detect Bedrock via the Geyser API if available.
        if (isBedrockPlayer(player)) return rows.coerceAtMost(4)
        return rows
    }

    override fun sendFakeBlock(player: Player, x: Int, y: Int, z: Int, materialName: String) {
        val mat = runCatching { Material.valueOf(materialName.uppercase()) }.getOrNull() ?: return
        val loc = Location(player.world, x.toDouble(), y.toDouble(), z.toDouble())
        player.sendBlockChange(loc, mat.createBlockData())
    }

    // ── Geyser / Bedrock detection ────────────────────────────────────────────

    private fun isBedrockPlayer(player: Player): Boolean {
        return try {
            val floodgateApi = Class.forName("org.geysermc.floodgate.api.FloodgateApi")
            val instance = floodgateApi.getMethod("getInstance").invoke(null)
            floodgateApi.getMethod("isFloodgatePlayer", java.util.UUID::class.java)
                .invoke(instance, player.uniqueId) as? Boolean ?: false
        } catch (_: Exception) {
            false
        }
    }
}
