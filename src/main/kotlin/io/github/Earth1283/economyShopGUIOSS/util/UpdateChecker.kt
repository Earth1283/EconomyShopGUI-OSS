package io.github.Earth1283.economyShopGUIOSS.util

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import io.github.Earth1283.economyShopGUIOSS.lang.Lang
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * Checks for a newer version on the Spigot resource page and notifies
 * operators on join.
 *
 * The HTTP check is done once asynchronously on startup.  The result is
 * cached; no repeated network calls are made per join.
 */
class UpdateChecker(private val plugin: EconomyShopGUIOSS) : Listener {

    private var latestVersion: String? = null
    private val currentVersion: String = plugin.pluginMeta.version

    // Spigot resource ID — placeholder until published
    private val resourceId = 0

    fun checkAsync() {
        if (!plugin.configManager.config.updateChecking) return
        SchedulerUtils.runAsync(plugin) {
            try {
                val client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build()
                val request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.spigotmc.org/legacy/update.php?resource=$resourceId"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build()
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                val remote = response.body().trim()
                if (remote != currentVersion) {
                    latestVersion = remote
                    plugin.logger.info("Update available: $remote (current: $currentVersion)")
                }
            } catch (_: Exception) {
                // Network failure — silently skip update check
            }
        }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val latest = latestVersion ?: return
        if (!player.hasPermission("economyshopgui.admin")) return

        SchedulerUtils.runLater(plugin, 40L) {
            player.sendMessage(Lang.UPDATE_AVAILABLE.resolve(plugin.langRegistry,
                Placeholder.parsed("version", latest),
                Placeholder.parsed("url", "https://www.spigotmc.org/resources/$resourceId"),
            ))
        }
    }
}
