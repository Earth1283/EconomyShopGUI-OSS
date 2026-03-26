package io.github.Earth1283.economyShopGUIOSS.lang

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/**
 * Every user-visible message key used by the plugin.
 *
 * Each entry carries a [default] MiniMessage string that is used when no
 * language file is loaded or when the key is absent from the loaded file.
 * All defaults use MiniMessage syntax — legacy `&`/`§` codes are never used.
 *
 * Callers resolve a key to a rendered [Component] via [LangRegistry.resolve]:
 * ```kotlin
 * player.sendMessage(Lang.BUY_SUCCESS.resolve(langRegistry,
 *     Placeholder.parsed("qty",  "3"),
 *     Placeholder.parsed("item", "Diamond"),
 *     Placeholder.parsed("price", "$4.50")
 * ))
 * ```
 */
@Suppress("unused")
enum class Lang(val default: String) {

    // ── General ──────────────────────────────────────────────────────────────
    NO_PERMISSION("<red>You don't have permission to do that."),
    PLAYER_ONLY("<red>This command can only be run by a player."),
    CONSOLE_ONLY("<red>This command can only be run from the console."),
    UNKNOWN_ERROR("<red>An unexpected error occurred. Please check the console."),
    PLAYER_NOT_FOUND("<red>Player <yellow><player></yellow> was not found."),

    // ── Plugin Lifecycle ──────────────────────────────────────────────────────
    RELOAD_SUCCESS("<green>Configuration reloaded successfully."),
    RELOAD_FAILED("<red>Reload failed — check the console for details."),
    UPDATE_AVAILABLE(
        "<gold>[EconomyShopGUI-OSS] <yellow>A new version is available: " +
        "<white><version></white>. Download at <aqua><url></aqua>"
    ),
    UPDATE_CURRENT("<green>EconomyShopGUI-OSS is up to date (<version>)."),

    // ── Shop Navigation ───────────────────────────────────────────────────────
    SHOP_OPENED("<green>Opening the shop…"),
    SHOP_INVALID_SECTION("<red>Section <yellow><section></yellow> does not exist."),
    SHOP_INVALID_PAGE(
        "<red>Page <yellow><page></yellow> does not exist for " +
        "section <yellow><section></yellow>."
    ),
    SHOP_WORLD_DISABLED("<red>The shop is disabled in this world."),
    SHOP_SECTION_HIDDEN("<red>That section is not accessible."),

    // ── Buying ────────────────────────────────────────────────────────────────
    BUY_SUCCESS("<green>Purchased <white><qty>x <item></white> for <gold><price></gold>."),
    BUY_FAILED_FUNDS(
        "<red>You can't afford this. You need <gold><price></gold> " +
        "but only have <gold><balance></gold>."
    ),
    BUY_FAILED_INVENTORY_FULL("<red>Your inventory is full."),
    BUY_FAILED_MAX_REACHED(
        "<red>You have reached the maximum purchase limit of " +
        "<yellow><max></yellow> for this item."
    ),
    BUY_FAILED_NOT_BUYABLE("<red>This item cannot be purchased."),
    BUY_FAILED_CANCELLED("<red>Purchase cancelled."),
    BUY_FAILED_REQUIREMENT("<red>You do not meet the requirements to buy this item."),

    // ── Selling ───────────────────────────────────────────────────────────────
    SELL_SUCCESS("<green>Sold <white><qty>x <item></white> for <gold><price></gold>."),
    SELL_FAILED_NO_ITEMS("<red>You don't have any of that item to sell."),
    SELL_FAILED_MAX_REACHED(
        "<red>You have reached the maximum sell limit of " +
        "<yellow><max></yellow> for this item."
    ),
    SELL_FAILED_NOT_SELLABLE("<red>This item cannot be sold."),
    SELL_FAILED_CANCELLED("<red>Sale cancelled."),
    SELL_FAILED_REQUIREMENT("<red>You do not meet the requirements to sell this item."),

