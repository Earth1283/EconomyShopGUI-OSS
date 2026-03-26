package io.github.Earth1283.economyShopGUIOSS.gui.components

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import io.github.Earth1283.economyShopGUIOSS.lang.Lang
import io.github.Earth1283.economyShopGUIOSS.model.NavBarAction
import io.github.Earth1283.economyShopGUIOSS.model.NavBarConfig
import io.github.Earth1283.economyShopGUIOSS.model.NavBarItem
import io.github.Earth1283.economyShopGUIOSS.model.NavBarMode
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import org.bukkit.inventory.Inventory

/**
 * Renders navigation bar buttons into a shop inventory.
 *
 * The nav bar can display:
 * - **Previous page** / **Next page** arrows
 * - **Back to main menu** button
 * - **Close** button
 *
 * Slot positions and materials are driven by [NavBarConfig].  When a section
 * uses [NavBarMode.INHERIT], the global nav bar defaults from config are used.
 * When [NavBarMode.DISABLED], nothing is rendered.
 */
class NavBar(private val plugin: EconomyShopGUIOSS) {

    // Default slot assignments (bottom row of a 6-row inventory)
    private val defaultSlots = mapOf(
        NavBarAction.PREVIOUS_PAGE to 45,
        NavBarAction.NEXT_PAGE     to 53,
        NavBarAction.MAIN_MENU     to 49,
        NavBarAction.CLOSE         to 49,
    )

    private val defaultMaterials = mapOf(
        NavBarAction.PREVIOUS_PAGE to Material.ARROW,
        NavBarAction.NEXT_PAGE     to Material.ARROW,
        NavBarAction.MAIN_MENU     to Material.NETHER_STAR,
        NavBarAction.CLOSE         to Material.BARRIER,
    )

    /**
     * Render nav bar buttons into [inventory].
     *
     * @param config     The section's nav bar config.
     * @param currentPage 1-indexed current page number.
     * @param totalPages  Total page count.
     * @param hasParent   True if there is a parent section to return to.
     */
    fun render(
        inventory: Inventory,
        config: NavBarConfig,
        currentPage: Int,
        totalPages: Int,
        hasParent: Boolean,
    ) {
        if (config.mode == NavBarMode.DISABLED) return

        val lang = plugin.langRegistry

        // Previous page
        if (currentPage > 1) {
            val navItem = config.items[NavBarAction.PREVIOUS_PAGE]
            val slot = navItem?.slot ?: defaultSlots[NavBarAction.PREVIOUS_PAGE]!!
            val mat  = navItem?.material ?: defaultMaterials[NavBarAction.PREVIOUS_PAGE]!!
            val name = navItem?.displayName ?: Lang.NAV_PREVIOUS_PAGE.resolve(lang,
                Placeholder.parsed("page", (currentPage - 1).toString()))
            val lore = navItem?.lore ?: emptyList()
            inventory.setItem(slot, ItemBuilder(mat).name(name).lore(lore).hideFlags().build())
        }

        // Next page
        if (currentPage < totalPages) {
            val navItem = config.items[NavBarAction.NEXT_PAGE]
            val slot = navItem?.slot ?: defaultSlots[NavBarAction.NEXT_PAGE]!!
            val mat  = navItem?.material ?: defaultMaterials[NavBarAction.NEXT_PAGE]!!
            val name = navItem?.displayName ?: Lang.NAV_NEXT_PAGE.resolve(lang,
                Placeholder.parsed("page", (currentPage + 1).toString()))
            val lore = navItem?.lore ?: emptyList()
            inventory.setItem(slot, ItemBuilder(mat).name(name).lore(lore).hideFlags().build())
        }

        // Main menu / back button
        val backAction = if (hasParent) NavBarAction.MAIN_MENU else NavBarAction.MAIN_MENU
        val backNavItem = config.items[backAction]
        val backSlot = backNavItem?.slot ?: defaultSlots[NavBarAction.MAIN_MENU]!!
        val backMat  = backNavItem?.material ?: defaultMaterials[NavBarAction.MAIN_MENU]!!
        val backName = backNavItem?.displayName ?: Lang.NAV_MAIN_MENU.resolve(lang)
        val backLore = backNavItem?.lore ?: emptyList()
        inventory.setItem(backSlot, ItemBuilder(backMat).name(backName).lore(backLore).hideFlags().build())
    }

    /**
     * Return the [NavBarAction] for a given [slot], or null if the slot is
     * not a nav bar slot in [config].
     */
    fun actionAt(slot: Int, config: NavBarConfig, currentPage: Int, totalPages: Int): NavBarAction? {
        // Check custom items first
        config.items.entries.firstOrNull { (_, item) -> item.slot == slot }?.key?.let { return it }

        // Fall back to defaults
        return when (slot) {
            defaultSlots[NavBarAction.PREVIOUS_PAGE] -> if (currentPage > 1) NavBarAction.PREVIOUS_PAGE else null
            defaultSlots[NavBarAction.NEXT_PAGE]     -> if (currentPage < totalPages) NavBarAction.NEXT_PAGE else null
            defaultSlots[NavBarAction.MAIN_MENU]     -> NavBarAction.MAIN_MENU
            else -> null
        }
    }
}
