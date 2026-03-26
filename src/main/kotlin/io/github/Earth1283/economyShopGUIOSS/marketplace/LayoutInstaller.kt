package io.github.Earth1283.economyShopGUIOSS.marketplace

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import io.github.Earth1283.economyShopGUIOSS.lang.Lang
import io.github.Earth1283.economyShopGUIOSS.util.SchedulerUtils
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.CommandSender

/**
 * Downloads a layout from the marketplace and installs it on disk,
 * then reloads the shop.
 *
 * All network I/O happens on a background thread; the shop reload and
 * feedback messages are dispatched back to the main thread.
 */
class LayoutInstaller(private val plugin: EconomyShopGUIOSS) {

    private val client = MarketplaceClient(plugin)

    /**
     * Asynchronously fetch and install the layout identified by [code].
     *
     * [sender] receives progress and result messages.
     */
    fun installAsync(sender: CommandSender, code: String) {
        sender.sendMessage(Lang.MARKETPLACE_CONNECTING.resolve(plugin.langRegistry))

        SchedulerUtils.runAsync(plugin) {
            val result = client.download(code)

            SchedulerUtils.runSync(plugin) {
                when (result) {
                    is MarketplaceResult.Success -> {
                        install(result.value)
                        // Increment download counter fire-and-forget
                        SchedulerUtils.runAsync(plugin) { client.recordDownload(code) }
                        sender.sendMessage(Lang.EDITOR_LAYOUT_INSTALLED.resolve(plugin.langRegistry,
                            Placeholder.parsed("layout", result.value.meta.code)))
                        // Reload shop so the new section appears immediately
                        plugin.configManager.load()
                        plugin.shopRepository.load()
                    }
                    is MarketplaceResult.Failure.NotFound ->
                        sender.sendMessage(Lang.EDITOR_LAYOUT_NOT_FOUND.resolve(plugin.langRegistry,
                            Placeholder.parsed("layout", code)))
                    is MarketplaceResult.Failure.RateLimited ->
                        sender.sendMessage(Lang.MARKETPLACE_ERROR.resolve(plugin.langRegistry,
                            Placeholder.parsed("message", "Rate limited — try again in ${result.retryAfterSeconds}s")))
                    else ->
                        sender.sendMessage(Lang.EDITOR_LAYOUT_FETCH_ERROR.resolve(plugin.langRegistry))
                }
            }
        }
    }

    // ── Disk write ────────────────────────────────────────────────────────────

    private fun install(download: LayoutDownload) {
        val code = download.meta.code

        val sectionFile = plugin.configManager.sectionsDir.resolve("$code.yml")
        val shopFile    = plugin.configManager.shopsDir.resolve("$code.yml")

        sectionFile.writeText(download.sectionYaml, Charsets.UTF_8)
        shopFile.writeText(download.shopYaml,    Charsets.UTF_8)

        plugin.logger.info("Layout '$code' installed: ${download.meta.name}")
    }
}
