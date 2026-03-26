package io.github.Earth1283.economyShopGUIOSS.hook

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import org.bukkit.Bukkit

/**
 * Detects which optional (soft-depend) plugins are loaded at startup.
 *
 * All detection is lazy: each property calls [Bukkit.getPluginManager]
 * once and caches the result.  Using lazy initialisation means detection
 * only runs when first accessed, keeping plugin enable time fast.
 *
 * Consumers should check the relevant property before calling into any
 * third-party API to avoid [NoClassDefFoundError] when the plugin is absent.
 */
class HookManager(private val plugin: EconomyShopGUIOSS) {

    // ── Economy ───────────────────────────────────────────────────────────────

    val hasVault: Boolean by lazy {
        isLoaded("Vault").also { if (it) plugin.logger.info("Hook: Vault detected") }
    }

    val hasPlayerPoints: Boolean by lazy {
        isLoaded("PlayerPoints").also { if (it) plugin.logger.info("Hook: PlayerPoints detected") }
    }

    val hasGemsEconomy: Boolean by lazy {
        isLoaded("GemsEconomy").also { if (it) plugin.logger.info("Hook: GemsEconomy detected") }
    }

    // ── Regions & quests ──────────────────────────────────────────────────────

    val hasWorldGuard: Boolean by lazy {
        isLoaded("WorldGuard").also { if (it) plugin.logger.info("Hook: WorldGuard detected") }
    }

    val hasQuests: Boolean by lazy {
        isLoaded("Quests").also { if (it) plugin.logger.info("Hook: Quests detected") }
    }

    val hasQuestsC: Boolean by lazy {
        isLoaded("QuestsC").also { if (it) plugin.logger.info("Hook: QuestsC detected") }
    }

    // ── Display & integrations ────────────────────────────────────────────────

    val hasPlaceholderAPI: Boolean by lazy {
        isLoaded("PlaceholderAPI").also { if (it) plugin.logger.info("Hook: PlaceholderAPI detected") }
    }

    val hasDiscordSRV: Boolean by lazy {
        isLoaded("DiscordSRV").also { if (it) plugin.logger.info("Hook: DiscordSRV detected") }
    }

    val hasGeyser: Boolean by lazy {
        (isLoaded("Geyser-Spigot") || isLoaded("GeyserMC"))
            .also { if (it) plugin.logger.info("Hook: Geyser (Bedrock) detected") }
    }

    // ── Seasons ───────────────────────────────────────────────────────────────

    val hasRealisticSeasons: Boolean by lazy {
        isLoaded("RealisticSeasons").also { if (it) plugin.logger.info("Hook: RealisticSeasons detected") }
    }

    // ── Spawner plugins ───────────────────────────────────────────────────────

    val hasSilkSpawners: Boolean   by lazy { isLoaded("SilkSpawners") }
    val hasWildStacker: Boolean    by lazy { isLoaded("WildStacker") }
    val hasRoseStacker: Boolean    by lazy { isLoaded("RoseStacker") }
    val hasSmartSpawner: Boolean   by lazy { isLoaded("SmartSpawner") }
    val hasUltimateStacker: Boolean by lazy { isLoaded("UltimateStacker") }
    val hasMineableSpawners: Boolean by lazy { isLoaded("MineableSpawners") }
    val hasSpawnerMeta: Boolean    by lazy { isLoaded("SpawnerMeta") }
    val hasSpawnerLegacy: Boolean  by lazy { isLoaded("SpawnerLegacy") }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /**
     * Force-initialise all hook detection properties so that any "detected"
     * log messages appear at a predictable point during startup.
     */
    fun register() {
        // Economy
        hasVault; hasPlayerPoints; hasGemsEconomy

        // Regions / quests
        hasWorldGuard; hasQuests; hasQuestsC

        // Display / integrations
        hasPlaceholderAPI; hasDiscordSRV; hasGeyser; hasRealisticSeasons

        // Spawners (logged only in debug mode to avoid console spam)
        if (plugin.configManager.config.debug) {
            listOf(
                "SilkSpawners"    to hasSilkSpawners,
                "WildStacker"     to hasWildStacker,
                "RoseStacker"     to hasRoseStacker,
                "SmartSpawner"    to hasSmartSpawner,
                "UltimateStacker" to hasUltimateStacker,
                "MineableSpawners" to hasMineableSpawners,
                "SpawnerMeta"     to hasSpawnerMeta,
                "SpawnerLegacy"   to hasSpawnerLegacy,
            ).filter { it.second }.forEach { (name, _) ->
                plugin.logger.info("[Debug] Spawner hook: $name detected")
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun isLoaded(name: String): Boolean =
        Bukkit.getPluginManager().getPlugin(name)?.isEnabled == true
}
