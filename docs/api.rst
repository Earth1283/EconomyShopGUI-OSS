Developer API
=============

EconomyShopGUI-OSS exposes a stable Kotlin/Java API that lets other plugins
read shop data, execute transactions, and listen to shop events without
depending on internal classes.

.. contents::
   :local:
   :depth: 2

----

Dependency
----------

Add EconomyShopGUI-OSS as a ``softdepend`` or ``depend`` in your
``plugin.yml``:

.. code-block:: yaml

   softdepend:
     - EconomyShopGUI-OSS

For Maven / Gradle, add the plugin JAR as a ``compileOnly`` dependency
(it will be present on the server at runtime):

.. code-block:: kotlin

   // build.gradle.kts
   dependencies {
       compileOnly(files("libs/EconomyShopGUI-OSS.jar"))
   }

----

Getting the API instance
------------------------

.. code-block:: kotlin

   import io.github.Earth1283.economyShopGUIOSS.api.EconomyShopGUIHook

   val hook: EconomyShopGUIHook = EconomyShopGUIHook.get()
       ?: return  // plugin not loaded

   // Java:
   EconomyShopGUIHook hook = EconomyShopGUIHook.get();
   if (hook == null) return;

The instance is registered on plugin enable and cleared on disable.
Always call ``get()`` lazily (not in your plugin's constructor or static
block).

----

Reading shop data
-----------------

.. code-block:: kotlin

   // All enabled sections
   val sections: List<ApiShopSection> = hook.allSections()

   // Single section by ID
   val tools: ApiShopSection? = hook.getSection("tools")

   // Single item by section + item ID
   val sword: ApiShopItem? = hook.getItem("tools", "diamond_sword")

   // Full-text search across all sections
   val results: List<ApiShopItem> = hook.searchItems("diamond")

``ApiShopSection`` and ``ApiShopItem`` are immutable data classes.
Modifying the shop requires a YAML write + ``hook.reloadShop()``.

----

Executing transactions
----------------------

.. code-block:: kotlin

   import io.github.Earth1283.economyShopGUIOSS.transaction.TransactionResult

   // Buy 3 stacks from the "tools" section
   val result = hook.buy(player, "tools", "diamond_sword", quantity = 3)

   when (result) {
       is TransactionResult.Success ->
           player.sendMessage("Bought ${result.quantity}x sword for ${result.price}")
       is TransactionResult.Failure.NotEnoughMoney ->
           player.sendMessage("Need ${result.required}, have ${result.balance}")
       is TransactionResult.Failure.InventoryFull ->
           player.sendMessage("Inventory full!")
       else ->
           player.sendMessage("Transaction failed: $result")
   }

   // Sell 10 swords
   val sellResult = hook.sell(player, "tools", "diamond_sword", quantity = 10)

Transactions go through the full pipeline:

1. Permission check
2. Quantity bounds check
3. Requirements evaluation (quest, region, time)
4. Price modifier application
5. ``PreTransactionEvent`` (cancellable — other plugins can intervene)
6. Economy withdraw / deposit
7. Inventory give / take
8. ``PostTransactionEvent``
9. Transaction logging (SQLite)

----

Economy balance queries
-----------------------

.. code-block:: kotlin

   // Default economy (configured in config.yml)
   val balance: Double = hook.getBalance(player)

   // Specific economy type
   val xpBalance: Double = hook.getBalance(player, "xp")
   val vaultBalance: Double = hook.getBalance(player, "vault")

Valid economy type strings: ``"vault"``, ``"xp"``, ``"playerpoints"``,
``"gems"``.

----

Reloading the shop
------------------

.. code-block:: kotlin

   // Must be called on the server main thread
   hook.reloadShop()

This re-reads all YAML from disk and fires ``ShopItemsLoadEvent``.
Equivalent to ``/sreload``.

----

Listening to events
-------------------

See :doc:`events` for the full event reference.

.. code-block:: kotlin

   class MyListener : Listener {

       @EventHandler
       fun onShopLoad(event: ShopItemsLoadEvent) {
           plugin.logger.info("Shop loaded: ${event.sections.size} sections")
       }

       @EventHandler
       fun onPreBuy(event: PreTransactionEvent) {
           if (!event.type.isBuy) return
           // Give 5% discount to VIP players
           if (event.player.hasPermission("myplugin.vip")) {
               event.finalPrice *= 0.95
           }
       }

       @EventHandler
       fun onPostTransaction(event: PostTransactionEvent) {
           val success = event.result as? TransactionResult.Success ?: return
           analytics.record(event.player, event.item.sectionId, success.price)
       }
   }

----

ApiShopSection reference
-------------------------

.. list-table::
   :header-rows: 1
   :widths: 30 70

   * - Property
     - Description
   * - ``id: String``
     - Section ID (file name without ``.yml``).
   * - ``displayName: Component``
     - Rendered MiniMessage component of the section title.
   * - ``enabled: Boolean``
     - Whether this section is active.
   * - ``slot: Int``
     - Main-menu slot index.  ``-1`` if not in the main menu.
   * - ``hidden: Boolean``
     - Hidden sections are excluded from the main menu.
   * - ``items: List<ApiShopItem>``
     - All items across all pages.

----

ApiShopItem reference
---------------------

.. list-table::
   :header-rows: 1
   :widths: 30 70

   * - Property
     - Description
   * - ``id: String``
     - Item ID (key in the shop YAML).
   * - ``sectionId: String``
     - Parent section ID.
   * - ``page: Int``
     - 1-indexed page number.
   * - ``slot: Int``
     - Inventory slot index.
   * - ``displayName: Component``
     - Rendered display name.
   * - ``buyPrice: Double?``
     - Base buy price; ``null`` if not purchasable.
   * - ``sellPrice: Double?``
     - Base sell price; ``null`` if not sellable.
   * - ``economyType: String``
     - Economy backend: ``"vault"``, ``"xp"``, etc.
   * - ``stackSize: Int``
     - Items per buy/sell click.
   * - ``maxBuyQty: Int``
     - Maximum total quantity per purchase.  ``-1`` = unlimited.
   * - ``maxSellQty: Int``
     - Maximum total quantity per sale.  ``-1`` = unlimited.
   * - ``permission: String?``
     - Required permission node; ``null`` if unrestricted.

.. note::
   ``buyPrice`` and ``sellPrice`` are the **base** prices from YAML.
   Price modifiers (seasonal, discount, multiplier) are applied during an
   actual transaction.  Use ``hook.buy()`` / ``hook.sell()`` to get the
   modifier-adjusted result.
