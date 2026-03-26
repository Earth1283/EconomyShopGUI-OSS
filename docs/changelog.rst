Changelog
=========

----

1.0.0 (unreleased)
-------------------

Initial open-source release of EconomyShopGUI-OSS.

**Features**

- Full buy/sell GUI with paginated sections and multiple pages per section
- Economy backends: Vault (default), XP, PlayerPoints, GemsEconomy
- Price modifiers: seasonal (spring/summer/fall/winter), per-section discounts,
  global multipliers
- Item requirements: quest completion (Quests plugin), WorldGuard region,
  time of day
- ``/shop`` — open main menu or jump to a section/page
- ``/sellall [inventory|hand]`` — bulk sell from inventory
- ``/sellgui`` — drag-and-drop sell screen
- ``/shopgive`` — admin command to give shop items directly
- ``/sreload`` — hot-reload all configuration without restart
- ``/eshop`` — in-game editor: add/edit/remove items and sections, view
  transaction log
- SQLite transaction log via Exposed ORM (async writes)
- ``PreTransactionEvent`` (cancellable, price-mutable) and
  ``PostTransactionEvent`` for third-party plugin integration
- ``ShopItemsLoadEvent`` fired on every load/reload
- Physical shop stands — blocks that open a shop when right-clicked;
  persist across restarts in ``stands.yml``
- PlaceholderAPI expansion (``%economyshopgui_*%``)
- bStats metrics integration
- Layout marketplace: ``/eshop install <code>`` downloads and installs
  community shop layouts; ``/eshop upload`` shares your layouts
- Public API: ``EconomyShopGUIHook.get()`` for third-party plugin integration
- Adventure/MiniMessage throughout — zero legacy ``§``/``&`` color codes
- Folia-compatible scheduling via ``SchedulerUtils``
- Paper 1.21, Java 21, Kotlin 2.x
- RST documentation covering all features, the public API, and the full
  marketplace HTTP API contract for clean-room server implementations

**Known limitations in 1.0.0**

- In-game editor GUI screens (click-to-edit UI) are not yet implemented;
  use ``/eshop additem`` / ``edititem`` commands instead
- Hologram display above shop stands requires a future update
- Spawner hook integrations (SilkSpawners, WildStacker, etc.) are wired
  at the soft-depend level but not yet implemented
- The marketplace base URL is a placeholder; the live server will be
  announced when the project is published
