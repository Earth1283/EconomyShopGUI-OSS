package io.github.Earth1283.economyShopGUIOSS.stands

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/**
 * Persists [ShopStand] instances to `stands.yml` inside the plugin data folder.
 *
 * YAML is used here instead of JSON because it's already on the classpath and
 * eliminates a Gson dependency.  Format:
 *
 * ```yaml
 * stands:
 *   <uuid>:
 *     section: "tools"
 *     item: "diamond_sword"    # optional
 *     world: "world"
 *     x: 100
 *     y: 64
 *     z: 200
 * ```
 */
class ShopStandStorage(private val plugin: EconomyShopGUIOSS) {

    private val file: File get() = File(plugin.dataFolder, "stands.yml")

    // ── Read ──────────────────────────────────────────────────────────────────

    fun loadAll(): List<ShopStand> {
        if (!file.exists()) return emptyList()
        val yaml    = YamlConfiguration.loadConfiguration(file)
        val section = yaml.getConfigurationSection("stands") ?: return emptyList()
        return section.getKeys(false).mapNotNull { id ->
            val s = section.getConfigurationSection(id) ?: return@mapNotNull null
            ShopStand(
                id        = id,
                sectionId = s.getString("section") ?: return@mapNotNull null,
                itemId    = s.getString("item"),
                world     = s.getString("world") ?: return@mapNotNull null,
                x         = s.getInt("x"),
                y         = s.getInt("y"),
                z         = s.getInt("z"),
            )
        }
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    fun saveAll(stands: Collection<ShopStand>) {
        val yaml = YamlConfiguration()
        stands.forEach { stand ->
            val path = "stands.${stand.id}"
            yaml.set("$path.section", stand.sectionId)
            stand.itemId?.let { yaml.set("$path.item", it) }
            yaml.set("$path.world", stand.world)
            yaml.set("$path.x", stand.x)
            yaml.set("$path.y", stand.y)
            yaml.set("$path.z", stand.z)
        }
        yaml.save(file)
    }
}
