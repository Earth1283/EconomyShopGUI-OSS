package io.github.Earth1283.economyShopGUIOSS.hook

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import org.bstats.bukkit.Metrics

/**
 * Registers bStats metrics for the plugin.
 *
 * The bStats plugin ID is registered at https://bstats.org/ — use the ID
 * assigned to "EconomyShopGUI-OSS" once the project is published.
 * For now the placeholder ID `0` is used (will not submit real data).
 *
 * Custom charts:
 * - Economy type distribution
 * - Section count
 * - Item count
 */
class BStatsMetrics(private val plugin: EconomyShopGUIOSS) {

    private val pluginId = 0  // Replace with real bStats plugin ID after publishing

    fun register() {
        val metrics = Metrics(plugin, pluginId)

        metrics.addCustomChart(
            org.bstats.charts.SimplePie("economy_type") {
                plugin.configManager.config.defaultEconomy
            }
        )

        metrics.addCustomChart(
            org.bstats.charts.SingleLineChart("section_count") {
                plugin.shopManager.allSections().size
            }
        )

        metrics.addCustomChart(
            org.bstats.charts.SingleLineChart("item_count") {
                plugin.shopManager.allSections().sumOf { it.allItems.size }
            }
        )
    }
}
