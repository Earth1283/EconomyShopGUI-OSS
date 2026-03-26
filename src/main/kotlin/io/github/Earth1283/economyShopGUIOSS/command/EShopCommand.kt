package io.github.Earth1283.economyShopGUIOSS.command

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import io.github.Earth1283.economyShopGUIOSS.lang.Lang
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

/**
 * `/eshop <subcommand> [args…]` — in-game shop editor and admin utility.
 *
 * Subcommands:
 * - `additem`       — add the held item to a section/page/slot
 * - `edititem`      — change buy/sell price or other properties of an existing item
 * - `delitem`       — remove an item from a section
 * - `addsection`    — create a new section YAML
 * - `delsection`    — delete a section
 * - `setslot`       — change the main-menu slot of a section
 * - `reload`        — alias for `/sreload`
 * - `log`           — view the last N transaction records
 * - `shopstands`    — shop-stand sub-menu
 * - `help`          — list all subcommands
 */
class EShopCommand(private val plugin: EconomyShopGUIOSS) : TabExecutor {

    private val subcommands = listOf(
        "additem", "edititem", "delitem",
        "addsection", "delsection", "setslot",
        "reload", "log", "shopstands", "help",
    )

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (!sender.hasPermission("economyshopgui.admin")) {
            sender.sendMessage(Lang.NO_PERMISSION.resolve(plugin.langRegistry))
            return true
        }

        val sub = args.getOrNull(0)?.lowercase() ?: "help"
        val rest = args.drop(1).toTypedArray()

