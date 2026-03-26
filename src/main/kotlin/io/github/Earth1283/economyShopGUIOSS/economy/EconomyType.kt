package io.github.Earth1283.economyShopGUIOSS.economy

/**
 * Identifies which economy backend a shop section uses.
 *
 * Sealed so exhaustive ``when`` expressions are enforced at compile time.
 * New economy types added here automatically produce compile errors at every
 * unhandled call site, making additions safe.
 */
sealed class EconomyType {

    /** Vault — the standard multi-economy abstraction layer. */
    data object Vault : EconomyType()

    /** Minecraft experience points / levels. */
    data object XP : EconomyType()

    /** PlayerPoints plugin currency. */
    data object PlayerPoints : EconomyType()

    /** GemsEconomy plugin currency. */
    data object Gems : EconomyType()

    // ── Parsing ───────────────────────────────────────────────────────────────

    companion object {
        /**
         * Parse a config string into an [EconomyType].
         * Accepts common aliases; falls back to [Vault] for unrecognised values.
         */
        fun fromString(value: String): EconomyType = when (value.lowercase().trim()) {
            "vault"                  -> Vault
            "xp", "exp", "levels"   -> XP
            "playerpoints", "points" -> PlayerPoints
            "gems", "gemseconomy"   -> Gems
            else                    -> Vault
        }
    }
}