    // ── Sell All (/sellall) ───────────────────────────────────────────────────
    SELLALL_HEADER("<dark_gray>━━━━━━━━━━━━ <gold>Sell All Results</gold> ━━━━━━━━━━━━"),
    SELLALL_ITEM_ROW("<gray>  <white><qty>x <item></white> <dark_gray>→</dark_gray> <gold><price></gold>"),
    SELLALL_FOOTER("<dark_gray>━━━━━━━━━━━━ <green>Total: <gold><total></gold> <dark_gray>━━━━━━━━━━━━"),
    SELLALL_NOTHING_TO_SELL("<yellow>You have no sellable items in your inventory."),
    SELLALL_WORLD_DISABLED("<red>Sell All is disabled in this world."),
    SELLALL_INVALID_MODE(
        "<red>Invalid mode. Usage: <yellow>/sellall [inventory|item|hand] [quantity]"
    ),

    // ── Sell GUI (/sellgui) ───────────────────────────────────────────────────
    SELLGUI_TITLE("<dark_gray>Sell Items"),
    SELLGUI_CONFIRM_NAME("<green>Confirm Sale"),
    SELLGUI_CONFIRM_LORE_TOTAL("<gray>Total: <gold><price>"),
    SELLGUI_CONFIRM_LORE_CLICK("<yellow>Click to sell all items above."),
    SELLGUI_CANCEL_NAME("<red>Cancel"),
    SELLGUI_CANCEL_LORE("<gray>Click to cancel and retrieve your items."),
    SELLGUI_SUCCESS("<green>Sold items for a total of <gold><price></gold>."),
    SELLGUI_NOTHING("<yellow>Place items in the top slots to sell them."),
    SELLGUI_WORLD_DISABLED("<red>Sell GUI is disabled in this world."),

    // ── Shop Give (/shopgive) ─────────────────────────────────────────────────
    SHOPGIVE_USAGE("<yellow>Usage: /shopgive <section.item> <player> [qty]"),
    SHOPGIVE_SUCCESS("<green>Gave <white><qty>x <item></white> to <yellow><player></yellow>."),
    SHOPGIVE_RECEIVED("<green>You received <white><qty>x <item></white> from <yellow><sender></yellow>."),
    SHOPGIVE_INVALID_ITEM("<red>Item <yellow><item></yellow> was not found in any shop section."),
    SHOPGIVE_PLAYER_INVENTORY_FULL(
        "<red><player>'s inventory is full. The item was dropped on the ground."
    ),

    // ── Navigation GUI Elements ───────────────────────────────────────────────
    GUI_PREVIOUS_PAGE_NAME("<yellow>← Previous Page"),
    GUI_PREVIOUS_PAGE_LORE("<gray>Go to page <white><page></white>."),
    GUI_NEXT_PAGE_NAME("<yellow>Next Page →"),
    GUI_NEXT_PAGE_LORE("<gray>Go to page <white><page></white>."),
    GUI_CURRENT_PAGE_NAME("<gold>Page <page> <dark_gray>/ <gray><pages>"),
    GUI_CURRENT_PAGE_LORE(""),
    GUI_BACK_NAME("<yellow>← Back"),
    GUI_BACK_LORE("<gray>Return to the previous menu."),
    GUI_MAIN_MENU_NAME("<yellow>⌂ Main Menu"),
    GUI_MAIN_MENU_LORE("<gray>Return to the main shop menu."),
    GUI_SEARCH_NAME("<yellow>⌕ Search"),
    GUI_SEARCH_LORE("<gray>Search for a shop item by name."),
    GUI_CLOSE_NAME("<red>✕ Close"),
    GUI_CLOSE_LORE("<gray>Close the shop."),

