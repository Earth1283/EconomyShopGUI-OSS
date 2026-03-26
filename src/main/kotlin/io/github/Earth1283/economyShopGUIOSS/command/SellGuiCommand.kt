package io.github.Earth1283.economyShopGUIOSS.command

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import io.github.Earth1283.economyShopGUIOSS.gui.screens.SellGuiScreen
import io.github.Earth1283.economyShopGUIOSS.lang.Lang
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

/** `/sellgui` — opens the drag-and-drop sell GUI. */
class SellGuiCommand(private val plugin: EconomyShopGUIOSS) : TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(Lang.PLAYER_ONLY.resolve(plugin.langRegistry))
            return true
        }
        if (plugin.configManager.config.disabledWorldsSellGui.contains(sender.world.name)) {
            sender.sendMessage(Lang.SELLGUI_WORLD_DISABLED.resolve(plugin.langRegistry))
            return true
        }
        SellGuiScreen(plugin).open(sender)
        return true
    }

    override fun onTabComplete(
        sender: CommandSender, command: Command, alias: String, args: Array<String>
    ): List<String> = emptyList()
}
