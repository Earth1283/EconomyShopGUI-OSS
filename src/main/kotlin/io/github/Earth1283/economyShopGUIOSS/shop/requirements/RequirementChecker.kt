package io.github.Earth1283.economyShopGUIOSS.shop.requirements

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import io.github.Earth1283.economyShopGUIOSS.model.Requirement
import io.github.Earth1283.economyShopGUIOSS.model.ShopItem
import io.github.Earth1283.economyShopGUIOSS.transaction.TransactionResult
import org.bukkit.entity.Player

/**
 * Evaluates all [Requirement]s on a [ShopItem] against a [Player].
 *
 * Returns `null` on success, or a [TransactionResult.Failure.RequirementNotMet]
 * describing the first failing requirement.
 */
class RequirementChecker(private val plugin: EconomyShopGUIOSS) {

    fun check(player: Player, item: ShopItem): TransactionResult.Failure? {
        for (req in item.requirements) {
            val failure = evaluate(player, req) ?: continue
            return TransactionResult.Failure.RequirementNotMet(failure)
        }
        return null
    }

    private fun evaluate(player: Player, requirement: Requirement): String? = when (requirement) {
        is Requirement.Quest     -> checkQuest(player, requirement)
        is Requirement.Region    -> checkRegion(player, requirement)
        is Requirement.TimeOfDay -> checkTime(player, requirement)
    }

    // ── Requirement implementations ───────────────────────────────────────────

    private fun checkQuest(player: Player, req: Requirement.Quest): String? {
        if (!plugin.hookManager.hasQuests) return null   // soft-dep absent → pass
        return try {
            val questsPlugin = plugin.server.pluginManager.getPlugin("Quests") ?: return null
            val api = questsPlugin::class.java.getMethod("getQuester", java.util.UUID::class.java)
                .invoke(questsPlugin, player.uniqueId) ?: return "quest:${req.questId} not started"
            val quest = questsPlugin::class.java.getMethod("getQuest", String::class.java)
                .invoke(questsPlugin, req.questId) ?: return "quest:${req.questId} unknown"
            val currentStages = api::class.java.getMethod("getCurrentStages").invoke(api)
                as? Map<*, *> ?: return null
            val completed = currentStages.keys.any { k ->
                k?.toString()?.let {
                    questsPlugin::class.java.getMethod("getQuest", String::class.java)
                        .invoke(questsPlugin, it)?.toString() == quest.toString()
                } == true
            }
            if (completed) null else "quest:${req.questId} not completed"
        } catch (_: Exception) {
            null // If reflection fails, do not block the player
        }
    }

    private fun checkRegion(player: Player, req: Requirement.Region): String? {
        if (!plugin.hookManager.hasWorldGuard) return null
        return try {
            val wg = com.sk89q.worldguard.WorldGuard.getInstance()
            val worldName = req.worldName ?: player.world.name
            val world = plugin.server.getWorld(worldName) ?: return "region:unknown world $worldName"
            val location = com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(player.location)
            val container = wg.platform.regionContainer
            val query = container.createQuery()
            val regions = query.getApplicableRegions(location)
            val inRegion = regions.any { it.id.equals(req.regionId, ignoreCase = true) }
            if (inRegion) null else "region:${req.regionId}"
        } catch (_: Exception) {
            null
        }
    }

    private fun checkTime(player: Player, req: Requirement.TimeOfDay): String? {
        val worldTime = player.world.time
        return if (req.isMet(worldTime)) null else "time:${req.fromTick}–${req.toTick}"
    }
}
