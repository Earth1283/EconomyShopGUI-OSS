package io.github.Earth1283.economyShopGUIOSS.economy

import org.black_ixx.playerpoints.PlayerPoints
import org.black_ixx.playerpoints.PlayerPointsAPI
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

/**
 * [EconomyProvider] backed by the PlayerPoints plugin.
 *
 * PlayerPoints stores integer point values per player.  All [Double] amounts
 * are truncated to [Int] since the plugin only handles whole numbers.
 *
 * **Availability:** requires the PlayerPoints plugin to be loaded and its API
 * to be accessible.  [isAvailable] is `false` when the plugin is absent.
 */
class PlayerPointsEconomy : EconomyProvider {

    override val type: EconomyType = EconomyType.PlayerPoints
    override val displayName: String = "PlayerPoints"

    private val api: PlayerPointsAPI? by lazy {
        val plugin = Bukkit.getPluginManager().getPlugin("PlayerPoints")
            as? PlayerPoints ?: return@lazy null
        plugin.api
    }

    override val isAvailable: Boolean get() = api != null

    override fun getBalance(player: OfflinePlayer): Double =
        api?.look(player.uniqueId)?.toDouble() ?: 0.0

    override fun withdraw(player: OfflinePlayer, amount: Double): Boolean {
        val points = api ?: return false
        val cost = amount.toInt()
        if (points.look(player.uniqueId) < cost) return false
        return points.take(player.uniqueId, cost)
    }

    override fun deposit(player: OfflinePlayer, amount: Double): Boolean {
        val points = api ?: return false
        return points.give(player.uniqueId, amount.toInt())
    }

    override fun format(amount: Double): String = "${amount.toInt()} Points"
}
