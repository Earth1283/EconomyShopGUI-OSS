package io.github.Earth1283.economyShopGUIOSS.model

import net.kyori.adventure.text.Component
import org.bukkit.Material

/** Determines how a section's navigation bar is rendered. */
enum class NavBarMode {
    /** Use the server-wide default nav bar (configured in `config.yml`). */
    INHERIT,
    /** Use a section-specific nav bar defined in the section's YAML. */
    SELF,
    /** No navigation bar; the last inventory row is treated as item space. */
    DISABLED,
    ;

    companion object {
        fun fromString(value: String): NavBarMode = when (value.uppercase().trim()) {
            "SELF"     -> SELF
            "DISABLED" -> DISABLED
            else       -> INHERIT
        }
    }
}

/** Identifies the role of a nav-bar button. */
enum class NavBarAction {
    PREVIOUS_PAGE,
    NEXT_PAGE,
    CURRENT_PAGE,
    BACK,
    MAIN_MENU,
    CLOSE,
    /** Slot is occupied by a static decoration item (no action). */
    DECORATION,
}

/**
 * A single button in the navigation bar.
 *
 * @param slot         Inventory slot index (0–53).
 * @param action       What this button does when clicked.
 * @param material     The icon material.
 * @param displayName  MiniMessage-parsed display name.
 * @param lore         MiniMessage-parsed lore lines.
 * @param customModelData  Custom model data for resource-pack icons (-1 = none).
 */
data class NavBarItem(
    val slot: Int,
    val action: NavBarAction,
    val material: Material,
    val displayName: Component,
    val lore: List<Component> = emptyList(),
    val customModelData: Int = -1,
)

/**
 * Full navigation-bar configuration for a shop section.
 *
 * @param mode   Controls whether INHERIT / SELF / DISABLED rendering is used.
 * @param items  The [NavBarItem] buttons keyed by their [NavBarAction].
 *               A missing action means no button is rendered for that role.
 */
data class NavBarConfig(
    val mode: NavBarMode,
    val items: Map<NavBarAction, NavBarItem>,
) {
    companion object {

        /** Slot assignments for a 6-row (54-slot) inventory. */
        private val DEFAULT_SLOTS = mapOf(
            NavBarAction.PREVIOUS_PAGE to 45,
            NavBarAction.MAIN_MENU     to 48,
            NavBarAction.BACK          to 49,
            NavBarAction.CURRENT_PAGE  to 49,
            NavBarAction.NEXT_PAGE     to 53,
            NavBarAction.CLOSE         to 49,
        )

        /**
         * The global default nav bar used when a section has [NavBarMode.INHERIT].
         * Items are plain; the display name and lore come from [Lang] at render time.
         */
        val DEFAULT = NavBarConfig(
            mode = NavBarMode.INHERIT,
            items = emptyMap(), // rendered from Lang keys at runtime
        )

        /** A disabled nav bar (no buttons rendered at all). */
        val DISABLED = NavBarConfig(NavBarMode.DISABLED, emptyMap())
    }
}
