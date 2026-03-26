package io.github.Earth1283.economyShopGUIOSS.shop.price

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import io.github.Earth1283.economyShopGUIOSS.model.PriceModifier
import io.github.Earth1283.economyShopGUIOSS.model.Season
import io.github.Earth1283.economyShopGUIOSS.model.ShopItem
import io.github.Earth1283.economyShopGUIOSS.model.ShopSection
import org.bukkit.entity.Player

/**
 * Computes the final price for a [ShopItem] after applying all relevant
 * [PriceModifier]s in a deterministic order:
 *
 * 1. Section-level modifiers (seasonal → multiplier → discount)
 * 2. Item-level modifiers (same order)
 *
 * Price never drops below `0.0`.
 */
class PriceModifierEngine(private val plugin: EconomyShopGUIOSS) {

    /**
     * Compute the effective buy price for [item], or `null` if no buy price
     * is configured.
     *
     * [section] is required to apply section-level modifiers.
     */
    fun effectiveBuyPrice(item: ShopItem, section: ShopSection, player: Player): Double? {
        val base = item.buyPrice ?: return null
        return applyModifiers(base, item, section)
    }

    /**
     * Compute the effective sell price for [item], or `null` if no sell price
     * is configured.
     */
    fun effectiveSellPrice(item: ShopItem, section: ShopSection, player: Player): Double? {
        val base = item.sellPrice ?: return null
        return applyModifiers(base, item, section)
    }

    private fun applyModifiers(base: Double, item: ShopItem, section: ShopSection): Double {
        val season = currentSeason()
        var price = base
        // Section modifiers first
        price = section.priceModifiers.fold(price) { acc, mod -> mod.apply(acc, season) }
        // Then item-level overrides
        price = item.modifiers.fold(price) { acc, mod -> mod.apply(acc, season) }
        return price.coerceAtLeast(0.0)
    }

    private fun currentSeason(): Season {
        // Prefer RealisticSeasons hook if present
        if (plugin.hookManager.hasRealisticSeasons) {
            return try {
                val rs = plugin.server.pluginManager.getPlugin("RealisticSeasons")
                    ?: return Season.fromMonth(java.time.LocalDate.now().monthValue)
                // RealisticSeasons API: SeasonAPI.getInstance().getCurrentSeason()
                val api = rs::class.java.getMethod("getSeasonAPI").invoke(rs)
                val name = api::class.java.getMethod("getCurrentSeason").invoke(api).toString().uppercase()
                Season.entries.firstOrNull { it.name == name }
                    ?: Season.fromMonth(java.time.LocalDate.now().monthValue)
            } catch (_: Exception) {
                Season.fromMonth(java.time.LocalDate.now().monthValue)
            }
        }
        return Season.fromMonth(java.time.LocalDate.now().monthValue)
    }
}
