package io.github.Earth1283.economyShopGUIOSS.marketplace

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import io.github.Earth1283.economyShopGUIOSS.lang.Lang
import io.github.Earth1283.economyShopGUIOSS.util.SchedulerUtils
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.CommandSender

/**
 * Reads a section's YAML files from disk and uploads them to the marketplace.
 *
 * Requires a Bearer token stored in `config.yml → marketplace.token`.
 * All network I/O is off-thread; feedback returns to the main thread.
 */
class LayoutUploader(private val plugin: EconomyShopGUIOSS) {

    private val client = MarketplaceClient(plugin)

    /**
     * Upload the section identified by [sectionId] to the marketplace.
     *
     * @param sender      Receives progress and result messages.
     * @param sectionId   Section ID (file name without `.yml`).
     * @param name        Human-readable marketplace listing name.
     * @param description Optional description for the listing.
     * @param tags        Optional tag list.
     */
    fun uploadAsync(
        sender: CommandSender,
        sectionId: String,
        name: String,
        description: String = "",
        tags: List<String> = emptyList(),
    ) {
        val token = plugin.configManager.config.marketplaceToken
        if (token.isBlank()) {
            sender.sendMessage(Lang.MARKETPLACE_ERROR.resolve(plugin.langRegistry,
                Placeholder.parsed("message", "No marketplace token configured (marketplace.token in config.yml)")))
            return
        }

        val sectionFile = plugin.configManager.sectionsDir.resolve("$sectionId.yml")
        val shopFile    = plugin.configManager.shopsDir.resolve("$sectionId.yml")

        if (!sectionFile.exists()) {
            sender.sendMessage(Lang.EDITOR_SECTION_NOT_FOUND.resolve(plugin.langRegistry,
                Placeholder.parsed("section", sectionId)))
            return
        }

        val sectionYaml = sectionFile.readText(Charsets.UTF_8)
        val shopYaml    = if (shopFile.exists()) shopFile.readText(Charsets.UTF_8) else ""

        sender.sendMessage(Lang.MARKETPLACE_CONNECTING.resolve(plugin.langRegistry))

        SchedulerUtils.runAsync(plugin) {
            val result = client.upload(token, name, description, tags, sectionYaml, shopYaml)
            SchedulerUtils.runSync(plugin) {
                when (result) {
                    is MarketplaceResult.Success ->
                        sender.sendMessage(Lang.EDITOR_LAYOUT_UPLOADED.resolve(plugin.langRegistry,
                            Placeholder.parsed("code", result.value.code)))
                    is MarketplaceResult.Failure.Unauthorized ->
                        sender.sendMessage(Lang.MARKETPLACE_ERROR.resolve(plugin.langRegistry,
                            Placeholder.parsed("message", "Invalid marketplace token")))
                    is MarketplaceResult.Failure.ValidationError ->
                        sender.sendMessage(Lang.MARKETPLACE_ERROR.resolve(plugin.langRegistry,
                            Placeholder.parsed("message", "Validation failed: ${result.body}")))
                    else ->
                        sender.sendMessage(Lang.MARKETPLACE_ERROR.resolve(plugin.langRegistry,
                            Placeholder.parsed("message", result.toString())))
                }
            }
        }
    }

    /**
     * Update an existing marketplace listing identified by [code].
     */
    fun updateAsync(
        sender: CommandSender,
        sectionId: String,
        code: String,
        name: String? = null,
        description: String? = null,
        tags: List<String>? = null,
    ) {
        val token = plugin.configManager.config.marketplaceToken
        if (token.isBlank()) {
            sender.sendMessage(Lang.MARKETPLACE_ERROR.resolve(plugin.langRegistry,
                Placeholder.parsed("message", "No marketplace token configured")))
            return
        }

        val sectionFile = plugin.configManager.sectionsDir.resolve("$sectionId.yml")
        val shopFile    = plugin.configManager.shopsDir.resolve("$sectionId.yml")

        val sectionYaml = if (sectionFile.exists()) sectionFile.readText() else null
        val shopYaml    = if (shopFile.exists())    shopFile.readText()    else null

        SchedulerUtils.runAsync(plugin) {
            val result = client.update(token, code, name, description, tags, sectionYaml, shopYaml)
            SchedulerUtils.runSync(plugin) {
                when (result) {
                    is MarketplaceResult.Success ->
                        sender.sendMessage(Lang.EDITOR_LAYOUT_UPDATED.resolve(plugin.langRegistry,
                            Placeholder.parsed("layout", code)))
                    is MarketplaceResult.Failure.NotFound ->
                        sender.sendMessage(Lang.EDITOR_LAYOUT_NOT_FOUND.resolve(plugin.langRegistry,
                            Placeholder.parsed("layout", code)))
                    else ->
                        sender.sendMessage(Lang.MARKETPLACE_ERROR.resolve(plugin.langRegistry,
                            Placeholder.parsed("message", result.toString())))
                }
            }
        }
    }
}
