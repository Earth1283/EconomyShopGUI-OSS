package io.github.Earth1283.economyShopGUIOSS.lang

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import io.github.Earth1283.economyShopGUIOSS.util.ColorUtils
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.configuration.file.YamlConfiguration

/**
 * Loads the active language file and resolves [Lang] keys to [Component]s.
 *
 * ### Resolution order
 * 1. The loaded `lang-<language>.yml` value (if the key is present).
 * 2. The [Lang.default] MiniMessage string (built-in fallback).
 *
 * Call [load] at startup and again after each `/sreload`.
 */
class LangRegistry(private val plugin: EconomyShopGUIOSS) {

    /** Raw MiniMessage strings indexed by [Lang]. Empty until [load] is called. */
    private val messages = mutableMapOf<Lang, String>()

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /**
     * Determine the configured language, copy the bundled file if absent,
     * and populate [messages] from the YAML on disk.
     */
    fun load() {
        val lang = plugin.configManager.config.language
        val resourceName = "lang-$lang.yml"
        val file = plugin.dataFolder.resolve(resourceName)

        // Copy bundled lang file to data folder on first run (never overwrite)
        if (!file.exists()) {
            runCatching { plugin.saveResource(resourceName, false) }
                .onFailure {
                    plugin.logger.warning(
                        "No bundled resource for '$resourceName'. " +
                        "Falling back to built-in defaults."
                    )
                    return
                }
        }

        val yaml = YamlConfiguration.loadConfiguration(file)

        messages.clear()
        for (key in Lang.entries) {
            yaml.getString(key.name)?.let { messages[key] = it }
        }

        plugin.logger.info("Language loaded: $lang (${messages.size}/${Lang.entries.size} keys found)")
    }

    // ── Resolution ────────────────────────────────────────────────────────────

    /**
     * Resolve [key] to a [Component] using the loaded language file,
     * falling back to [Lang.default] if the key is absent.
     */
    fun resolve(key: Lang): Component {
        val raw = messages[key] ?: key.default
        return ColorUtils.parse(raw)
    }

    /**
     * Resolve [key] with one or more [TagResolver]s for inline placeholder
     * substitution:
     * ```kotlin
     * registry.resolve(Lang.BUY_SUCCESS,
     *     Placeholder.parsed("qty",   "5"),
     *     Placeholder.parsed("item",  "Diamond"),
     *     Placeholder.parsed("price", "$25.00"))
     * ```
     */
    fun resolve(key: Lang, vararg tags: TagResolver): Component {
        val raw = messages[key] ?: key.default
        return ColorUtils.parse(raw, *tags)
    }

    /**
     * Return the raw MiniMessage string for [key] without deserialising it.
     * Useful when you need to store or forward the template string.
     */
    fun rawString(key: Lang): String = messages[key] ?: key.default
}
