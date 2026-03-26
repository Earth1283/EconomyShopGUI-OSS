package io.github.Earth1283.economyShopGUIOSS.config

import io.github.Earth1283.economyShopGUIOSS.economy.EconomyType
import io.github.Earth1283.economyShopGUIOSS.model.*
import io.github.Earth1283.economyShopGUIOSS.util.ColorUtils
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.io.File
import java.util.logging.Logger

/**
 * Reads `sections/<id>.yml` and `shops/<id>.yml` from the plugin data folder and
 * produces a list of fully-parsed [ShopSection] instances.
 *
 * All [Component] values are deserialized from MiniMessage at load time so
 * the GUI layer never needs to re-parse strings.
 *
 * This object is stateless; every [loadAll] call reads fresh data from disk.
 */
object ShopConfigLoader {

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Load every section found in [sectionsDir], pairing each one with its
     * corresponding shop file in [shopsDir].
     *
     * Sections whose YAML is malformed are skipped with a warning; the rest
     * continue to load normally.
     */
    fun loadAll(
        sectionsDir: File,
        shopsDir: File,
        defaultEconomy: EconomyType,
        logger: Logger,
    ): List<ShopSection> {
        val sectionFiles = sectionsDir.listFiles { f -> f.extension == "yml" }
            ?: return emptyList()

        return sectionFiles.mapNotNull { sectionFile ->
            val id = sectionFile.nameWithoutExtension
            runCatching {
                loadSection(id, sectionFile, shopsDir.resolve("$id.yml"), defaultEconomy)
            }.onFailure { e ->
                logger.warning("Failed to load section '$id': ${e.message}")
                if (logger.isLoggable(java.util.logging.Level.FINE)) e.printStackTrace()
            }.getOrNull()
        }
    }

    // ── Section loading ───────────────────────────────────────────────────────

    private fun loadSection(
        id: String,
        sectionFile: File,
        shopFile: File,
        defaultEconomy: EconomyType,
    ): ShopSection {
        val sec = YamlConfiguration.loadConfiguration(sectionFile)
        val economyType = EconomyType.fromString(sec.getString("economy") ?: defaultEconomy.toString().lowercase())

        val pages: List<ShopPage> = if (shopFile.exists()) {
            val shop = YamlConfiguration.loadConfiguration(shopFile)
            loadPages(shop, id, economyType)
        } else {
            emptyList()
        }

        return ShopSection(
            id            = id,
            enabled       = sec.getBoolean("enable", true),
            displayName   = sec.component("title"),
            slot          = sec.getInt("slot", -1),
            displayItem   = sec.parseDisplayItem("display-item"),
            economyType   = economyType,
            fillItem      = sec.parseMaterialItem("fill-item"),
            itemLayout    = sec.parseItemLayout("item-layout"),
            navBarConfig  = sec.parseNavBarConfig("nav-bar"),
            clickMapping  = sec.parseClickMapping("click-mappings"),
            pages         = pages,
            hidden        = sec.getBoolean("hidden", false),
            isSubSection  = sec.getBoolean("sub-section", false),
            priceModifiers = sec.parsePriceModifiers("price-modifiers"),
        )
    }

    // ── Page loading ──────────────────────────────────────────────────────────

    private fun loadPages(
        shopConfig: YamlConfiguration,
        sectionId: String,
        sectionEconomy: EconomyType,
    ): List<ShopPage> {
        val pagesSection = shopConfig.getConfigurationSection("pages") ?: return emptyList()

        return pagesSection.getKeys(false)
            .mapNotNull { pageKey ->
                val pageNum = pageKey.toIntOrNull() ?: return@mapNotNull null
                val pageSec = pagesSection.getConfigurationSection(pageKey) ?: return@mapNotNull null
                runCatching {
                    loadPage(pageSec, pageNum, sectionId, sectionEconomy)
                }.getOrNull()
            }
            .sortedBy { it.pageNumber }
    }

    private fun loadPage(
        pageSec: ConfigurationSection,
        pageNumber: Int,
        sectionId: String,
        sectionEconomy: EconomyType,
    ): ShopPage {
        val rows = pageSec.getInt("gui-rows", 6).coerceIn(1, 6)
        val title = pageSec.component("title")

        val itemsSection = pageSec.getConfigurationSection("items")
        val items: Map<Int, ShopItem> = if (itemsSection != null) {
            buildItemMap(itemsSection, pageNumber, sectionId, sectionEconomy)
        } else {
            emptyMap()
        }

        return ShopPage(pageNumber = pageNumber, title = title, rows = rows, items = items)
    }