    // ── Item Lore Templates ───────────────────────────────────────────────────
    LORE_BUY_PRICE("<gray>Buy: <gold><price>"),
    LORE_SELL_PRICE("<gray>Sell: <gold><price>"),
    LORE_NO_BUY("<gray>Buy: <red>Not for sale"),
    LORE_NO_SELL("<gray>Sell: <red>Not accepted"),
    LORE_MAX_BUY("<gray>Max purchase: <yellow><max>"),
    LORE_MAX_SELL("<gray>Max sell: <yellow><max>"),
    LORE_LEFT_CLICK_BUY("<yellow>Left-click <gray>to buy"),
    LORE_RIGHT_CLICK_SELL("<yellow>Right-click <gray>to sell"),
    LORE_MIDDLE_CLICK_SELL_ALL("<yellow>Middle-click <gray>to sell all"),
    LORE_SHIFT_LEFT_CLICK("<yellow>Shift + Left-click <gray>to buy a stack"),
    LORE_SHIFT_RIGHT_CLICK("<yellow>Shift + Right-click <gray>to sell all"),

    // ── Requirements in Lore ──────────────────────────────────────────────────
    LORE_REQUIREMENT_HEADER("<gray>Requirements:"),
    LORE_REQUIREMENT_QUEST_MET("<green>  ✔ Quest: <white><quest>"),
    LORE_REQUIREMENT_QUEST_UNMET("<red>  ✘ Quest: <white><quest>"),
    LORE_REQUIREMENT_REGION_MET("<green>  ✔ Region: <white><region>"),
    LORE_REQUIREMENT_REGION_UNMET("<red>  ✘ Region: <white><region>"),
    LORE_REQUIREMENT_TIME_MET("<green>  ✔ Time: <white><from> – <to>"),
    LORE_REQUIREMENT_TIME_UNMET("<red>  ✘ Time: <white><from> – <to>"),

    // ── Requirement Failure Messages ──────────────────────────────────────────
    REQUIREMENT_QUEST_FAILED("<red>You must complete quest <yellow><quest></yellow> first."),
    REQUIREMENT_REGION_FAILED("<red>You must be in region <yellow><region></yellow> to use this shop."),
    REQUIREMENT_TIME_FAILED(
        "<red>This item is only available between " +
        "<yellow><from></yellow> and <yellow><to></yellow>."
    ),

    // ── Economy ───────────────────────────────────────────────────────────────
    ECONOMY_NOT_LOADED("<red>The required economy plugin is not loaded. Contact an administrator."),
    ECONOMY_VAULT_NOT_FOUND("<red>Vault is not installed. Economy features are disabled."),
    ECONOMY_WITHDRAW_FAILED("<red>Failed to withdraw from your balance. Contact an administrator."),
    ECONOMY_DEPOSIT_FAILED("<red>Failed to deposit to your balance. Contact an administrator."),

    // ── Transaction Screen ────────────────────────────────────────────────────
    TRANSACTION_SCREEN_TITLE("<dark_gray>Confirm Transaction"),
    TRANSACTION_BUY_TITLE("<dark_gray>Buy: <white><item>"),
    TRANSACTION_SELL_TITLE("<dark_gray>Sell: <white><item>"),
    TRANSACTION_CONFIRM_BUY("<green>✔ Buy"),
    TRANSACTION_CONFIRM_SELL("<green>✔ Sell"),
    TRANSACTION_CANCEL("<red>✘ Cancel"),
    TRANSACTION_AMOUNT_PROMPT("<yellow>Enter an amount in chat (or type 'cancel'):"),
    TRANSACTION_AMOUNT_INVALID("<red>Invalid amount. Please enter a positive whole number."),
    TRANSACTION_AMOUNT_CANCELLED("<yellow>Transaction cancelled."),

