package io.github.Earth1283.economyShopGUIOSS.stands

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import org.bukkit.Location
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory registry for [ShopStand] instances with persistence via
 * [ShopStandStorage].
 *
 * All mutations call [persist] immediately so the data file is always
 * up-to-date — no shutdown hook required.
 */
class ShopStandManager(private val plugin: EconomyShopGUIOSS) {

    private val storage = ShopStandStorage(plugin)
    private val stands  = ConcurrentHashMap<String, ShopStand>()  // id → stand

    // A separate spatial index mapping block coords to stand IDs for O(1)
    // click lookup.  Key format: "world:x:y:z"
    private val locationIndex = ConcurrentHashMap<String, String>()

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    fun load() {
        stands.clear()
        locationIndex.clear()
        storage.loadAll().forEach { stand ->
            stands[stand.id] = stand
            locationIndex[locationKey(stand.world, stand.x, stand.y, stand.z)] = stand.id
        }
        plugin.logger.info("Loaded ${stands.size} shop stand(s).")
    }

    // ── Mutation ──────────────────────────────────────────────────────────────

    fun place(sectionId: String, itemId: String?, location: Location): ShopStand {
        val stand = ShopStand(
            id        = UUID.randomUUID().toString(),
            sectionId = sectionId,
            itemId    = itemId,
            world     = location.world.name,
            x         = location.blockX,
            y         = location.blockY,
            z         = location.blockZ,
        )
        stands[stand.id] = stand
        locationIndex[locationKey(stand)] = stand.id
        persist()
        return stand
    }

    fun remove(id: String): Boolean {
        val stand = stands.remove(id) ?: return false
        locationIndex.remove(locationKey(stand))
        persist()
        return true
    }

    fun removeAt(location: Location): Boolean {
        val key = locationKey(location.world.name, location.blockX, location.blockY, location.blockZ)
        val id  = locationIndex[key] ?: return false
        return remove(id)
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    fun getAt(location: Location): ShopStand? {
        val key = locationKey(location.world.name, location.blockX, location.blockY, location.blockZ)
        val id  = locationIndex[key] ?: return null
        return stands[id]
    }

    fun getById(id: String): ShopStand? = stands[id]

    fun all(): List<ShopStand> = stands.values.toList()

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun locationKey(stand: ShopStand): String =
        locationKey(stand.world, stand.x, stand.y, stand.z)

    private fun locationKey(world: String, x: Int, y: Int, z: Int): String =
        "$world:$x:$y:$z"

    private fun persist() {
        storage.saveAll(stands.values)
    }
}
