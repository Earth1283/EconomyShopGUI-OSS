package io.github.Earth1283.economyShopGUIOSS.shop.price

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import io.github.Earth1283.economyShopGUIOSS.economy.EconomyProvider
import io.github.Earth1283.economyShopGUIOSS.economy.EconomyType
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Formats [Double] price values into human-readable strings.
 *
 * Formatting logic, in priority order:
 * 1. If the backing [EconomyProvider] can format the value (e.g. Vault adds
 *    the currency symbol), delegate to it.
 * 2. If abbreviations are enabled and the value is large enough, abbreviate
 *    it (e.g. `1500.0` → `"1.5k"`).
 * 3. Fall back to the [DecimalFormat] pattern from `config.yml`.
 *
 * A new [PriceFormatter] is created on each [load] / `/sreload` call so that
 * config changes are picked up without a restart.
 */
class PriceFormatter(private val plugin: EconomyShopGUIOSS) {

    private lateinit var decimalFormat: DecimalFormat
    private lateinit var sortedThresholds: List<Pair<String, Long>>
    private var abbreviationsEnabled: Boolean = false

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /**
     * (Re-)initialise the formatter from the current [MainConfig].
     * Called by [EconomyShopGUIOSS.onEnable] and on `/sreload`.
     */
    fun load() {
        val cfg = plugin.configManager.config

        val locale = runCatching { Locale.forLanguageTag(cfg.locale) }.getOrDefault(Locale.US)
        decimalFormat = DecimalFormat(cfg.currencyFormat, DecimalFormatSymbols(locale))

        abbreviationsEnabled = cfg.abbreviationsEnabled
        // Sort descending so the largest threshold is tried first
        sortedThresholds = cfg.abbreviationThresholds.entries
            .sortedByDescending { it.value }
            .map { it.key to it.value }
    }

    // ── Formatting ────────────────────────────────────────────────────────────

    /**
     * Format [amount] using the given [provider] for currency-symbol decoration.
     * Abbreviation is applied before delegation so that `"1.5k"` is formatted
     * by the provider (e.g. Vault might turn it into `"$1.5k"`).
     */
    fun format(amount: Double, provider: EconomyProvider): String {
        val abbreviated = tryAbbreviate(amount)
        return if (abbreviated != null) {
            // Delegate to provider with the abbreviated numeric string; Vault
            // and others accept arbitrary strings in their format() call.
            provider.format(abbreviated.numericPart) + abbreviated.suffix
        } else {
            provider.format(amount)
        }
    }

    /**
     * Format [amount] using the plain [DecimalFormat] pattern (no currency
     * symbol — used for display in item lore and GUI elements).
     */
    fun format(amount: Double): String {
        val abbreviated = tryAbbreviate(amount)
        return if (abbreviated != null) {
            decimalFormat.format(abbreviated.numericPart) + abbreviated.suffix
        } else {
            decimalFormat.format(amount)
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private data class Abbreviated(val numericPart: Double, val suffix: String)

    private fun tryAbbreviate(amount: Double): Abbreviated? {
        if (!abbreviationsEnabled) return null
        val absAmount = kotlin.math.abs(amount)
        for ((suffix, threshold) in sortedThresholds) {
            if (absAmount >= threshold) {
                return Abbreviated(amount / threshold, suffix)
            }
        }
        return null
    }
}