    // ── In-Game Editor (/eshop) ───────────────────────────────────────────────
    EDITOR_USAGE("<yellow>Usage: <white>/eshop <subcommand> [args...]"),
    EDITOR_SUBCOMMAND_UNKNOWN(
        "<red>Unknown subcommand: <yellow><sub></yellow>. " +
        "Use <white>/eshop help</white> for a list."
    ),
    EDITOR_ITEM_ADDED("<green>Item <yellow><item></yellow> added to section <yellow><section></yellow>."),
    EDITOR_ITEM_EDITED("<green>Item <yellow><item></yellow> in section <yellow><section></yellow> updated."),
    EDITOR_ITEM_DELETED("<green>Item <yellow><item></yellow> removed from section <yellow><section></yellow>."),
    EDITOR_ITEM_NOT_FOUND("<red>Item <yellow><item></yellow> was not found in section <yellow><section></yellow>."),
    EDITOR_ITEM_INVALID_MATERIAL("<red><yellow><material></yellow> is not a valid material name."),
    EDITOR_ITEM_INVALID_PRICE("<red>Invalid price: <yellow><value></yellow>. Must be a positive number."),
    EDITOR_ITEM_HAND_EMPTY("<red>You must be holding an item."),
    EDITOR_SECTION_ADDED("<green>Section <yellow><section></yellow> created."),
    EDITOR_SECTION_EDITED("<green>Section <yellow><section></yellow> updated."),
    EDITOR_SECTION_DELETED("<green>Section <yellow><section></yellow> deleted."),
    EDITOR_SECTION_NOT_FOUND("<red>Section <yellow><section></yellow> does not exist."),
    EDITOR_SECTION_ALREADY_EXISTS("<red>Section <yellow><section></yellow> already exists."),
    EDITOR_IMPORT_SUCCESS("<green>Import complete: <yellow><count></yellow> item(s) imported."),
    EDITOR_IMPORT_FAILED("<red>Import failed — check the console for details."),
    EDITOR_MIGRATE_SUCCESS("<green>Migration complete."),
    EDITOR_MIGRATE_FAILED("<red>Migration failed — check the console for details."),
    EDITOR_LAYOUT_INSTALLED("<green>Layout <yellow><layout></yellow> installed successfully."),
    EDITOR_LAYOUT_UPLOADED("<green>Layout uploaded. Share code: <aqua><code></aqua>"),
    EDITOR_LAYOUT_UPDATED("<green>Layout <yellow><layout></yellow> updated."),
    EDITOR_LAYOUT_NOT_FOUND("<red>Layout <yellow><layout></yellow> was not found on the marketplace."),
    EDITOR_LAYOUT_FETCH_ERROR("<red>Could not connect to the marketplace. Try again later."),

    // ── Transaction Log ───────────────────────────────────────────────────────
    TRANSACTION_LOG_EXPORTED("<green>Transaction log exported to <yellow><file></yellow>."),
    TRANSACTION_LOG_EXPORT_FAILED("<red>Export failed — check the console for details."),
    TRANSACTION_LOG_EMPTY("<yellow>The transaction log is empty."),

    // ── Shop Stands ───────────────────────────────────────────────────────────
    STAND_GIVEN("<green>Shop stand for <yellow><item></yellow> placed in your hand."),
    STAND_PLACED("<green>Shop stand created."),
    STAND_DESTROYED("<green>Shop stand destroyed."),
    STAND_INVALID_ITEM("<red>Item <yellow><item></yellow> was not found in any shop section."),
    STAND_NOT_FOUND("<red>No shop stand found at that location."),
    STAND_BROWSE_TITLE("<dark_gray>Shop Stands"),
    STAND_BROWSE_ENTRY(
        "<gray>  <yellow><id></yellow> <dark_gray>│</dark_gray> " +
        "<white><item></white> <dark_gray>@</dark_gray> " +
        "<gray><world> <white><x></white>, <white><y></white>, <white><z></white>"
    ),
    STAND_BROWSE_EMPTY("<yellow>No shop stands have been placed."),
    STAND_EDIT_SUCCESS("<green>Stand <yellow><id></yellow> updated."),

    // ── Price Modifiers (info) ────────────────────────────────────────────────
    MODIFIER_DISCOUNT_APPLIED("<gray>Discount applied: <green>-<percent>%"),
    MODIFIER_MULTIPLIER_APPLIED("<gray>Price multiplier: <gold>×<factor>"),
    MODIFIER_SEASONAL("<gray>Seasonal price (<season>): <gold><price>"),

