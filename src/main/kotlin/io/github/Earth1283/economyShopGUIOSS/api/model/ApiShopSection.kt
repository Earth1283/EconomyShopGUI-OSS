package io.github.Earth1283.economyShopGUIOSS.api.model

import net.kyori.adventure.text.Component

/**
 * Immutable, API-facing view of a [io.github.Earth1283.economyShopGUIOSS.model.ShopSection].
 *
 * External plugins receive this type from [io.github.Earth1283.economyShopGUIOSS.api.EconomyShopGUIHook]
 * and cannot modify internal shop state through it.
 */
data class ApiShopSection(
    val id: String,
    val displayName: Component,
    val enabled: Boolean,
    val slot: Int,
    val hidden: Boolean,
    val items: List<ApiShopItem>,
)
