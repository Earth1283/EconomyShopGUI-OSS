package io.github.Earth1283.economyShopGUIOSS.command

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS

/**
 * Registers all plugin commands with Bukkit.
 *
 * Each command is bound to its executor and tab-completer here so the rest
 * of the plugin never touches `getCommand()` directly.
 */
class CommandRegistry(private val plugin: EconomyShopGUIOSS) {

    fun register() {
        bind("shop",     ShopCommand(plugin))
        bind("sellall",  SellAllCommand(plugin))
        bind("sellgui",  SellGuiCommand(plugin))
        bind("shopgive", ShopGiveCommand(plugin))
        bind("sreload",  SReloadCommand(plugin))
        bind("eshop",    EShopCommand(plugin))
    }

    private fun bind(name: String, executor: org.bukkit.command.TabExecutor) {
        plugin.getCommand(name)?.let { cmd ->
            cmd.setExecutor(executor)
            cmd.tabCompleter = executor
        } ?: plugin.logger.warning("Command '$name' not found in plugin.yml")
    }
}
