package io.github.Earth1283.economyShopGUIOSS.economy

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS

/**
 * Initialises and holds all available [EconomyProvider] instances.
 *
 * At startup, [init] constructs every provider and registers those whose
 * [EconomyProvider.isAvailable] is `true`.  Call [resolve] to retrieve the
 * provider for a given [EconomyType]; it always returns a provider (falling
 * back to [XPEconomy] if the requested backend is unavailable).
 *
 * The XP provider needs no third-party plugin and is therefore always
 * registered as the final fallback.
 */
class EconomyRegistry(private val plugin: EconomyShopGUIOSS) {

    private val providers = mutableMapOf<EconomyType, EconomyProvider>()

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    fun init() {
        val hooks = plugin.hookManager

        // Always available — no plugin dependency
        register(XPEconomy())

        // Soft-depend economy plugins
        if (hooks.hasVault)        tryRegister(VaultEconomy())
        if (hooks.hasPlayerPoints) tryRegister(PlayerPointsEconomy())
        if (hooks.hasGemsEconomy)  tryRegister(GemsEconomy())

        plugin.logger.info("Economies available: ${providers.keys.joinToString { it::class.simpleName ?: it.toString() }}")
    }

    // ── Resolution ────────────────────────────────────────────────────────────

    /**
     * Return the [EconomyProvider] for [type].
     *
     * If no provider is registered for [type] (e.g. the plugin is absent),
     * a warning is logged and the [XPEconomy] (always available) is returned
     * so that the plugin remains functional.
     */
    fun resolve(type: EconomyType): EconomyProvider =
        providers[type] ?: run {
            plugin.logger.warning(
                "Economy type ${type::class.simpleName} is not available. " +
                "Falling back to XP economy."
            )
            providers[EconomyType.XP]!!   // XP is always registered
        }

    /** Returns the default economy as configured in `config.yml`. */
    fun resolveDefault(): EconomyProvider =
        resolve(EconomyType.fromString(plugin.configManager.config.defaultEconomy))

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun register(provider: EconomyProvider) {
        providers[provider.type] = provider
    }

    private fun tryRegister(provider: EconomyProvider) {
        if (provider.isAvailable) {
            providers[provider.type] = provider
            plugin.logger.info("Economy registered: ${provider.displayName}")
        } else {
            plugin.logger.warning(
                "Economy plugin detected (${provider.displayName}) but its " +
                "service is not available. Is an economy provider installed?"
            )
        }
    }
}
