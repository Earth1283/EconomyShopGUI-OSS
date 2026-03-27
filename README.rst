EconomyShopGUI-OSS
==================

An open-source, fully-featured Minecraft shop plugin for `PaperMC <https://papermc.io>`_ 1.21+,
written in idiomatic Kotlin with the modern Adventure / MiniMessage text API.

.. list-table::
   :widths: 20 80
   :stub-columns: 1

   * - Server
     - PaperMC 1.21+ · Folia supported
   * - Java
     - 21+
   * - Economy
     - Vault (default) · XP · PlayerPoints · GemsEconomy
   * - License
     - MIT

----

Features
--------

**Buy / sell GUI**

* Unlimited sections and pages per section
* One-click buy and sell with configurable stack sizes and quantity limits
* ``/sellall`` — bulk sell from inventory, hand, or by item type
* ``/sellgui`` — drag-and-drop sell screen with Shulker Box content support
* Middle-click sell-all directly from a shop screen

**Economy backends**

* Vault (works with EssentialsX, CMI, and any Vault-compatible provider)
* XP levels
* PlayerPoints
* GemsEconomy (connected via reflection — no hard dependency)

**Price modifiers**

* Seasonal adjustments (spring / summer / fall / winter) — optionally driven by RealisticSeasons
* Per-section buy/sell discounts and multipliers
* Global price multipliers

**Item requirements**

* Quest completion (Quests plugin)
* WorldGuard region membership
* Time-of-day windows

**Admin tooling**

* ``/eshop additem`` / ``edititem`` / ``delitem`` — manage shop items without restarting
* ``/eshop addsection`` / ``delsection`` / ``setslot`` — manage sections
* ``/eshop log`` — query the transaction log
* ``/eshop install <code>`` / ``upload`` — layout marketplace integration
* ``/sreload`` — hot-reload all YAML without a restart

**Physical shop stands**

* Place an ArmorStand-based stand that opens a section on right-click
* Persists across server restarts in ``stands.json``
* Configurable per stand (which section to open)

**Integrations**

* PlaceholderAPI — ``%economyshopgui_balance%``, ``%economyshopgui_buy_<section>_<item>%``, etc.
* bStats metrics
* Automatic update checker (notifies ops on join)
* Geyser / Floodgate — inventory resized for Bedrock players automatically

**Developer API**

* ``EconomyShopGUIHook.get()`` — read shop data, execute transactions, query balances
* ``PreTransactionEvent`` (cancellable, price-mutable) and ``PostTransactionEvent``
* ``ShopItemsLoadEvent`` fired on every load/reload
* Full RST documentation in ``docs/``

**Text formatting**

All user-visible text uses `MiniMessage <https://docs.advntr.dev/minimessage>`_ — zero legacy
``§``/``&`` color codes anywhere in config or source.

----

Quick start
-----------

1. Install `Vault <https://www.spigotmc.org/resources/vault.34315/>`_ and an economy provider
   (e.g. EssentialsX).
2. Drop ``EconomyShopGUI-OSS-<version>.jar`` into your ``plugins/`` folder.
3. Start the server. Configuration files are created in ``plugins/EconomyShopGUI-OSS/``.
4. Add a section by creating ``plugins/EconomyShopGUI-OSS/sections/tools.yml`` and a matching
   ``shops/tools.yml`` (see `docs/shop-format.rst <docs/shop-format.rst>`_).
5. Run ``/sreload`` and open the shop with ``/shop``.

----

Building from source
--------------------

Requires Java 21 and Git. No separate Kotlin installation needed.

::

   git clone https://github.com/Earth1283/EconomyShopGUI-OSS.git
   cd EconomyShopGUI-OSS
   ./gradlew build

Output JAR: ``build/libs/EconomyShopGUI-OSS-<version>.jar``

Always deploy the **shadow JAR** (the one without ``-plain`` in the name) — it bundles all
runtime dependencies.

To run a local Paper 1.21 test server::

   ./gradlew runServer

----

Commands
--------

.. list-table::
   :header-rows: 1
   :widths: 35 30 35

   * - Command
     - Permission
     - Description
   * - ``/shop [section] [page]``
     - ``economyshopgui.shop``
     - Open the main menu or jump to a section
   * - ``/sellall [inventory|item|hand] [qty]``
     - ``economyshopgui.sellall``
     - Bulk sell items from inventory
   * - ``/sellgui``
     - ``economyshopgui.sellgui``
     - Open the drag-and-drop sell screen
   * - ``/shopgive <item> <player>``
     - ``economyshopgui.admin``
     - Give a shop item directly to a player
   * - ``/sreload``
     - ``economyshopgui.admin``
     - Hot-reload all configuration
   * - ``/eshop <subcommand>``
     - ``economyshopgui.admin``
     - In-game editor and admin tools

----

Documentation
-------------

Full documentation lives in ``docs/`` and covers:

* `Installation <docs/installation.rst>`_
* `Configuration reference <docs/configuration.rst>`_
* `Shop format <docs/shop-format.rst>`_
* `Commands <docs/commands.rst>`_
* `Economy backends <docs/economy.rst>`_
* `Price modifiers <docs/price-modifiers.rst>`_
* `Requirements <docs/requirements.rst>`_
* `Shop stands <docs/shop-stands.rst>`_
* `PlaceholderAPI placeholders <docs/placeholders.rst>`_
* `Developer API <docs/api.rst>`_
* `Events <docs/events.rst>`_
* `Marketplace API contract <docs/marketplace-api.rst>`_
* `Contributing <docs/contributing.rst>`_
* `Changelog <docs/changelog.rst>`_

----

Contributing
------------

Pull requests are welcome. Please read `docs/contributing.rst <docs/contributing.rst>`_ for the
code style guide and branch conventions. Key rules:

* Kotlin only — no Java source files
* MiniMessage everywhere — no legacy ``&`` color codes
* Immutable data classes for domain models; sealed classes for variant types
* ``./gradlew build`` must pass with zero errors before opening a PR

----

License
-------

MIT — see ``LICENSE``.