        return when (sub) {
            "additem"    -> handleAddItem(sender, rest)
            "edititem"   -> handleEditItem(sender, rest)
            "delitem"    -> handleDelItem(sender, rest)
            "addsection" -> handleAddSection(sender, rest)
            "delsection" -> handleDelSection(sender, rest)
            "setslot"    -> handleSetSlot(sender, rest)
            "reload"     -> { SReloadCommand(plugin).onCommand(sender, command, label, rest); true }
            "log"        -> handleLog(sender, rest)
            "shopstands" -> handleShopStands(sender, rest)
            "help"       -> { sendHelp(sender); true }
            else -> {
                sender.sendMessage(Lang.EDITOR_SUBCOMMAND_UNKNOWN.resolve(plugin.langRegistry,
                    Placeholder.parsed("sub", sub)))
                true
            }
        }
    }

    // ── Subcommand handlers ───────────────────────────────────────────────────

    private fun handleAddItem(sender: CommandSender, args: Array<String>): Boolean {
        if (sender !is Player) { sender.sendMessage(Lang.PLAYER_ONLY.resolve(plugin.langRegistry)); return true }
        if (args.size < 3) {
            sender.sendMessage(Lang.EDITOR_USAGE.resolve(plugin.langRegistry))
            return true
        }
        val sectionId = args[0]
        val page      = args[1].toIntOrNull() ?: 1
        val slot      = args[2].toIntOrNull() ?: 0
        val buyPrice  = args.getOrNull(3)?.toDoubleOrNull()
        val sellPrice = args.getOrNull(4)?.toDoubleOrNull()

        val heldItem = sender.inventory.itemInMainHand
        if (heldItem.type.isAir) {
            sender.sendMessage(Lang.EDITOR_ITEM_HAND_EMPTY.resolve(plugin.langRegistry))
            return true
        }

        val section = plugin.shopManager.getSection(sectionId)
        if (section == null) {
            sender.sendMessage(Lang.EDITOR_SECTION_NOT_FOUND.resolve(plugin.langRegistry,
                Placeholder.parsed("section", sectionId)))
            return true
        }

        // Write to shops/<sectionId>.yml
        val shopsDir = plugin.configManager.shopsDir
        val shopFile = shopsDir.resolve("$sectionId.yml")
        val yaml = if (shopFile.exists())
            org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(shopFile)
        else
            org.bukkit.configuration.file.YamlConfiguration()

        val itemId   = "${heldItem.type.name.lowercase()}_${slot}"
        val path     = "pages.$page.items.$itemId"
        yaml.set("$path.material", heldItem.type.name)
        yaml.set("$path.slot", slot)
        buyPrice?.let  { yaml.set("$path.buy",  it) }
        sellPrice?.let { yaml.set("$path.sell", it) }
        yaml.save(shopFile)

        plugin.shopRepository.load()

        sender.sendMessage(Lang.EDITOR_ITEM_ADDED.resolve(plugin.langRegistry,
            Placeholder.parsed("item",    itemId),
            Placeholder.parsed("section", sectionId)))
        return true
    }

    private fun handleEditItem(sender: CommandSender, args: Array<String>): Boolean {
        // /eshop edititem <section> <itemId> <field> <value>
        if (args.size < 4) { sender.sendMessage(Lang.EDITOR_USAGE.resolve(plugin.langRegistry)); return true }
        val sectionId = args[0]
        val itemId    = args[1]
        val field     = args[2].lowercase()
        val value     = args[3]

        val shopFile = plugin.configManager.shopsDir.resolve("$sectionId.yml")
        if (!shopFile.exists()) {
            sender.sendMessage(Lang.EDITOR_ITEM_NOT_FOUND.resolve(plugin.langRegistry,
                Placeholder.parsed("item", itemId), Placeholder.parsed("section", sectionId)))
            return true
        }

        val yaml = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(shopFile)
        val section = plugin.shopManager.getSection(sectionId)
        val item = section?.findItem(itemId)
        if (item == null) {
            sender.sendMessage(Lang.EDITOR_ITEM_NOT_FOUND.resolve(plugin.langRegistry,
                Placeholder.parsed("item", itemId), Placeholder.parsed("section", sectionId)))
            return true
        }

        val basePath = "pages.${item.page}.items.$itemId"
        when (field) {
            "buy"  -> value.toDoubleOrNull()?.let { yaml.set("$basePath.buy",  it) }
                ?: run { sender.sendMessage(Lang.EDITOR_ITEM_INVALID_PRICE.resolve(plugin.langRegistry,
                    Placeholder.parsed("value", value))); return true }
            "sell" -> value.toDoubleOrNull()?.let { yaml.set("$basePath.sell", it) }
                ?: run { sender.sendMessage(Lang.EDITOR_ITEM_INVALID_PRICE.resolve(plugin.langRegistry,
                    Placeholder.parsed("value", value))); return true }
            "slot" -> value.toIntOrNull()?.let { yaml.set("$basePath.slot", it) }
                ?: run { sender.sendMessage(Lang.EDITOR_USAGE.resolve(plugin.langRegistry)); return true }
            else -> { sender.sendMessage(Lang.EDITOR_USAGE.resolve(plugin.langRegistry)); return true }
        }
        yaml.save(shopFile)
        plugin.shopRepository.load()

        sender.sendMessage(Lang.EDITOR_ITEM_EDITED.resolve(plugin.langRegistry,
            Placeholder.parsed("item", itemId), Placeholder.parsed("section", sectionId)))
        return true
    }

    private fun handleDelItem(sender: CommandSender, args: Array<String>): Boolean {
        if (args.size < 2) { sender.sendMessage(Lang.EDITOR_USAGE.resolve(plugin.langRegistry)); return true }
        val sectionId = args[0]
        val itemId    = args[1]

        val shopFile = plugin.configManager.shopsDir.resolve("$sectionId.yml")
        val section  = plugin.shopManager.getSection(sectionId)
        val item     = section?.findItem(itemId)

        if (!shopFile.exists() || item == null) {
            sender.sendMessage(Lang.EDITOR_ITEM_NOT_FOUND.resolve(plugin.langRegistry,
                Placeholder.parsed("item", itemId), Placeholder.parsed("section", sectionId)))
            return true
        }

        val yaml = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(shopFile)
        yaml.set("pages.${item.page}.items.$itemId", null)
        yaml.save(shopFile)
        plugin.shopRepository.load()

        sender.sendMessage(Lang.EDITOR_ITEM_DELETED.resolve(plugin.langRegistry,
            Placeholder.parsed("item", itemId), Placeholder.parsed("section", sectionId)))
        return true
    }

    private fun handleAddSection(sender: CommandSender, args: Array<String>): Boolean {
        if (args.isEmpty()) { sender.sendMessage(Lang.EDITOR_USAGE.resolve(plugin.langRegistry)); return true }
        val sectionId = args[0]
        val slot      = args.getOrNull(1)?.toIntOrNull() ?: 0

        if (plugin.shopManager.sectionExists(sectionId)) {
            sender.sendMessage(Lang.EDITOR_SECTION_ALREADY_EXISTS.resolve(plugin.langRegistry,
                Placeholder.parsed("section", sectionId)))
            return true
        }

        val secFile = plugin.configManager.sectionsDir.resolve("$sectionId.yml")
        val yaml    = org.bukkit.configuration.file.YamlConfiguration()
        yaml.set("enable", true)
        yaml.set("title", sectionId)
        yaml.set("slot", slot)
        yaml.set("display-item", "CHEST")
        yaml.save(secFile)
        plugin.shopRepository.load()

        sender.sendMessage(Lang.EDITOR_SECTION_ADDED.resolve(plugin.langRegistry,
            Placeholder.parsed("section", sectionId)))
        return true
    }

    private fun handleDelSection(sender: CommandSender, args: Array<String>): Boolean {
        if (args.isEmpty()) { sender.sendMessage(Lang.EDITOR_USAGE.resolve(plugin.langRegistry)); return true }
        val sectionId = args[0]

        val secFile  = plugin.configManager.sectionsDir.resolve("$sectionId.yml")
        val shopFile = plugin.configManager.shopsDir.resolve("$sectionId.yml")

        if (!secFile.exists()) {
            sender.sendMessage(Lang.EDITOR_SECTION_NOT_FOUND.resolve(plugin.langRegistry,
                Placeholder.parsed("section", sectionId)))
            return true
        }

        secFile.delete()
        if (shopFile.exists()) shopFile.delete()
        plugin.shopRepository.load()

        sender.sendMessage(Lang.EDITOR_SECTION_DELETED.resolve(plugin.langRegistry,
            Placeholder.parsed("section", sectionId)))
        return true
    }

    private fun handleSetSlot(sender: CommandSender, args: Array<String>): Boolean {
        if (args.size < 2) { sender.sendMessage(Lang.EDITOR_USAGE.resolve(plugin.langRegistry)); return true }
        val sectionId = args[0]
        val slot      = args[1].toIntOrNull()

        if (slot == null) { sender.sendMessage(Lang.EDITOR_USAGE.resolve(plugin.langRegistry)); return true }

        val secFile = plugin.configManager.sectionsDir.resolve("$sectionId.yml")
        if (!secFile.exists()) {
            sender.sendMessage(Lang.EDITOR_SECTION_NOT_FOUND.resolve(plugin.langRegistry,
                Placeholder.parsed("section", sectionId)))
            return true
        }

        val yaml = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(secFile)
        yaml.set("slot", slot)
        yaml.save(secFile)
        plugin.shopRepository.load()

        sender.sendMessage(Lang.EDITOR_SECTION_EDITED.resolve(plugin.langRegistry,
            Placeholder.parsed("section", sectionId)))
        return true
    }

    private fun handleLog(sender: CommandSender, args: Array<String>): Boolean {
        val limit = args.getOrNull(0)?.toIntOrNull() ?: 20
        val records = plugin.transactionProcessor.logger.queryAll(limit)
        if (records.isEmpty()) {
            sender.sendMessage(Lang.TRANSACTION_LOG_EMPTY.resolve(plugin.langRegistry))
            return true
        }
        records.forEach { r ->
            sender.sendMessage(io.github.Earth1283.economyShopGUIOSS.util.ColorUtils.parse(
                "<gray>${java.time.Instant.ofEpochMilli(r.timestamp)} " +
                "<white>${r.playerName}</white> " +
                "<yellow>${r.type}</yellow> " +
                "<white>${r.quantity}x ${r.sectionId}.${r.itemId}</white> " +
                "<gold>${r.price}</gold>"
            ))
        }
        return true
    }

    private fun handleShopStands(sender: CommandSender, args: Array<String>): Boolean {
        // Placeholder — implemented in Phase 9
        sender.sendMessage(io.github.Earth1283.economyShopGUIOSS.util.ColorUtils.parse(
            "<yellow>Shop stands management is coming in a future update."))
        return true
    }

    private fun sendHelp(sender: CommandSender) {
        val lines = listOf(
            "<gold>━━━ EconomyShopGUI-OSS Admin Commands ━━━",
            "<yellow>/eshop additem <section> <page> <slot> [buy] [sell]",
            "<yellow>/eshop edititem <section> <item> <buy|sell|slot> <value>",
            "<yellow>/eshop delitem <section> <item>",
            "<yellow>/eshop addsection <id> [slot]",
            "<yellow>/eshop delsection <id>",
            "<yellow>/eshop setslot <section> <slot>",
            "<yellow>/eshop reload",
            "<yellow>/eshop log [limit]",
            "<yellow>/eshop shopstands",
        )
        lines.forEach { sender.sendMessage(io.github.Earth1283.economyShopGUIOSS.util.ColorUtils.parse(it)) }
    }

    // ── Tab completion ────────────────────────────────────────────────────────

    override fun onTabComplete(
        sender: CommandSender, command: Command, alias: String, args: Array<String>
    ): List<String> {
        if (!sender.hasPermission("economyshopgui.admin")) return emptyList()
        return when (args.size) {
            1 -> subcommands.filter { it.startsWith(args[0], ignoreCase = true) }
            2 -> when (args[0].lowercase()) {
                "edititem", "delitem", "delsection", "setslot" ->
                    plugin.shopManager.allSections().map { it.id }
                        .filter { it.startsWith(args[1], ignoreCase = true) }
                "additem", "addsection" ->
                    plugin.shopManager.allSections().map { it.id }
                        .filter { it.startsWith(args[1], ignoreCase = true) }
                else -> emptyList()
            }
            else -> emptyList()
        }
    }
}
