package io.github.Earth1283.economyShopGUIOSS.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/**
 * Thin, stateless wrapper around the Adventure [MiniMessage] parser.
 *
 * This is the **only** text-parsing API used in this codebase. Legacy
 * `&`-style or `§`-style colour codes are never accepted anywhere.
 *
 * All user-visible strings — item names, lore, inventory titles, chat
 * messages — flow through [parse] on their way to a [Component].
 */
object ColorUtils {

    /** The singleton MiniMessage instance (thread-safe and allocation-free). */
    private val mm: MiniMessage = MiniMessage.miniMessage()

    /**
     * Deserialise a MiniMessage string into a [Component].
     *
     * Example:
     * ```kotlin
     * val c = ColorUtils.parse("<green>Hello, <yellow><name>!</yellow>",
     *             Placeholder.parsed("name", player.name))
     * player.sendMessage(c)
     * ```
     */
    fun parse(input: String): Component = mm.deserialize(input)

    /**
     * Deserialise a MiniMessage string with one or more [TagResolver]s
     * that supply placeholder values at parse time.
     */
    fun parse(input: String, vararg tags: TagResolver): Component =
        mm.deserialize(input, *tags)

    /**
     * Serialise a [Component] back into a MiniMessage string.
     * Useful for storing edited text in YAML after in-game editing.
     */
    fun serialize(component: Component): String = mm.serialize(component)
}
