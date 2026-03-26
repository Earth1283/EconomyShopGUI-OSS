package io.github.Earth1283.economyShopGUIOSS.hook

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

/**
 * PlaceholderAPI expansion that exposes shop data as `%economyshopgui_<placeholder>%`.
 *
 * Available placeholders:
 * - `%economyshopgui_balance%`                       — player's Vault balance
 * - `%economyshopgui_buy_<section>_<item>%`          — current buy price
 * - `%economyshopgui_sell_<section>_<item>%`         — current sell price
 * - `%economyshopgui_section_count%`                 — number of enabled sections
 * - `%economyshopgui_item_count%`                    — total number of shop items
 */
class PlaceholderAPIExpansion(private val plugin: EconomyShopGUIOSS) : PlaceholderExpansion() {

    override fun getIdentifier(): String = "economyshopgui"
    override fun getAuthor(): String     = "EconomyShopGUI-OSS"
    override fun getVersion(): String    = plugin.pluginMeta.version
    override fun persist(): Boolean      = true
    override fun canRegister(): Boolean  = true

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        return when {
            params == "balance" -> {
                val onlinePlayer = player?.player ?: return "N/A"
                val provider = plugin.economyRegistry.resolveDefault()
                plugin.priceFormatter.format(provider.getBalance(onlinePlayer))
            }

            params == "section_count" ->
                plugin.shopManager.allSections().count { it.enabled }.toString()

            params == "item_count" ->
                plugin.shopManager.allSections().sumOf { it.allItems.size }.toString()

            params.startsWith("buy_") -> {
                val rest = params.removePrefix("buy_")
                val (sectionId, itemId) = splitItemRef(rest) ?: return null
                val item = plugin.shopManager.findItem(sectionId, itemId) ?: return null
                item.buyPrice?.let { plugin.priceFormatter.format(it) } ?: "N/A"
            }

            params.startsWith("sell_") -> {
                val rest = params.removePrefix("sell_")
                val (sectionId, itemId) = splitItemRef(rest) ?: return null
                val item = plugin.shopManager.findItem(sectionId, itemId) ?: return null
                item.sellPrice?.let { plugin.priceFormatter.format(it) } ?: "N/A"
            }

            else -> null
        }
    }

    private fun splitItemRef(ref: String): Pair<String, String>? {
        val idx = ref.indexOf('_')
        if (idx < 0) return null
        return ref.substring(0, idx) to ref.substring(idx + 1)
    }
}
