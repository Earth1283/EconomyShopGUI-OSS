package io.github.Earth1283.economyShopGUIOSS.util

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Utility functions for inventory item operations.
 *
 * All functions operate on the player's main inventory (36 slots) only;
 * armour and off-hand are intentionally excluded from sell/remove operations.
 */
object ItemUtils {

    /**
     * Give [amount] copies of [template] to [player].
     *
     * Returns the number of items that could **not** be given (overflow that
     * was dropped or lost). Returns `0` on full success.
     */
    fun giveItems(player: Player, template: ItemStack, amount: Int): Int {
        if (amount <= 0) return 0
        val maxStack = template.type.maxStackSize
        var remaining = amount

        while (remaining > 0) {
            val stackSize = minOf(remaining, maxStack)
            val stack = template.clone().apply { this.amount = stackSize }
            val leftover = player.inventory.addItem(stack)
            if (leftover.isNotEmpty()) {
                // Could not fit — drop at feet and count as overflow
                leftover.values.forEach { player.world.dropItemNaturally(player.location, it) }
                remaining -= (stackSize - leftover.values.sumOf { it.amount })
                return remaining + leftover.values.sumOf { it.amount }
            }
            remaining -= stackSize
        }
        return 0
    }

    /**
     * Count how many of [template] the player holds in their main inventory.
     *
     * When [matchMeta] is `true`, item meta (name, enchants, lore) must also
     * match.  When `false`, only [ItemStack.type] is compared.
     */
    fun countItems(player: Player, template: ItemStack, matchMeta: Boolean): Int {
        return player.inventory.storageContents
            .filterNotNull()
            .filter { matches(it, template, matchMeta) }
            .sumOf { it.amount }
    }

    /**
     * Remove [amount] of [template] from the player's main inventory.
     *
     * Silently removes as many as possible (up to [amount]) without erroring
     * if the player has fewer than requested.  Callers should verify count
     * beforehand via [countItems].
     */
    fun removeItems(player: Player, template: ItemStack, amount: Int, matchMeta: Boolean) {
        var remaining = amount
        for (slot in 0 until player.inventory.storageContents.size) {
            if (remaining <= 0) break
            val stack = player.inventory.getItem(slot) ?: continue
            if (!matches(stack, template, matchMeta)) continue
            if (stack.amount <= remaining) {
                remaining -= stack.amount
                player.inventory.setItem(slot, null)
            } else {
                stack.amount -= remaining
                remaining = 0
            }
        }
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private fun matches(stack: ItemStack, template: ItemStack, matchMeta: Boolean): Boolean {
        if (stack.type != template.type) return false
        if (!matchMeta) return true
        return stack.itemMeta == template.itemMeta
    }
}
