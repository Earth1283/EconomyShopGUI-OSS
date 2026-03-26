package io.github.Earth1283.economyShopGUIOSS.api.events

import io.github.Earth1283.economyShopGUIOSS.model.ShopSection
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Fired after the plugin finishes loading all shop sections and items from YAML.
 *
 * This event is called both on the initial [onEnable] load and on every
 * `/sreload`.  External plugins can listen to this event to:
 * - Add or modify shop items programmatically.
 * - Trigger dependent reloads (e.g. custom price providers).
 * - Log or validate the loaded shop state.
 *
 * The [sections] list is the live state — modifications are reflected
 * immediately (though editing the list itself has no effect; use the
 * EconomyShopGUI-OSS API to register custom sections).
 */
class ShopItemsLoadEvent(
    /** All sections successfully loaded in this reload. */
    val sections: List<ShopSection>,
) : Event() {

    companion object {
        @JvmStatic
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }

    override fun getHandlers(): HandlerList = HANDLERS
}
