package io.github.Earth1283.economyShopGUIOSS.config

/**
 * Typed, immutable snapshot of `config.yml`.
 *
 * Created by [ConfigManager.load] and replaced on each `/sreload`.
 * All references to the current configuration go through
 * [io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS.configManager]`.config`.
 */
data class MainConfig(

    // ── Language ──────────────────────────────────────────────────────────────
    /** Language file suffix, e.g. `"en"` → `lang-en.yml`. */
    val language: String,

    // ── Currency Formatting ───────────────────────────────────────────────────
    /** Java locale tag used for price formatting (e.g. `"en-US"`). */
    val locale: String,
    /** Java `DecimalFormat` pattern for raw price display (e.g. `"#,##0.00"`). */
    val currencyFormat: String,
    val abbreviationsEnabled: Boolean,
    /** Map of suffix → threshold value (e.g. `"k" to 1_000`). */
    val abbreviationThresholds: Map<String, Long>,

    // ── Economy ───────────────────────────────────────────────────────────────
    /** Default economy type identifier when a section has none specified. */
    val defaultEconomy: String,

    // ── Shop Behaviour ────────────────────────────────────────────────────────
    val matchMeta: Boolean,
    val useItemName: Boolean,
    val prioritizeItemLore: Boolean,
    val middleClickSellAll: Boolean,
    val escapeBack: Boolean,

    // ── Main Menu ─────────────────────────────────────────────────────────────
    /** Inventory size for the main shop menu (must be 9–54, multiple of 9). */
    val mainMenuSize: Int,

    // ── Shop Stands ───────────────────────────────────────────────────────────
    val shopStandsEnabled: Boolean,
    val shopStandsHolograms: Boolean,

    // ── Transaction Logging ───────────────────────────────────────────────────
    val transactionLogEnabled: Boolean,
    val transactionLogFile: String,
    val advancedTransactionLogEnabled: Boolean,
    val advancedTransactionLogDatabase: String,

    // ── Integrations ─────────────────────────────────────────────────────────
    val resizeGuiBedrock: Boolean,
    val placeholderCacheSeconds: Int,

    // ── Performance ───────────────────────────────────────────────────────────
    val usePaperMeta: Boolean,

    // ── Miscellaneous ─────────────────────────────────────────────────────────
    val updateChecking: Boolean,
    val debug: Boolean,

    // ── Disabled Worlds ───────────────────────────────────────────────────────
    /** World names where `/shop` is blocked. */
    val disabledWorldsShop: Set<String>,
    /** World names where `/sellall` is blocked. */
    val disabledWorldsSellAll: Set<String>,
    /** World names where `/sellgui` is blocked. */
    val disabledWorldsSellGui: Set<String>,
) {
    companion object {
        /** Sensible defaults — used when `config.yml` is missing a key. */
        val DEFAULTS = MainConfig(
            language = "en",
            locale = "en-US",
            currencyFormat = "#,##0.00",
            abbreviationsEnabled = true,
            abbreviationThresholds = mapOf(
                "k" to 1_000L,
                "m" to 1_000_000L,
                "b" to 1_000_000_000L,
                "t" to 1_000_000_000_000L,
                "q" to 1_000_000_000_000_000L,
            ),
            defaultEconomy = "vault",
            matchMeta = false,
            useItemName = false,
            prioritizeItemLore = false,
            middleClickSellAll = true,
            escapeBack = true,
            mainMenuSize = 54,
            shopStandsEnabled = true,
            shopStandsHolograms = true,
            transactionLogEnabled = false,
            transactionLogFile = "transactions.log",
            advancedTransactionLogEnabled = false,
            advancedTransactionLogDatabase = "transactions.db",
            resizeGuiBedrock = true,
            placeholderCacheSeconds = 0,
            usePaperMeta = true,
            updateChecking = true,
            debug = false,
            disabledWorldsShop = emptySet(),
            disabledWorldsSellAll = emptySet(),
            disabledWorldsSellGui = emptySet(),
        )
    }
}
