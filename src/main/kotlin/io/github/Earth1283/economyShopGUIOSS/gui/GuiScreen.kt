package io.github.Earth1283.economyShopGUIOSS.gui

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory

/**
 * Contract for every screen in the shop GUI system.
 *
 * Screens are opened by calling [open], which builds the [Inventory] and
 * registers it with [GuiManager].  The global [InventoryListener] dispatches
 * all click, drag, and close events to the screen that owns the inventory.
 *
 * Screens should be **stateless across opens** — create a new instance for
 * each call to [open] rather than caching and re-opening.
 */
interface GuiScreen {

    /** Build and return the inventory that backs this screen. */
    fun build(): Inventory

    /**
     * Called when the player opens this screen.
     *
     * Default: opens [build] result using [GuiManager.open].
     */
    fun open(player: Player) {
        GuiManager.open(player, this)
    }

    /**
     * Handle a slot click inside this screen's inventory.
     *
     * The event is already cancelled before this is called, so implementors
     * only need to act on it — not cancel it themselves.
     */
    fun handleClick(event: InventoryClickEvent)

    /**
     * Handle a drag operation inside this screen's inventory.
     *
     * Default: no-op (drag is blocked by the global listener).
     */
    fun handleDrag(event: InventoryDragEvent) {}

    /**
     * Called when the inventory is closed.
     *
     * Default: no-op.
     */
    fun handleClose(event: InventoryCloseEvent) {}
}
