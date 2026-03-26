package io.github.Earth1283.economyShopGUIOSS.lang

import net.kyori.adventure.text.Component

/**
 * Implemented by any domain object that can render itself as a [Component]
 * given a loaded [LangRegistry].
 *
 * Typical implementors: requirement display wrappers, price-modifier labels,
 * or shop-section descriptions that compose several [Lang] keys together.
 */
fun interface Translatable {
    fun translate(registry: LangRegistry): Component
}
