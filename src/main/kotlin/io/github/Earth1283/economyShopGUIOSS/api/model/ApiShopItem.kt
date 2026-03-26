package io.github.Earth1283.economyShopGUIOSS.api.model

import net.kyori.adventure.text.Component

/**
 * Immutable, API-facing view of a [io.github.Earth1283.economyShopGUIOSS.model.ShopItem].
 *
 * All prices are the **base** prices from YAML; they do not reflect
 * per-player modifier application.  Use
 * [io.github.Earth1283.economyShopGUIOSS.api.EconomyShopGUIHook.buy] /
 * [io.github.Earth1283.economyShopGUIOSS.api.EconomyShopGUIHook.sell]
 * to execute a transaction that applies modifiers correctly.
 */
data class ApiShopItem(
    val id: String,
    val sectionId: String,
    val page: Int,
    val slot: Int,
    val displayName: Component,
    /** Base buy price from config, or null if not purchasable. */
    val buyPrice: Double?,
    /** Base sell price from config, or null if not sellable. */
    val sellPrice: Double?,
    /** Economy type string: `"vault"`, `"xp"`, `"playerpoints"`, `"gems"`. */
    val economyType: String,
    val stackSize: Int,
    /** `-1` means unlimited. */
    val maxBuyQty: Int,
    /** `-1` means unlimited. */
    val maxSellQty: Int,
    /** Required permission node, or null. */
    val permission: String?,
)
