package io.github.Earth1283.economyShopGUIOSS.nms

import io.github.Earth1283.economyShopGUIOSS.nms.v1_21.NmsHandlerImpl
import io.github.Earth1283.economyShopGUIOSS.util.VersionUtils

/**
 * Creates the appropriate [NmsHandler] for the running server version.
 *
 * Currently only 1.21.x is supported.  If an unsupported version is detected,
 * a [PaperApiNmsHandler] (pure Paper API, no NMS) is returned so that the
 * plugin remains functional with degraded NMS features rather than failing.
 */
object NmsHandlerFactory {

    fun create(): NmsHandler {
        return when {
            VersionUtils.minecraftVersion.startsWith("1.21") -> NmsHandlerImpl()
            else -> {
                // Future versions — return the pure-Paper fallback and log a warning.
                // Add version-specific handlers here as new MC versions release.
                PaperApiNmsHandler()
            }
        }
    }
}

/**
 * Pure-Paper-API fallback used when no version-specific handler is available.
 *
 * All operations that require NMS are no-ops or use the best available Paper
 * equivalent.
 */
internal class PaperApiNmsHandler : NmsHandler {

    override fun updateInventoryTitle(
        player: org.bukkit.entity.Player,
        inventory: org.bukkit.inventory.Inventory,
        title: net.kyori.adventure.text.Component,
    ) {
        // Paper 1.21: update view title via the public API
        player.openInventory.title()  // read current title
        // In Paper 1.21 there is no public API to change the title in-place
        // without reopening.  Fall back to close + reopen.
        // This is a no-op here; callers should reopen if they need a title change.
    }

    override fun effectiveRows(player: org.bukkit.entity.Player, rows: Int): Int = rows

    override fun sendFakeBlock(
        player: org.bukkit.entity.Player,
        x: Int, y: Int, z: Int,
        materialName: String,
    ) {
        val mat = runCatching {
            org.bukkit.Material.valueOf(materialName.uppercase())
        }.getOrNull() ?: return
        val loc = org.bukkit.Location(player.world, x.toDouble(), y.toDouble(), z.toDouble())
        player.sendBlockChange(loc, mat.createBlockData())
    }
}
