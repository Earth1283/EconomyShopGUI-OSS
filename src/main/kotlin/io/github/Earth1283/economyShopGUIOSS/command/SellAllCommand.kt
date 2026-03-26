package io.github.Earth1283.economyShopGUIOSS.command

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import io.github.Earth1283.economyShopGUIOSS.lang.Lang
import io.github.Earth1283.economyShopGUIOSS.model.ShopSection
import io.github.Earth1283.economyShopGUIOSS.transaction.TransactionResult
import io.github.Earth1283.economyShopGUIOSS.transaction.TransactionType
import io.github.Earth1283.economyShopGUIOSS.util.ItemUtils
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

/**
 * `/sellall [inventory|item|hand] [quantity]`
 *
 * Sells items from the player's inventory in bulk.
 *
 * - No args / `inventory`: sell every sellable item in the whole inventory.
 * - `hand`: sell only the item in the main hand.
 * - `item <id>`: sell a specific shop item by section.item id.
 */
class SellAllCommand(private val plugin: EconomyShopGUIOSS) : TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(Lang.PLAYER_ONLY.resolve(plugin.langRegistry))
            return true
        }
        if (plugin.configManager.config.disabledWorldsSellAll.contains(sender.world.name)) {
            sender.sendMessage(Lang.SELLALL_WORLD_DISABLED.resolve(plugin.langRegistry))
            return true
        }

        val mode = args.getOrNull(0)?.lowercase() ?: "inventory"

        when (mode) {
            "inventory" -> sellInventory(sender)
            "hand"      -> sellHand(sender)
            else -> sender.sendMessage(Lang.SELLALL_INVALID_MODE.resolve(plugin.langRegistry))
        }
        return true
    }

    // ── Sell all sellable items in the inventory ───────────────────────────────

    private fun sellInventory(player: Player) {
        var totalEarned = 0.0
        var sold = 0

        for (section in plugin.shopManager.allSections()) {
            for (item in section.allItems) {
                if (item.sellPrice == null) continue
                val held = ItemUtils.countItems(player, item.itemStack, item.matchMeta)
                if (held <= 0) continue

                val qty = if (item.maxSellQty > 0) minOf(held, item.maxSellQty) else held
                val result = plugin.transactionProcessor.sell(
                    player, item, section, qty, TransactionType.SellAll)
                if (result is TransactionResult.Success) {
                    totalEarned += result.price
                    sold += qty
                }
            }
        }

        if (sold == 0) {
            player.sendMessage(Lang.SELLALL_NOTHING_TO_SELL.resolve(plugin.langRegistry))
        } else {
            player.sendMessage(Lang.SELL_ALL_SUCCESS.resolve(plugin.langRegistry,
                Placeholder.parsed("price", plugin.priceFormatter.format(totalEarned))))
        }
    }

    // ── Sell the item in the main hand ────────────────────────────────────────

    private fun sellHand(player: Player) {
        val handStack = player.inventory.itemInMainHand
        if (handStack.type.isAir) {
            player.sendMessage(Lang.NO_ITEMS_TO_SELL.resolve(plugin.langRegistry))
            return
        }

        var totalEarned = 0.0
        var sold = false

        for (section in plugin.shopManager.allSections()) {
            for (item in section.allItems) {
                if (item.sellPrice == null) continue
                if (item.itemStack.type != handStack.type) continue
                val qty = handStack.amount
                val result = plugin.transactionProcessor.sell(
                    player, item, section, qty, TransactionType.SellAll)
                if (result is TransactionResult.Success) {
                    totalEarned += result.price
                    sold = true
                    break
                }
            }
            if (sold) break
        }

        if (!sold) {
            player.sendMessage(Lang.SELLALL_NOTHING_TO_SELL.resolve(plugin.langRegistry))
        } else {
            player.sendMessage(Lang.SELL_ALL_SUCCESS.resolve(plugin.langRegistry,
                Placeholder.parsed("price", plugin.priceFormatter.format(totalEarned))))
        }
    }

    override fun onTabComplete(
        sender: CommandSender, command: Command, alias: String, args: Array<String>
    ): List<String> {
        if (args.size == 1) return listOf("inventory", "hand").filter {
            it.startsWith(args[0], ignoreCase = true)
        }
        return emptyList()
    }
}
