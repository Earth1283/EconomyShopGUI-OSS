package io.github.Earth1283.economyShopGUIOSS.model

import org.bukkit.event.inventory.ClickType

/**
 * What happens when a player clicks a shop item.
 *
 * Each [ClickAction] describes the intent; the GUI layer translates it into
 * a concrete transaction or screen-open operation.
 */
enum class ClickAction {
    /** Immediately buy one [stackSize] of the item. */
    BUY,
    /** Immediately sell one [stackSize] of the item. */
    SELL,
    /** Sell all matching items from the player's inventory. */
    SELL_ALL,
    /** Open the buy-confirmation screen (choose quantity). */
    BUY_SCREEN,
    /** Open the sell-confirmation screen (choose quantity). */
    SELL_SCREEN,
    /** Open the buy-stacks screen (buy multiple stacks at once). */
    BUY_STACKS_SCREEN,
    /** No action — click is silently ignored. */
    NONE;

    companion object {
        fun fromString(value: String): ClickAction = when (value.uppercase().trim()) {
            "BUY"              -> BUY
            "SELL"             -> SELL
            "SELL_ALL", "SELLALL" -> SELL_ALL
            "BUY_SCREEN", "BUYSCREEN" -> BUY_SCREEN
            "SELL_SCREEN", "SELLSCREEN" -> SELL_SCREEN
            "BUY_STACKS", "BUY_STACKS_SCREEN" -> BUY_STACKS_SCREEN
            else               -> NONE
        }
    }
}

/**
 * Maps Bukkit [ClickType]s to [ClickAction]s for a shop section.
 *
 * Defaults follow the original EconomyShopGUI conventions:
 * left = buy, right = sell, middle = sell-all,
 * shift-left = buy-confirmation, shift-right = sell-confirmation.
 */
data class ClickMapping(
    val leftClick: ClickAction       = ClickAction.BUY,
    val rightClick: ClickAction      = ClickAction.SELL,
    val middleClick: ClickAction     = ClickAction.SELL_ALL,
    val shiftLeftClick: ClickAction  = ClickAction.BUY_SCREEN,
    val shiftRightClick: ClickAction = ClickAction.SELL_SCREEN,
    val dropClick: ClickAction       = ClickAction.NONE,
) {
    /** Resolve the [ClickAction] for a given Bukkit [ClickType]. */
    fun resolve(type: ClickType): ClickAction = when (type) {
        ClickType.LEFT             -> leftClick
        ClickType.RIGHT            -> rightClick
        ClickType.MIDDLE           -> middleClick
        ClickType.SHIFT_LEFT       -> shiftLeftClick
        ClickType.SHIFT_RIGHT      -> shiftRightClick
        ClickType.DROP, ClickType.CONTROL_DROP -> dropClick
        else                       -> ClickAction.NONE
    }

    companion object {
        /** Sensible defaults matching the original plugin behaviour. */
        val DEFAULT = ClickMapping()

        fun fromConfig(
            left: String       = "BUY",
            right: String      = "SELL",
            middle: String     = "SELL_ALL",
            shiftLeft: String  = "BUY_SCREEN",
            shiftRight: String = "SELL_SCREEN",
            drop: String       = "NONE",
        ) = ClickMapping(
            leftClick       = ClickAction.fromString(left),
            rightClick      = ClickAction.fromString(right),
            middleClick     = ClickAction.fromString(middle),
            shiftLeftClick  = ClickAction.fromString(shiftLeft),
            shiftRightClick = ClickAction.fromString(shiftRight),
            dropClick       = ClickAction.fromString(drop),
        )
    }
}
