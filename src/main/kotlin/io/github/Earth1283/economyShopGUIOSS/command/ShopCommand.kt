package io.github.Earth1283.economyShopGUIOSS.command

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import io.github.Earth1283.economyShopGUIOSS.gui.screens.MainMenuScreen
import io.github.Earth1283.economyShopGUIOSS.gui.screens.ShopScreen
import io.github.Earth1283.economyShopGUIOSS.lang.Lang
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

/**
 * `/shop [section] [page]`
 *
 * Opens the main menu (no args), a specific section, or a specific page
 * within a section.
 */
class ShopCommand(private val plugin: EconomyShopGUIOSS) : TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(Lang.PLAYER_ONLY.resolve(plugin.langRegistry))
            return true
        }

        if (plugin.configManager.config.disabledWorldsShop.contains(sender.world.name)) {
            sender.sendMessage(Lang.SHOP_WORLD_DISABLED.resolve(plugin.langRegistry))
            return true
        }

        when (args.size) {
            0 -> MainMenuScreen(plugin).open(sender)
            else -> {
                val sectionId = args[0]
                val section   = plugin.shopManager.getSection(sectionId)
                if (section == null) {
                    sender.sendMessage(Lang.SHOP_INVALID_SECTION.resolve(plugin.langRegistry,
                        Placeholder.parsed("section", sectionId)))
                    return true
                }
                if (section.hidden) {
                    sender.sendMessage(Lang.SHOP_SECTION_HIDDEN.resolve(plugin.langRegistry))
                    return true
                }
                val page = args.getOrNull(1)?.toIntOrNull() ?: 1
                if (section.getPage(page) == null && section.pages.isNotEmpty()) {
                    sender.sendMessage(Lang.SHOP_INVALID_PAGE.resolve(plugin.langRegistry,
                        Placeholder.parsed("page",    page.toString()),
                        Placeholder.parsed("section", sectionId)))
                    return true
                }
                ShopScreen(plugin, section, page).open(sender)
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender, command: Command, alias: String, args: Array<String>
    ): List<String> {
        if (sender !is Player) return emptyList()
        return when (args.size) {
            1 -> plugin.shopManager.allSections()
                    .filter { !it.hidden }
                    .map { it.id }
                    .filter { it.startsWith(args[0], ignoreCase = true) }
            2 -> {
                val section = plugin.shopManager.getSection(args[0]) ?: return emptyList()
                (1..section.pages.size).map { it.toString() }
                    .filter { it.startsWith(args[1]) }
            }
            else -> emptyList()
        }
    }
}
