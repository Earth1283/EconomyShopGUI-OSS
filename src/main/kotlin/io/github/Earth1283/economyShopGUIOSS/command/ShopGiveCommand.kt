package io.github.Earth1283.economyShopGUIOSS.command

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import io.github.Earth1283.economyShopGUIOSS.lang.Lang
import io.github.Earth1283.economyShopGUIOSS.util.ItemUtils
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

/**
 * `/shopgive <section.item> <player> [qty]`
 *
 * Gives a shop item directly to a player's inventory.  Admin-only command.
 */
class ShopGiveCommand(private val plugin: EconomyShopGUIOSS) : TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (!sender.hasPermission("economyshopgui.admin")) {
            sender.sendMessage(Lang.NO_PERMISSION.resolve(plugin.langRegistry))
            return true
        }
        if (args.size < 2) {
            sender.sendMessage(Lang.SHOPGIVE_USAGE.resolve(plugin.langRegistry))
            return true
        }

        val itemRef    = args[0]        // format: "sectionId.itemId"
        val playerName = args[1]
        val qty        = args.getOrNull(2)?.toIntOrNull()?.coerceAtLeast(1) ?: 1

        val parts = itemRef.split(".", limit = 2)
        if (parts.size != 2) {
            sender.sendMessage(Lang.SHOPGIVE_INVALID_ITEM.resolve(plugin.langRegistry,
                Placeholder.parsed("item", itemRef)))
            return true
        }
        val (sectionId, itemId) = parts
        val item = plugin.shopManager.findItem(sectionId, itemId)
        if (item == null) {
            sender.sendMessage(Lang.SHOPGIVE_INVALID_ITEM.resolve(plugin.langRegistry,
                Placeholder.parsed("item", itemRef)))
            return true
        }

        val target = Bukkit.getPlayer(playerName)
        if (target == null) {
            sender.sendMessage(Lang.PLAYER_NOT_FOUND.resolve(plugin.langRegistry,
                Placeholder.parsed("player", playerName)))
            return true
        }

        val overflow = ItemUtils.giveItems(target, item.itemStack, qty)
        if (overflow > 0) {
            sender.sendMessage(Lang.SHOPGIVE_PLAYER_INVENTORY_FULL.resolve(plugin.langRegistry,
                Placeholder.parsed("player", target.name)))
        } else {
            sender.sendMessage(Lang.SHOPGIVE_SUCCESS.resolve(plugin.langRegistry,
                Placeholder.parsed("qty",    qty.toString()),
                Placeholder.parsed("item",   itemRef),
                Placeholder.parsed("player", target.name),
            ))
            target.sendMessage(Lang.SHOPGIVE_RECEIVED.resolve(plugin.langRegistry,
                Placeholder.parsed("qty",    qty.toString()),
                Placeholder.parsed("item",   itemRef),
                Placeholder.parsed("sender", sender.name),
            ))
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender, command: Command, alias: String, args: Array<String>
    ): List<String> {
        if (!sender.hasPermission("economyshopgui.admin")) return emptyList()
        return when (args.size) {
            1 -> plugin.shopManager.allSections().flatMap { section ->
                section.allItems.map { "${section.id}.${it.id}" }
            }.filter { it.startsWith(args[0], ignoreCase = true) }
            2 -> Bukkit.getOnlinePlayers().map { it.name }
                .filter { it.startsWith(args[1], ignoreCase = true) }
            3 -> listOf("1", "16", "32", "64")
            else -> emptyList()
        }
    }
}
