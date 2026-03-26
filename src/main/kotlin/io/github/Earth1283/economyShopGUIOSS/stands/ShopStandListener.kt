package io.github.Earth1283.economyShopGUIOSS.stands

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import io.github.Earth1283.economyShopGUIOSS.gui.screens.MainMenuScreen
import io.github.Earth1283.economyShopGUIOSS.gui.screens.ShopScreen
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

/**
 * Listens for right-click interactions on blocks that have a [ShopStand]
 * registered at their location, and opens the appropriate shop screen.
 */
class ShopStandListener(private val plugin: EconomyShopGUIOSS) : Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val block = event.clickedBlock ?: return

        val stand = plugin.shopStandManager.getAt(block.location) ?: return

        event.isCancelled = true
        val player = event.player

        val section = plugin.shopManager.getSection(stand.sectionId) ?: run {
            plugin.logger.warning(
                "Shop stand ${stand.id} references missing section '${stand.sectionId}'")
            return
        }

        if (stand.itemId != null) {
            ShopScreen(plugin, section, 1).open(player)
        } else {
            ShopScreen(plugin, section, 1).open(player)
        }
    }
}
