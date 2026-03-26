package io.github.Earth1283.economyShopGUIOSS.model

/**
 * A gate that must be satisfied before a player may buy or sell an item.
 *
 * Requirements are stored on [ShopItem] and checked by
 * [io.github.Earth1283.economyShopGUIOSS.shop.requirements.RequirementChecker]
 * before each transaction.  All subtypes are sealed so handling is exhaustive.
 */
sealed class Requirement {

    /**
     * The player must have completed a specific quest (and optionally be at
     * or past a given [stage]).
     *
     * Resolved by [io.github.Earth1283.economyShopGUIOSS.hook.QuestsHook];
     * if neither Quests nor QuestsC is installed this requirement silently
     * passes (to avoid breaking servers that removed the quest plugin).
     *
     * @param questId  The quest ID or name as recognised by the quest plugin.
     * @param stage    If not null, the player must be at this stage or later.
     */
    data class Quest(
        val questId: String,
        val stage: Int? = null,
    ) : Requirement()

    /**
     * The player must be standing inside a WorldGuard region.
     *
     * Resolved by [io.github.Earth1283.economyShopGUIOSS.hook.WorldGuardHook];
     * silently passes when WorldGuard is absent.
     *
     * @param regionId  The WorldGuard region name.
     * @param worldName If specified, the player must also be in this world.
     */
    data class Region(
        val regionId: String,
        val worldName: String? = null,
    ) : Requirement()

    /**
     * The shop item is only available during a time window expressed in
     * Minecraft ticks (0 = sunrise, 6000 = noon, 12000 = sunset, 18000 = midnight).
     *
     * @param fromTick  Start of the allowed window (inclusive, 0–23999).
     * @param toTick    End of the allowed window (inclusive, 0–23999).
     *                  If [toTick] < [fromTick] the window wraps around midnight.
     */
    data class TimeOfDay(
        val fromTick: Int,
        val toTick: Int,
    ) : Requirement() {

        /** Return true if [worldTime] (0–23999) falls within this window. */
        fun isMet(worldTime: Long): Boolean {
            val t = (worldTime % 24000).toInt()
            return if (fromTick <= toTick) t in fromTick..toTick
            else t >= fromTick || t <= toTick   // wraps midnight
        }
    }
}
