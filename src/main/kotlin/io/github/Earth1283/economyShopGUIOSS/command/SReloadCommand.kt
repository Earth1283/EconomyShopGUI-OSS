package io.github.Earth1283.economyShopGUIOSS.command

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import io.github.Earth1283.economyShopGUIOSS.lang.Lang
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

/**
 * `/sreload` — reload config, lang, and shop data without restarting.
 *
 * Admin-only.  Calls the same load sequence as [onEnable] in order, so the
 * plugin state is fully refreshed.  Active GUI sessions are not forcibly closed;
 * players will see the old data until they reopen a screen.
 */
class SReloadCommand(private val plugin: EconomyShopGUIOSS) : TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (!sender.hasPermission("economyshopgui.admin")) {
            sender.sendMessage(Lang.NO_PERMISSION.resolve(plugin.langRegistry))
            return true
        }
        try {
            plugin.configManager.load()
            plugin.langRegistry.load()
            plugin.priceFormatter.load()
            plugin.shopRepository.load()
            sender.sendMessage(Lang.RELOAD_SUCCESS.resolve(plugin.langRegistry))
        } catch (e: Exception) {
            plugin.logger.severe("Reload failed: ${e.message}")
            e.printStackTrace()
            sender.sendMessage(Lang.RELOAD_FAILED.resolve(plugin.langRegistry))
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender, command: Command, alias: String, args: Array<String>
    ): List<String> = emptyList()
}
