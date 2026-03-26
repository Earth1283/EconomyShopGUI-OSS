package io.github.Earth1283.economyShopGUIOSS.economy

import org.bukkit.OfflinePlayer

/**
 * Common contract for all economy backends.
 *
 * Implement this interface to add support for a new economy plugin.
 * Implementations are registered in [EconomyRegistry] and selected per
 * shop section based on its configured [EconomyType].
 *
 * All balance operations are synchronous; callers are responsible for
 * dispatching to an async context when needed.
 */
interface EconomyProvider {

    /** The economy type this provider handles. */
    val type: EconomyType

    /**
     * Whether the backing plugin is present and its service is available.
     * The [EconomyRegistry] only registers providers where this is `true`.
     */
    val isAvailable: Boolean

    // ── Balance ───────────────────────────────────────────────────────────────

    /** Return the current balance for [player]. */
    fun getBalance(player: OfflinePlayer): Double

    /**
     * Withdraw [amount] from [player]'s balance.
     * @return `true` if the withdrawal succeeded; `false` if the player had
     *         insufficient funds or the operation was rejected by the plugin.
     */
    fun withdraw(player: OfflinePlayer, amount: Double): Boolean

    /**
     * Deposit [amount] into [player]'s balance.
     * @return `true` if the deposit succeeded.
     */
    fun deposit(player: OfflinePlayer, amount: Double): Boolean

    // ── Display ───────────────────────────────────────────────────────────────

    /**
     * Format [amount] as a human-readable currency string according to the
     * conventions of this economy (e.g. `"$1,234.56"` for Vault, `"1234 XP"`
     * for the XP provider).
     */
    fun format(amount: Double): String

    /**
     * A display name for this economy used in log messages and admin feedback
     * (e.g. `"Vault"`, `"XP"`, `"PlayerPoints"`).
     */
    val displayName: String
}
