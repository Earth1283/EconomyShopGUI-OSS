package io.github.Earth1283.economyShopGUIOSS.economy

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

/**
 * [EconomyProvider] backed by the GemsEconomy plugin.
 *
 * GemsEconomy does not publish a stable Maven artifact, so this provider
 * uses reflection to access its API without a compile-time dependency.
 * All class and method references are resolved lazily at runtime; if the
 * plugin is absent the reflection lookups simply fail and [isAvailable]
 * returns `false`.
 *
 * Supported class name: ``me.xanium.gemseconomy.api.GemsEconomyAPI``
 */
class GemsEconomy : EconomyProvider {

    override val type: EconomyType = EconomyType.Gems
    override val displayName: String = "GemsEconomy"

    // ── Reflective API access ─────────────────────────────────────────────────

    private val apiClass: Class<*>? by lazy {
        runCatching { Class.forName("me.xanium.gemseconomy.api.GemsEconomyAPI") }.getOrNull()
    }

    /** The GemsEconomyAPI instance obtained from the Bukkit services manager. */
    private val apiInstance: Any? by lazy {
        val cls = apiClass ?: return@lazy null
        @Suppress("UNCHECKED_CAST")
        Bukkit.getServicesManager()
            .getRegistration(cls as Class<Any>)
            ?.provider
    }

    override val isAvailable: Boolean
        get() = apiClass != null && apiInstance != null

    // ── Balance operations ────────────────────────────────────────────────────

    override fun getBalance(player: OfflinePlayer): Double =
        invokeDouble("getBalance", player.uniqueId) ?: 0.0

    override fun withdraw(player: OfflinePlayer, amount: Double): Boolean {
        val balance = invokeDouble("getBalance", player.uniqueId) ?: return false
        if (balance < amount) return false
        invokeVoid("withdraw", player.uniqueId, amount)
        return true
    }

    override fun deposit(player: OfflinePlayer, amount: Double): Boolean {
        invokeVoid("deposit", player.uniqueId, amount)
        return true
    }

    override fun format(amount: Double): String =
        invokeString("format", amount) ?: "$amount Gems"

    // ── Reflection helpers ────────────────────────────────────────────────────

    private fun invokeDouble(method: String, vararg args: Any?): Double? =
        invoke(method, *args) as? Double

    private fun invokeString(method: String, vararg args: Any?): String? =
        invoke(method, *args) as? String

    private fun invokeVoid(method: String, vararg args: Any?) {
        invoke(method, *args)
    }

    private fun invoke(method: String, vararg args: Any?): Any? {
        val instance = apiInstance ?: return null
        return runCatching {
            val paramTypes = args.map { it?.javaClass ?: Any::class.java }.toTypedArray()
            instance.javaClass.getMethod(method, *paramTypes).invoke(instance, *args)
        }.getOrNull()
    }
}
