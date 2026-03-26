package io.github.Earth1283.economyShopGUIOSS.gui.screens

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import io.github.Earth1283.economyShopGUIOSS.gui.GuiScreen
import io.github.Earth1283.economyShopGUIOSS.gui.components.ItemBuilder
import io.github.Earth1283.economyShopGUIOSS.lang.Lang
import io.github.Earth1283.economyShopGUIOSS.model.ShopSection
import io.github.Earth1283.economyShopGUIOSS.util.ColorUtils
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

/**
 * The top-level shop menu showing all non-hidden [ShopSection]s as clickable
 * icons in their configured slots.
 *
 * Layout is driven entirely by [ShopSection.slot] — the plugin does not impose
 * a fixed grid.  Fill items occupy remaining slots if configured.
 */
class MainMenuScreen(private val plugin: EconomyShopGUIOSS) : GuiScreen {

    private val sections: List<ShopSection> = plugin.shopManager.mainMenuSections()
    private val config = plugin.configManager.config
    private lateinit var inventory: Inventory

    // slot → section mapping built during [build]
    private val slotMap = mutableMapOf<Int, ShopSection>()

    override fun build(): Inventory {
        val rows = config.mainMenuRows.coerceIn(1, 6)
        val title = ColorUtils.parse(config.mainMenuTitle)
        inventory = Bukkit.createInventory(null, rows * 9, title)

        // Fill background
        config.mainMenuFillItem?.let { matName ->
            val mat = runCatching { Material.valueOf(matName.uppercase()) }.getOrNull()
            if (mat != null) {
                val fill = ItemBuilder(mat).name(Component.empty()).hideFlags().build()
                for (i in 0 until rows * 9) inventory.setItem(i, fill)
            }
        }

        // Place section icons
        slotMap.clear()
        for (section in sections) {
            val displayStack = section.displayItem ?: ItemBuilder(Material.CHEST)
                .name(section.displayName)
                .build()

            val iconStack = ItemBuilder.from(displayStack)
                .name(section.displayName)
                .build()

            inventory.setItem(section.slot, iconStack)
            slotMap[section.slot] = section
        }

        return inventory
    }

    override fun handleClick(event: InventoryClickEvent) {
        val slot = event.rawSlot
        val section = slotMap[slot] ?: return
        val player = event.whoClicked as? Player ?: return

        // Check section permission
        // (sections don't have their own permission field — items do)

        // Open shop screen for first page
        ShopScreen(plugin, section, page = 1).open(player)
    }
}
