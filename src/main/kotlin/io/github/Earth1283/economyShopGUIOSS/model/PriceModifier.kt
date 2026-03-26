package io.github.Earth1283.economyShopGUIOSS.model

/**
 * A price modification rule applied to a buy or sell price.
 *
 * Modifiers are stored on [ShopSection] (section-wide) and [ShopItem]
 * (item-specific).  During a transaction the modifier chain is applied in
 * declaration order via [applyToBuy] / [applyToSell].
 *
 * All subtypes are sealed so [when] expressions are exhaustive.
 */
sealed class PriceModifier {

    /**
     * Multiply the price by a per-season factor.
     *
     * A factor of 1.0 means no change; 1.5 means 50 % more expensive;
     * 0.8 means 20 % cheaper.  The current season is resolved by
     * [io.github.Earth1283.economyShopGUIOSS.hook.RealisticSeasonsHook].
     */
    data class Seasonal(
        val spring: Double = 1.0,
        val summer: Double = 1.0,
        val fall:   Double = 1.0,
        val winter: Double = 1.0,
    ) : PriceModifier() {

        fun factorFor(season: Season): Double = when (season) {
            Season.SPRING -> spring
            Season.SUMMER -> summer
            Season.FALL   -> fall
            Season.WINTER -> winter
        }
    }

    /**
     * Subtract a percentage from the price.
     *
     * [percent] is in the range 0–100.  A value of 20 means 20 % off.
     * Applied as: `price * (1 - percent / 100)`.
     */
    data class Discount(
        val percent: Double,
    ) : PriceModifier()

    /**
     * Multiply the price by [factor].
     *
     * A factor of 2.0 doubles all prices in the section.
     */
    data class Multiplier(
        val factor: Double,
    ) : PriceModifier()

    // ── Application helpers ───────────────────────────────────────────────────

    /**
     * Apply this modifier to [price], resolving seasonal factor from [season]
     * if applicable.  Returns the adjusted price (always ≥ 0).
     */
    fun apply(price: Double, season: Season? = null): Double {
        val adjusted = when (this) {
            is Seasonal   -> price * (season?.let { factorFor(it) } ?: 1.0)
            is Discount   -> price * (1.0 - percent / 100.0)
            is Multiplier -> price * factor
        }
        return adjusted.coerceAtLeast(0.0)
    }
}

/**
 * Apply the full [modifiers] chain to [price] in order.
 * Returns the final adjusted price.
 */
fun List<PriceModifier>.applyAll(price: Double, season: Season? = null): Double =
    fold(price) { acc, mod -> mod.apply(acc, season) }