    private fun buildItemMap(
        itemsSection: ConfigurationSection,
        pageNumber: Int,
        sectionId: String,
        sectionEconomy: EconomyType,
    ): Map<Int, ShopItem> {
        val result = mutableMapOf<Int, ShopItem>()
        for (itemKey in itemsSection.getKeys(false)) {
            val itemSec = itemsSection.getConfigurationSection(itemKey) ?: continue
            runCatching {
                val item = loadItem(itemSec, itemKey, pageNumber, sectionId, sectionEconomy)
                result[item.slot] = item
            }
        }
        return result
    }

    // ── Item loading ──────────────────────────────────────────────────────────

    private fun loadItem(
        sec: ConfigurationSection,
        id: String,
        page: Int,
        sectionId: String,
        sectionEconomy: EconomyType,
    ): ShopItem {
        val material = sec.parseMaterial("material") ?: Material.STONE
        val itemStack = buildItemStack(sec, material)

        val economyType = sec.getString("economy")
            ?.let { EconomyType.fromString(it) }
            ?: sectionEconomy

        return ShopItem(
            id              = id,
            sectionId       = sectionId,
            page            = page,
            slot            = sec.getInt("slot", 0),
            itemStack       = itemStack,
            displayName     = sec.component("name"),
            lore            = sec.componentList("lore"),
            buyPrice        = sec.getDoubleOrNull("buy"),
            sellPrice       = sec.getDoubleOrNull("sell"),
            economyType     = economyType,
            stackSize       = sec.getInt("stack-size", 1).coerceAtLeast(1),
            maxBuyQty       = sec.getInt("max-buy", -1),
            maxSellQty      = sec.getInt("max-sell", -1),
            minBuyQty       = sec.getInt("min-buy", 1).coerceAtLeast(1),
            minSellQty      = sec.getInt("min-sell", 1).coerceAtLeast(1),
            requirements    = sec.parseRequirements("requirements"),
            modifiers       = sec.parsePriceModifiers("price-modifiers"),
            permission      = sec.getString("permission"),
            linkedSectionId = sec.getString("section"),
            hidePricingLore = sec.getBoolean("hidePricingLore", false),
            closeOnPurchase = sec.getBoolean("close-menu", false),
            matchMeta       = sec.getBoolean("matchMeta", false),
        )
    }

    // ── ItemStack construction ────────────────────────────────────────────────

    private fun buildItemStack(sec: ConfigurationSection, material: Material): ItemStack {
        val stack = ItemStack(material)
        val meta = stack.itemMeta ?: return stack

        // Enchantments
        val enchantSec = sec.getConfigurationSection("enchantments")
        enchantSec?.getKeys(false)?.forEach { enchKey ->
            val ench = runCatching { Enchantment.getByName(enchKey.uppercase()) }.getOrNull()
                ?: return@forEach
            val level = enchantSec.getInt(enchKey, 1)
            meta.addEnchant(ench, level, true)
        }

        // Hide enchant/attribute/misc flags for display items
        if (sec.getBoolean("hide-flags", false)) {
            meta.addItemFlags(*ItemFlag.values())
        }

        // Custom model data
        val cmd = sec.getInt("custom-model-data", -1)
        if (cmd >= 0) meta.setCustomModelData(cmd)

        // Unbreakable
        if (sec.getBoolean("unbreakable", false)) meta.isUnbreakable = true

        stack.itemMeta = meta
        return stack
    }

    // ── Section sub-parsers ───────────────────────────────────────────────────

    private fun ConfigurationSection.parseItemLayout(key: String): ItemLayout {
        val pattern = getString(key)
        if (!pattern.isNullOrBlank()) return ItemLayout.fromPattern(pattern)

        val slotList = getIntegerList("$key-slots")
        if (slotList.isNotEmpty()) return ItemLayout.fromSlots(slotList)

        return ItemLayout.default(getInt("gui-rows", 6))
    }

    private fun ConfigurationSection.parseNavBarConfig(key: String): NavBarConfig {
        val navSec = getConfigurationSection(key) ?: return NavBarConfig.DEFAULT
        val mode = NavBarMode.fromString(navSec.getString("mode", "INHERIT")!!)
        if (mode == NavBarMode.DISABLED) return NavBarConfig.DISABLED

        // For SELF mode, parse custom items
        val items = mutableMapOf<NavBarAction, NavBarItem>()
        val itemsSec = navSec.getConfigurationSection("items")
        itemsSec?.getKeys(false)?.forEach { actionKey ->
            val action = NavBarAction.entries.firstOrNull {
                it.name.replace("_", "-").equals(actionKey, ignoreCase = true) ||
                it.name.equals(actionKey, ignoreCase = true)
            } ?: return@forEach
            val itemSec = itemsSec.getConfigurationSection(actionKey) ?: return@forEach
            val mat = itemSec.parseMaterial("material") ?: Material.ARROW
            items[action] = NavBarItem(
                slot        = itemSec.getInt("slot", 49),
                action      = action,
                material    = mat,
                displayName = itemSec.component("name"),
                lore        = itemSec.componentList("lore"),
                customModelData = itemSec.getInt("custom-model-data", -1),
            )
        }
        return NavBarConfig(mode, items)
    }

