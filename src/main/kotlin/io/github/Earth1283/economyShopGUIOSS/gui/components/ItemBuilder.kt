package io.github.Earth1283.economyShopGUIOSS.gui.components

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

/**
 * Fluent builder for [ItemStack] instances used in GUI screens.
 *
 * All display properties accept [Component] so that MiniMessage formatting is
 * preserved end-to-end — no legacy color codes are introduced at any point.
 *
 * Example:
 * ```kotlin
 * val item = ItemBuilder(Material.DIAMOND)
 *     .name(ColorUtils.parse("<aqua>Diamond"))
 *     .lore(listOf(ColorUtils.parse("<gray>Click to buy")))
 *     .amount(3)
 *     .hideFlags()
 *     .build()
 * ```
 */
class ItemBuilder(private val material: Material, private var amount: Int = 1) {

    private var name: Component?          = null
    private var lore: List<Component>     = emptyList()
    private var customModelData: Int      = -1
    private var unbreakable: Boolean      = false
    private var hideFlags: Boolean        = false
    private val enchants: MutableMap<Enchantment, Int> = mutableMapOf()

    // ── Builder methods ───────────────────────────────────────────────────────

    fun name(component: Component?)   = apply { name = component }
    fun lore(components: List<Component>) = apply { lore = components }
    fun amount(n: Int)                = apply { amount = n.coerceIn(1, 64) }
    fun customModelData(cmd: Int)     = apply { customModelData = cmd }
    fun unbreakable(value: Boolean = true) = apply { unbreakable = value }
    fun hideFlags(value: Boolean = true)   = apply { hideFlags = value }
    fun enchant(enchantment: Enchantment, level: Int) = apply { enchants[enchantment] = level }

    // ── Build ─────────────────────────────────────────────────────────────────

    fun build(): ItemStack {
        val stack = ItemStack(material, amount)
        val meta = stack.itemMeta ?: return stack

        name?.let { meta.displayName(it) }
        if (lore.isNotEmpty()) meta.lore(lore)
        if (customModelData >= 0) meta.setCustomModelData(customModelData)
        if (unbreakable) meta.isUnbreakable = true
        if (hideFlags) meta.addItemFlags(*ItemFlag.values())
        enchants.forEach { (ench, lvl) -> meta.addEnchant(ench, lvl, true) }

        stack.itemMeta = meta
        return stack
    }

    // ── Companion factory ─────────────────────────────────────────────────────

    companion object {
        /** Create a builder pre-loaded from an existing [ItemStack]. */
        fun from(stack: ItemStack): ItemBuilder {
            val builder = ItemBuilder(stack.type, stack.amount)
            val meta = stack.itemMeta ?: return builder
            builder.name   = meta.displayName()
            builder.lore   = meta.lore() ?: emptyList()
            if (meta.hasCustomModelData()) builder.customModelData = meta.customModelData
            builder.unbreakable = meta.isUnbreakable
            return builder
        }

        /** Convenience: build a single-item with just a name and material. */
        fun simple(material: Material, name: Component): ItemStack =
            ItemBuilder(material).name(name).build()
    }
}
