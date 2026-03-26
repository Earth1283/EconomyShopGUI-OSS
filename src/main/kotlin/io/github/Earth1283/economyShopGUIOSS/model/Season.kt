package io.github.Earth1283.economyShopGUIOSS.model

/**
 * The four seasons used by the [PriceModifier.Seasonal] system.
 *
 * The current season is resolved at runtime by
 * [io.github.Earth1283.economyShopGUIOSS.hook.RealisticSeasonsHook] when
 * RealisticSeasons is installed, or by the server's local time when absent.
 */
enum class Season {
    SPRING,
    SUMMER,
    FALL,
    WINTER;

    companion object {
        /**
         * Derive the season from the current month of the year (calendar-based
         * fallback used when RealisticSeasons is not installed).
         * Northern-hemisphere convention: Dec–Feb = WINTER, Mar–May = SPRING,
         * Jun–Aug = SUMMER, Sep–Nov = FALL.
         */
        fun fromMonth(month: Int): Season = when (month) {
            12, 1, 2 -> WINTER
            3, 4, 5  -> SPRING
            6, 7, 8  -> SUMMER
            else     -> FALL
        }
    }
}