    private fun ConfigurationSection.parseClickMapping(key: String): ClickMapping {
        val sec = getConfigurationSection(key) ?: return ClickMapping.DEFAULT
        return ClickMapping.fromConfig(
            left       = sec.getString("left", "BUY")!!,
            right      = sec.getString("right", "SELL")!!,
            middle     = sec.getString("middle", "SELL_ALL")!!,
            shiftLeft  = sec.getString("shift-left", "BUY_SCREEN")!!,
            shiftRight = sec.getString("shift-right", "SELL_SCREEN")!!,
            drop       = sec.getString("drop", "NONE")!!,
        )
    }

    private fun ConfigurationSection.parsePriceModifiers(key: String): List<PriceModifier> {
        val sec = getConfigurationSection(key) ?: return emptyList()
        val result = mutableListOf<PriceModifier>()

        sec.getConfigurationSection("seasonal")?.let { s ->
            result += PriceModifier.Seasonal(
                spring = s.getDouble("spring", 1.0),
                summer = s.getDouble("summer", 1.0),
                fall   = s.getDouble("fall",   1.0),
                winter = s.getDouble("winter", 1.0),
            )
        }

        val discount = sec.getDouble("discount", 0.0)
        if (discount > 0) result += PriceModifier.Discount(discount)

        val multiplier = sec.getDouble("multiplier", 1.0)
        if (multiplier != 1.0 && multiplier > 0) result += PriceModifier.Multiplier(multiplier)

        return result
    }

    private fun ConfigurationSection.parseRequirements(key: String): List<Requirement> {
        val sec = getConfigurationSection(key) ?: return emptyList()
        val result = mutableListOf<Requirement>()

        sec.getConfigurationSection("quest")?.let { q ->
            val questId = q.getString("id") ?: return@let
            result += Requirement.Quest(questId, q.getInt("stage", -1).takeIf { it >= 0 })
        }

        sec.getConfigurationSection("region")?.let { r ->
            val regionId = r.getString("id") ?: return@let
            result += Requirement.Region(regionId, r.getString("world"))
        }

        sec.getConfigurationSection("time")?.let { t ->
            val from = t.getInt("from", 0)
            val to   = t.getInt("to", 23999)
            result += Requirement.TimeOfDay(from, to)
        }

        return result
    }

    // ── Low-level YAML extension helpers ──────────────────────────────────────

    /** Parse [key] as a MiniMessage string → [Component]. Empty string → empty component. */
    private fun ConfigurationSection.component(key: String): Component {
        val raw = getString(key) ?: return Component.empty()
        return if (raw.isBlank()) Component.empty() else ColorUtils.parse(raw)
    }

    /** Parse [key] as a list of MiniMessage strings → list of [Component]s. */
    private fun ConfigurationSection.componentList(key: String): List<Component> =
        getStringList(key).map { ColorUtils.parse(it) }

    /** Parse [key] as a [Material] by name; returns null on unrecognised names. */
    private fun ConfigurationSection.parseMaterial(key: String): Material? =
        getString(key)?.uppercase()?.let { name ->
            runCatching { Material.valueOf(name) }.getOrNull()
        }

    /**
     * Parse [key] as an item that may be:
     * - a bare `MATERIAL` string
     * - a subsection with `material:`, `name:`, enchantments, etc.
     */
    private fun ConfigurationSection.parseMaterialItem(key: String): ItemStack? {
        val matString = getString(key)
        if (!matString.isNullOrBlank()) {
            val mat = runCatching { Material.valueOf(matString.uppercase()) }.getOrNull()
                ?: return null
            return ItemStack(mat)
        }
        val sec = getConfigurationSection(key) ?: return null
        val mat = sec.parseMaterial("material") ?: return null
        return buildItemStack(sec, mat)
    }

    /** Same as [parseMaterialItem] but for the section display item. */
    private fun ConfigurationSection.parseDisplayItem(key: String): ItemStack? =
        parseMaterialItem(key)

    /** Return a [Double] value for [key], or null if absent or not a number. */
    private fun ConfigurationSection.getDoubleOrNull(key: String): Double? =
        if (contains(key)) getDouble(key) else null
}
