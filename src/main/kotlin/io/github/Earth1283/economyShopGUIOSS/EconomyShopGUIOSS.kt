package io.github.Earth1283.economyShopGUIOSS

import io.github.Earth1283.economyShopGUIOSS.config.ConfigManager
import io.github.Earth1283.economyShopGUIOSS.economy.EconomyRegistry
import io.github.Earth1283.economyShopGUIOSS.hook.HookManager
import io.github.Earth1283.economyShopGUIOSS.lang.LangRegistry
import io.github.Earth1283.economyShopGUIOSS.api.EconomyShopGUIHookImpl
import io.github.Earth1283.economyShopGUIOSS.command.CommandRegistry
import io.github.Earth1283.economyShopGUIOSS.nms.NmsHandler
import io.github.Earth1283.economyShopGUIOSS.nms.NmsHandlerFactory
import io.github.Earth1283.economyShopGUIOSS.gui.InventoryListener
import io.github.Earth1283.economyShopGUIOSS.hook.BStatsMetrics
import io.github.Earth1283.economyShopGUIOSS.hook.PlaceholderAPIExpansion
import io.github.Earth1283.economyShopGUIOSS.shop.ShopManager
import io.github.Earth1283.economyShopGUIOSS.shop.ShopRepository
import io.github.Earth1283.economyShopGUIOSS.shop.price.PriceFormatter
import io.github.Earth1283.economyShopGUIOSS.stands.ShopStandListener
import io.github.Earth1283.economyShopGUIOSS.stands.ShopStandManager
import io.github.Earth1283.economyShopGUIOSS.transaction.TransactionProcessor
import io.github.Earth1283.economyShopGUIOSS.util.UpdateChecker
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

    val configManager:   ConfigManager    by lazy { ConfigManager(this) }
    val langRegistry:    LangRegistry     by lazy { LangRegistry(this) }
    val hookManager:     HookManager      by lazy { HookManager(this) }
    val economyRegistry: EconomyRegistry  by lazy { EconomyRegistry(this) }
    val priceFormatter:  PriceFormatter   by lazy { PriceFormatter(this) }
    val shopRepository:        ShopRepository       by lazy { ShopRepository(this) }
    val shopManager:           ShopManager          by lazy { ShopManager(shopRepository) }
    val transactionProcessor:  TransactionProcessor  by lazy { TransactionProcessor(this) }
    val shopStandManager:      ShopStandManager      by lazy { ShopStandManager(this) }
    val nmsHandler:            NmsHandler             by lazy { NmsHandlerFactory.create() }

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

        // 6 — Shop data (reads YAML from disk, fires ShopItemsLoadEvent)
        shopRepository.load()

        // 7 — Transaction engine (connects SQLite database)
        transactionProcessor.logger.init()

        // 8 — GUI (registers global inventory listener)
        server.pluginManager.registerEvents(InventoryListener(), this)

        // 9 — Commands
        CommandRegistry(this).register()

        // 10 — Shop stands
        shopStandManager.load()
        server.pluginManager.registerEvents(ShopStandListener(this), this)

        // 11 — NMS handler (version detection at first access)
        nmsHandler  // force lazy init so version mismatch surfaces at startup

        // 12 — Public API hook
        EconomyShopGUIHookImpl.INSTANCE = EconomyShopGUIHookImpl(this)

        // 13 — Optional integrations (PAPI, bStats, UpdateChecker)
        if (hookManager.hasPlaceholderAPI) {
            PlaceholderAPIExpansion(this).register()
            logger.info("PlaceholderAPI expansion registered.")
        }
        BStatsMetrics(this).register()
        val checker = UpdateChecker(this)
        server.pluginManager.registerEvents(checker, this)
        checker.checkAsync()

        logger.info("EconomyShopGUI-OSS v${pluginMeta.version} enabled.")
    }

    override fun onDisable() {
        EconomyShopGUIHookImpl.INSTANCE = null
        logger.info("EconomyShopGUI-OSS disabled.")
    }
}
