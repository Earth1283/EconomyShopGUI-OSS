package io.github.Earth1283.economyShopGUIOSS.gui

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import java.util.concurrent.ConcurrentHashMap

/**
 * Central registry mapping open [Inventory] instances to their [GuiScreen].
 *
 * The [InventoryListener] performs an O(1) lookup here on every click event.
 * Screens are registered on [open] and removed on inventory close.
 */
object GuiManager {

    private val screens = ConcurrentHashMap<Inventory, GuiScreen>()

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /**
     * Build and open a [GuiScreen] for [player], registering it so that
     * subsequent click events are dispatched correctly.
     */
    fun open(player: Player, screen: GuiScreen) {
        val inventory = screen.build()
        screens[inventory] = screen
        player.openInventory(inventory)
    }

    /** Remove the mapping when an inventory is closed. */
    fun unregister(inventory: Inventory) {
        screens.remove(inventory)
    }

    /** Dispatch a click to the owning screen, or ignore if not a GUI screen. */
    fun handleClick(event: InventoryClickEvent): Boolean {
        val screen = screens[event.inventory] ?: return false
        event.isCancelled = true
        screen.handleClick(event)
        return true
    }

    /** Dispatch a drag to the owning screen. */
    fun handleDrag(event: InventoryDragEvent): Boolean {
        val screen = screens[event.inventory] ?: return false
        event.isCancelled = true
        screen.handleDrag(event)
        return true
    }

    /** Dispatch a close event and clean up. */
    fun handleClose(event: InventoryCloseEvent): Boolean {
        val screen = screens.remove(event.inventory) ?: return false
        screen.handleClose(event)
        return true
    }

    /** True if the player currently has a registered GUI screen open. */
    fun hasOpenScreen(player: Player): Boolean =
        screens.containsKey(player.openInventory.topInventory)

    /** Return the screen the player currently has open, or null. */
    fun currentScreen(player: Player): GuiScreen? =
        screens[player.openInventory.topInventory]
}
