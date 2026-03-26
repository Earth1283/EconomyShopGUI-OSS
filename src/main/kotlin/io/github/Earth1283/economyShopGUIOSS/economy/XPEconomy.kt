package io.github.Earth1283.economyShopGUIOSS.economy

import org.bukkit.OfflinePlayer

/**
 * [EconomyProvider] that uses Minecraft experience points as currency.
 *
 * The "balance" is the player's **total accumulated experience** (not just
 * the current bar), derived from their level and progress bar position using
 * the vanilla XP-to-level formulae.
 *
 * Withdrawals remove XP by converting the deducted amount back to the nearest
 * equivalent level/progress pair.  Deposits grant XP directly via
 * [org.bukkit.entity.Player.giveExp].
 *
 * This provider is always [isAvailable] — it requires no third-party plugin.
 */
class XPEconomy : EconomyProvider {

    override val type: EconomyType = EconomyType.XP
    override val displayName: String = "XP"
    override val isAvailable: Boolean = true

    override fun getBalance(player: OfflinePlayer): Double {
        val online = player.player ?: return 0.0
        return totalExperience(online.level, online.exp).toDouble()
    }

    override fun withdraw(player: OfflinePlayer, amount: Double): Boolean {
        val online = player.player ?: return false
        val current = totalExperience(online.level, online.exp)
        val cost = amount.toLong()
        if (current < cost) return false
        online.totalExperience = (current - cost).coerceAtLeast(0).toInt()
        return true
    }

    override fun deposit(player: OfflinePlayer, amount: Double): Boolean {
        val online = player.player ?: return false
        online.giveExp(amount.toInt(), true)
        return true
    }

    override fun format(amount: Double): String =
        "${amount.toLong()} XP"

    // ── Vanilla XP formula helpers ────────────────────────────────────────────

    /**
     * Calculate total experience points accumulated at [level] with [progress]
     * (0.0–1.0) through the next level, using Minecraft's piecewise formula.
     */
    private fun totalExperience(level: Int, progress: Float): Long {
        val base = totalXpForLevel(level)
        val pointsToNext = xpForNextLevel(level)
        return (base + (progress * pointsToNext)).toLong()
    }

    /** Total XP required to *reach* [level] from zero. */
    private fun totalXpForLevel(level: Int): Long = when {
        level <= 16  -> (level * level + 6 * level).toLong()
        level <= 31  -> (2.5 * level * level - 40.5 * level + 360).toLong()
        else         -> (4.5 * level * level - 162.5 * level + 2220).toLong()
    }

    /** XP needed to go from [level] to [level]+1. */
    private fun xpForNextLevel(level: Int): Int = when {
        level <= 15 -> 2 * level + 7
        level <= 30 -> 5 * level - 38
        else        -> 9 * level - 158
    }
}
