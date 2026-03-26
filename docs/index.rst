EconomyShopGUI-OSS
==================

A clean-room Kotlin reimplementation of the EconomyShopGUI Minecraft plugin,
targeting full feature parity while using idiomatic Kotlin and the modern
Adventure / MiniMessage text API throughout.

.. toctree::
   :maxdepth: 2
   :caption: Getting Started

   installation
   configuration
   shop-format

.. toctree::
   :maxdepth: 2
   :caption: Player & Admin Reference

   commands
   economy
   price-modifiers
   requirements
   shop-stands
   placeholders

.. toctree::
   :maxdepth: 2
   :caption: Developer Reference

   api
   events
   contributing
   changelog

----

Overview
--------

EconomyShopGUI-OSS provides a GUI-based shop experience for PaperMC servers.
Players browse paginated section menus, purchase or sell items in one click,
and use ``/sellall`` or the drag-and-drop ``/sellgui`` for bulk sales.

Administrators configure the shop entirely through YAML files under
``plugins/EconomyShopGUI-OSS/`` and can make live edits via the
in-game ``/eshop`` editor without restarting the server.

Key features
~~~~~~~~~~~~

* Fully configurable buy/sell GUI with unlimited sections and pages
* Vault, XP, PlayerPoints, and GemsEconomy support
* Price modifiers: seasonal adjustments, per-section discounts and multipliers
* Item requirements: quest completion, WorldGuard regions, time of day
* ``/sellall`` — bulk-sell from inventory, hand, or by item type
* Drag-and-drop ``/sellgui`` with Shulker Box contents support
* Physical shop stands (ArmorStand + item entity) persisted across restarts
* PlaceholderAPI expansion for external display
* Public Java/Kotlin API for other plugins
* Full text formatting via MiniMessage — no legacy ``§``/``&`` codes

Text formatting
~~~~~~~~~~~~~~~

All user-visible text in configuration files and the language file uses
`MiniMessage <https://docs.advntr.dev/minimessage>`_ syntax::

   name: "<gold>Premium Items"
   lore:
     - "<gray>Buy: <gold><buy_price>"
     - "<gray>Sell: <gold><sell_price>"

Refer to the MiniMessage documentation for the full tag reference
(colours, gradients, decorations, hover/click events, etc.).

Platform requirements
~~~~~~~~~~~~~~~~~~~~~

.. list-table::
   :header-rows: 1
   :widths: 30 70

   * - Requirement
     - Detail
   * - Server software
     - PaperMC 1.21 or newer (Folia also supported)
   * - Java
     - 21 or newer
   * - Kotlin runtime
     - Bundled in the plugin JAR (no separate install needed)
   * - Economy
     - Vault recommended; XP, PlayerPoints, GemsEconomy are alternatives
