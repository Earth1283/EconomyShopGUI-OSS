package io.github.Earth1283.economyShopGUIOSS.economy

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

/**
 * [EconomyProvider] backed by the Vault economy service.
 *
 * Vault is a service-provider abstraction: the actual economy (EssentialsX,
 * CMI, etc.) registers with Vault, and we interact through Vault's interface.
 * This provider locates the registered service at construction time and
 * delegates every operation to it.
 *
 * **Availability:** requires the Vault plugin *and* a registered economy
 * service.  If either is absent, [isAvailable] is `false` and this provider
 * must not be used.
 */
class VaultEconomy : EconomyProvider {

    override val type: EconomyType = EconomyType.Vault
    override val displayName: String = "Vault"

    private val economy: Economy? = Bukkit.getServicesManager()
        .getRegistration(Economy::class.java)
        ?.provider

    override val isAvailable: Boolean get() = economy != null

    override fun getBalance(player: OfflinePlayer): Double =
        economy?.getBalance(player) ?: 0.0

    override fun withdraw(player: OfflinePlayer, amount: Double): Boolean {
        val econ = economy ?: return false
        if (econ.getBalance(player) < amount) return false
        return econ.withdrawPlayer(player, amount).transactionSuccess()
    }

    override fun deposit(player: OfflinePlayer, amount: Double): Boolean {
        val econ = economy ?: return false
        return econ.depositPlayer(player, amount).transactionSuccess()
    }

    override fun format(amount: Double): String =
        economy?.format(amount) ?: amount.toString()
}