    // ── Navigation GUI ────────────────────────────────────────────────────────
    NAV_PREVIOUS_PAGE("<yellow>← Previous Page <gray>(Page <page>)"),
    NAV_NEXT_PAGE("<yellow>Next Page → <gray>(Page <page>)"),
    NAV_MAIN_MENU("<yellow>⌂ Main Menu"),

    // ── Buy/Sell Quantity Screens ─────────────────────────────────────────────
    BUY_SCREEN_TITLE("<dark_gray>Buy: <white><item>"),
    BUY_SCREEN_PRICE("<gray>Price per unit: <gold><price>"),
    BUY_SCREEN_QTY("<gray>Quantity: <white><qty>"),
    BUY_SCREEN_ADD("<green>+<amount>"),
    BUY_SCREEN_REMOVE("<red><amount>"),
    SELL_SCREEN_TITLE("<dark_gray>Sell: <white><item>"),
    SELL_SCREEN_PRICE("<gray>Price per unit: <gold><price>"),
    SELL_SCREEN_QTY("<gray>Quantity: <white><qty>"),

    // ── Sell GUI ──────────────────────────────────────────────────────────────
    SELL_GUI_TITLE("<dark_gray>Sell Items"),
    SELL_GUI_CONFIRM("<green>✔ Sell Items"),
    SELL_GUI_CONFIRM_LORE("<gray>Close or click to sell all items above."),

    // ── Transaction Confirm/Cancel ────────────────────────────────────────────
    CONFIRM("<green>✔ Confirm — Buy <qty>x for <gold><price></gold>"),
    CONFIRM_SELL("<green>✔ Confirm — Sell <qty>x for <gold><price></gold>"),
    CANCEL("<red>✘ Cancel"),

    // ── Shorthand Transaction Failure Messages ────────────────────────────────
    NOT_ENOUGH_MONEY(
        "<red>Insufficient funds. You need <gold><amount></gold> " +
        "but have <gold><balance></gold>."
    ),
    INVENTORY_FULL("<red>Your inventory is full."),
    NOT_ENOUGH_ITEMS(
        "<red>You only have <yellow><held></yellow> " +
        "of the required <yellow><required></yellow>."
    ),
    CANNOT_BUY("<red>This item cannot be purchased."),
    CANNOT_SELL("<red>This item cannot be sold."),
    REQUIREMENT_NOT_MET("<red>Requirement not met: <yellow><requirement>"),
    ECONOMY_UNAVAILABLE("<red>The economy plugin is not available. Contact an administrator."),
    INVALID_QUANTITY(
        "<red>Invalid quantity. Minimum: <yellow><min></yellow>, " +
        "Maximum: <yellow><max></yellow>."
    ),
    GENERIC_ERROR("<red>An error occurred. Please try again."),
    NO_ITEMS_TO_SELL("<red>You have no items to sell."),
    SELL_ALL_SUCCESS("<green>Sold all items for a total of <gold><price></gold>."),

    // ── Marketplace ───────────────────────────────────────────────────────────
    MARKETPLACE_CONNECTING("<yellow>Connecting to the marketplace…"),
    MARKETPLACE_CONNECTED("<green>Connected."),
    MARKETPLACE_ERROR("<red>Marketplace error: <white><message>");

    // ── Resolution helpers ────────────────────────────────────────────────────

    /** Resolve this key to a [Component] using the loaded language file. */
    fun resolve(registry: LangRegistry): Component = registry.resolve(this)

    /**
     * Resolve this key with MiniMessage [TagResolver]s for placeholder
     * substitution (e.g. `Placeholder.parsed("price", "$4.50")`).
     */
    fun resolve(registry: LangRegistry, vararg tags: TagResolver): Component =
        registry.resolve(this, *tags)
}
