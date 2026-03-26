package io.github.Earth1283.economyShopGUIOSS.config

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/**
 * Owns the complete configuration lifecycle: saving defaults, loading YAML,
 * and exposing a typed [MainConfig] snapshot.
 *
 * After a successful [load], access parsed values through [config].
 * Call [load] again (e.g. from `/sreload`) to pick up any file changes.
 */
class ConfigManager(private val plugin: EconomyShopGUIOSS) {

    /** The most recently loaded typed configuration. Replaced on each [load]. */
    var config: MainConfig = MainConfig.DEFAULTS
        private set

    // ── Directories ───────────────────────────────────────────────────────────

    /** `plugins/EconomyShopGUI-OSS/shops/` — one YAML file per shop section. */
    val shopsDir: File get() = plugin.dataFolder.resolve("shops")

    /** `plugins/EconomyShopGUI-OSS/sections/` — one YAML file per section metadata. */
    val sectionsDir: File get() = plugin.dataFolder.resolve("sections")

    /** `plugins/EconomyShopGUI-OSS/layouts/` — installed layout templates. */
    val layoutsDir: File get() = plugin.dataFolder.resolve("layouts")

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /**
     * Save bundled defaults if absent, then parse `config.yml` into [config].
     * Safe to call multiple times (idempotent for file creation; always re-reads
     * the YAML on subsequent calls so reloads pick up changes).
     */
    fun load() {
        ensureDataFolderExists()
        plugin.saveDefaultConfig()          // copies bundled config.yml if missing
        plugin.reloadConfig()               // re-reads config.yml from disk

        shopsDir.mkdirs()
        sectionsDir.mkdirs()
        layoutsDir.mkdirs()

        config = parseConfig(plugin.config)

        if (config.debug) {
            plugin.logger.info("[Debug] Configuration loaded: $config")
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun ensureDataFolderExists() {
        if (!plugin.dataFolder.exists()) plugin.dataFolder.mkdirs()
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseConfig(yaml: org.bukkit.configuration.Configuration): MainConfig {
        val defaults = MainConfig.DEFAULTS

        val abbreviationThresholds: Map<String, Long> =
            yaml.getConfigurationSection("abbreviations.thresholds")
                ?.getValues(false)
                ?.mapValues { (_, v) ->
                    when (v) {
                        is Long   -> v
                        is Int    -> v.toLong()
                        is Number -> v.toLong()
                        else      -> 0L
                    }
                }
                ?: defaults.abbreviationThresholds

        return MainConfig(
            language                     = yaml.getString("language", defaults.language)!!,
            locale                       = yaml.getString("locale", defaults.locale)!!,
            currencyFormat               = yaml.getString("currency-format", defaults.currencyFormat)!!,
            abbreviationsEnabled         = yaml.getBoolean("abbreviations.enabled", defaults.abbreviationsEnabled),
            abbreviationThresholds       = abbreviationThresholds,
            defaultEconomy               = yaml.getString("economy", defaults.defaultEconomy)!!,
            matchMeta                    = yaml.getBoolean("match-meta", defaults.matchMeta),
            useItemName                  = yaml.getBoolean("use-item-name", defaults.useItemName),
            prioritizeItemLore           = yaml.getBoolean("prioritize-item-lore", defaults.prioritizeItemLore),
            middleClickSellAll           = yaml.getBoolean("middle-click-sell-all", defaults.middleClickSellAll),
            escapeBack                   = yaml.getBoolean("escape-back", defaults.escapeBack),
            mainMenuSize                 = yaml.getInt("main-menu-size", defaults.mainMenuSize)
                .coerceIn(9, 54).let { it - (it % 9) },   // enforce multiple-of-9
            mainMenuRows                 = yaml.getInt("main-menu-rows", defaults.mainMenuRows).coerceIn(1, 6),
            mainMenuTitle                = yaml.getString("main-menu-title", defaults.mainMenuTitle)!!,
            mainMenuFillItem             = yaml.getString("main-menu-fill-item", null),
            shopStandsEnabled            = yaml.getBoolean("shop-stands.enabled", defaults.shopStandsEnabled),
            shopStandsHolograms          = yaml.getBoolean("shop-stands.holograms", defaults.shopStandsHolograms),
            transactionLogEnabled        = yaml.getBoolean("transaction-log.enabled", defaults.transactionLogEnabled),
            transactionLogFile           = yaml.getString("transaction-log.file", defaults.transactionLogFile)!!,
            advancedTransactionLogEnabled = yaml.getBoolean("advanced-transaction-log.enabled", defaults.advancedTransactionLogEnabled),
            advancedTransactionLogDatabase = yaml.getString("advanced-transaction-log.database", defaults.advancedTransactionLogDatabase)!!,
            resizeGuiBedrock             = yaml.getBoolean("resize-gui-bedrock", defaults.resizeGuiBedrock),
            placeholderCacheSeconds      = yaml.getInt("placeholder-cache-seconds", defaults.placeholderCacheSeconds),
            usePaperMeta                 = yaml.getBoolean("use-paper-meta", defaults.usePaperMeta),
            marketplaceUrl               = yaml.getString("marketplace.url",   defaults.marketplaceUrl)!!,
            marketplaceToken             = yaml.getString("marketplace.token", defaults.marketplaceToken)!!,
            updateChecking               = yaml.getBoolean("update-checking", defaults.updateChecking),
            debug                        = yaml.getBoolean("debug", defaults.debug),
            disabledWorldsShop           = yaml.getStringList("disabled-worlds.shop").toSet(),
            disabledWorldsSellAll        = yaml.getStringList("disabled-worlds.sellall").toSet(),
            disabledWorldsSellGui        = yaml.getStringList("disabled-worlds.sellgui").toSet(),
        )
    }

    /**
     * Load an arbitrary YAML file from the plugin data folder.
     * Returns null (and logs a warning) if the file does not exist.
     */
    fun loadYaml(relativePath: String): YamlConfiguration? {
        val file = plugin.dataFolder.resolve(relativePath)
        if (!file.exists()) {
            plugin.logger.warning("File not found: ${file.path}")
            return null
        }
        return YamlConfiguration.loadConfiguration(file)
    }

    /**
     * Save a [YamlConfiguration] to [relativePath] inside the plugin data folder.
     * Parent directories are created automatically.
     */
    fun saveYaml(relativePath: String, yaml: YamlConfiguration) {
        val file = plugin.dataFolder.resolve(relativePath)
        file.parentFile?.mkdirs()
        yaml.save(file)
    }
}
