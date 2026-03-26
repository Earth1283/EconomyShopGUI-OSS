package io.github.Earth1283.economyShopGUIOSS.model

import io.github.Earth1283.economyShopGUIOSS.economy.EconomyType
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

/**
 * An immutable representation of a single item in the shop.
 *
 * All user-visible text ([displayName], [lore]) is stored as [Component]
 * — already deserialized from MiniMessage at config-load time — so rendering
 * in the GUI never re-parses strings.
 *
 * Price fields use `Double?`: a `null` value means the action is disabled
 * (e.g. `buyPrice = null` means the item cannot be purchased).
 *
 * **Thread safety:** All fields are `val`; the only mutable value is the
 * [ItemStack] (Bukkit makes these mutable by design).  Always [clone][ItemStack.clone]
 * before placing into an inventory.
 */
data class ShopItem(

    // ── Identity ──────────────────────────────────────────────────────────────

    /** The item's key within its section (e.g. `"apple"`). */
    val id: String,
    /** The parent section's ID (e.g. `"food"`). */
    val sectionId: String,
    /** 1-indexed page number this item lives on. */
    val page: Int,
    /** Slot index (0–53) within the page inventory. */
    val slot: Int,

    // ── Display ───────────────────────────────────────────────────────────────

    /** Base item used as the GUI icon (material, enchant glint, etc.). */
    val itemStack: ItemStack,
    /** Overridden display name; falls back to [ItemStack] name when empty. */
    val displayName: Component,
    /** Extra lore lines appended below the auto-generated price lore. */
    val lore: List<Component>,

    // ── Pricing ───────────────────────────────────────────────────────────────

    /** Buy price per [stackSize]; `null` = item cannot be purchased. */
    val buyPrice: Double?,
    /** Sell price per [stackSize]; `null` = item cannot be sold. */
    val sellPrice: Double?,
    /** Economy type for this item (inherits from section when not overridden). */
    val economyType: EconomyType,

    // ── Transaction limits ────────────────────────────────────────────────────

    /** How many items are exchanged per click. */
    val stackSize: Int = 1,
    /** Maximum quantity per buy transaction; -1 = unlimited. */
    val maxBuyQty: Int = -1,
    /** Maximum quantity per sell transaction; -1 = unlimited. */
    val maxSellQty: Int = -1,
    /** Minimum quantity per buy transaction. */
    val minBuyQty: Int = 1,
    /** Minimum quantity per sell transaction. */
    val minSellQty: Int = 1,

    // ── Requirements & modifiers ──────────────────────────────────────────────

    /** All requirements that must be met before buying or selling. */
    val requirements: List<Requirement> = emptyList(),
    /** Price modifier chain applied before every transaction. */
    val modifiers: List<PriceModifier> = emptyList(),

    // ── Behaviour flags ───────────────────────────────────────────────────────

    /** Permission node required to interact with this item (`null` = no permission needed). */
    val permission: String? = null,
    /** If set, clicking this item opens the named section instead of transacting. */
    val linkedSectionId: String? = null,
    /** Suppress the auto-generated price lore lines. */
    val hidePricingLore: Boolean = false,
    /** Close the shop inventory after a successful purchase. */
    val closeOnPurchase: Boolean = false,
    /** Match NBT metadata when checking the player's inventory for sell eligibility. */
    val matchMeta: Boolean = false,

) {
    // ── Effective price computation ───────────────────────────────────────────

    /**
     * Compute the effective buy price after applying all [modifiers].
     *
     * @param season  Current season for [PriceModifier.Seasonal]; pass `null`
     *                when season data is unavailable.
     * @return        The adjusted buy price, or `null` if the item is not buyable.
     */
    fun effectiveBuyPrice(season: Season? = null): Double? =
        buyPrice?.let { modifiers.applyAll(it, season) }

    /**
     * Compute the effective sell price after applying all [modifiers].
     *
     * @param season  Current season for [PriceModifier.Seasonal].
     * @return        The adjusted sell price, or `null` if the item is not sellable.
     */
    fun effectiveSellPrice(season: Season? = null): Double? =
        sellPrice?.let { modifiers.applyAll(it, season) }

    // ── Convenience ───────────────────────────────────────────────────────────

    val isBuyable: Boolean  get() = buyPrice != null
    val isSellable: Boolean get() = sellPrice != null
    val isLink: Boolean     get() = linkedSectionId != null
}
