package io.github.Earth1283.economyShopGUIOSS.gui

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent

/**
 * Single global listener that dispatches all inventory events to the
 * appropriate [GuiScreen] via [GuiManager].
 *
 * Registered once in [io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS.onEnable]
 * and never unregistered (Bukkit handles that on plugin disable).
 */
class InventoryListener : Listener {

    @EventHandler(priority = EventPriority.HIGH)
    fun onClick(event: InventoryClickEvent) {
        GuiManager.handleClick(event)
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onDrag(event: InventoryDragEvent) {
        GuiManager.handleDrag(event)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onClose(event: InventoryCloseEvent) {
        GuiManager.handleClose(event)
    }
}
