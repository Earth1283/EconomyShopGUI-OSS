package io.github.Earth1283.economyShopGUIOSS

import io.github.Earth1283.economyShopGUIOSS.config.ConfigManager
import io.github.Earth1283.economyShopGUIOSS.economy.EconomyRegistry
import io.github.Earth1283.economyShopGUIOSS.hook.HookManager
import io.github.Earth1283.economyShopGUIOSS.lang.LangRegistry
import io.github.Earth1283.economyShopGUIOSS.shop.price.PriceFormatter
import org.bukkit.plugin.java.JavaPlugin

/**
 * Plugin entry point and composition root.
 *
 * All managers are exposed as `by lazy` properties so that:
 * - initialisation failures surface with a clear stack trace,
 * - the ordering of [onEnable] is the single authoritative init sequence, and
 * - no manager is constructed until it is first accessed.
 *
 * External code should access shared state via `EconomyShopGUIOSS.instance.<manager>`
 * rather than keeping their own references.
 *
 * ## Manager additions per implementation phase
 * - Phase 2 : `hookManager`, `economyRegistry`
 * - Phase 3 : `shopRepository`, `shopManager`
 * - Phase 4 : `transactionProcessor`, `transactionLogger`
 * - Phase 5 : `guiManager`
 * - Phase 6 : `commandRegistry`
 * - Phase 8 : `spawnerRegistry`
 * - Phase 9 : `shopStandManager`
 */
class EconomyShopGUIOSS : JavaPlugin() {

    companion object {
        /**
         * The active plugin instance. Only valid after [onEnable] completes.
         * Accessing this before the plugin enables will throw
         * [UninitializedPropertyAccessException].
         */
        lateinit var instance: EconomyShopGUIOSS
            private set
    }

    // -- Managers --------------------------------------------------------------

    val configManager: ConfigManager   by lazy { ConfigManager(this) }
    val langRegistry: LangRegistry     by lazy { LangRegistry(this) }
    val hookManager: HookManager       by lazy { HookManager(this) }
    val economyRegistry: EconomyRegistry by lazy { EconomyRegistry(this) }
    val priceFormatter: PriceFormatter by lazy { PriceFormatter(this) }

    // -- Lifecycle -------------------------------------------------------------

    override fun onEnable() {
        instance = this

        // 1 — Configuration (no dependencies)
        configManager.load()

        // 2 — Language (reads language key from configManager)
        langRegistry.load()

        // 3 — Hook detection (queries PluginManager, no plugin dependencies)
        hookManager.register()

        // 4 — Economy (depends on hookManager for availability checks)
        economyRegistry.init()

        // 5 — Price formatter (depends on configManager for format pattern)
        priceFormatter.load()

        logger.info("EconomyShopGUI-OSS v${pluginMeta.version} enabled.")
    }

    override fun onDisable() {
        logger.info("EconomyShopGUI-OSS disabled.")
    }
}
