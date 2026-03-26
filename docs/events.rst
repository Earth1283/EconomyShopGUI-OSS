Events
======

EconomyShopGUI-OSS fires Bukkit events at key points in the transaction lifecycle.
External plugins can listen to these events to modify prices, cancel transactions,
or log activity.

All events are in the ``io.github.Earth1283.economyShopGUIOSS.api.events`` package.

----

ShopItemsLoadEvent
------------------

Fired after every load or ``/sreload``.  Use this to read or modify the shop
state after YAML has been parsed.

.. code-block:: kotlin

   @EventHandler
   fun onShopLoad(event: ShopItemsLoadEvent) {
       val count = event.sections.sumOf { it.allItems.size }
       logger.info("Shop loaded with $count items")
   }

.. list-table::
   :header-rows: 1
   :widths: 25 75

   * - Property
     - Description
   * - ``sections``
     - Live list of all loaded ``ShopSection`` instances.

----

PreTransactionEvent
-------------------

Fired immediately before a buy or sell transaction executes.  **Cancellable.**
The ``finalPrice`` property can be mutated by listeners to override the computed price.

.. code-block:: kotlin

   @EventHandler
   fun onPreTransaction(event: PreTransactionEvent) {
       // Give VIP players a 10% discount on all purchases
       if (event.type.isBuy && event.player.hasPermission("shop.vip")) {
           event.finalPrice *= 0.90
       }
       // Block transactions during maintenance
       if (maintenanceMode) {
           event.isCancelled = true
           event.player.sendMessage("Shop is under maintenance.")
       }
   }

.. list-table::
   :header-rows: 1
   :widths: 25 75

   * - Property
     - Description
   * - ``player``
     - The player performing the transaction.
   * - ``item``
     - The ``ShopItem`` being bought or sold.
   * - ``type``
     - ``TransactionType`` (``Buy``, ``Sell``, ``BuyScreen``, etc.)
   * - ``quantity``
     - Number of items in the transaction.
   * - ``finalPrice``
     - Mutable final price.  Negative values are clamped to ``0.0``.
   * - ``isCancelled``
     - Set to ``true`` to abort the transaction.

----

PostTransactionEvent
--------------------

Fired after a transaction completes — whether it succeeded or failed.  **Not cancellable.**

.. code-block:: kotlin

   @EventHandler
   fun onPostTransaction(event: PostTransactionEvent) {
       when (val result = event.result) {
           is TransactionResult.Success -> {
               db.log(event.player, event.item, result.price)
           }
           is TransactionResult.Failure.NotEnoughMoney -> {
               analytics.track("buy_failed_funds", event.player)
           }
           else -> {}
       }
   }

.. list-table::
   :header-rows: 1
   :widths: 25 75

   * - Property
     - Description
   * - ``player``
     - The player who attempted the transaction.
   * - ``item``
     - The ``ShopItem`` involved.
   * - ``type``
     - ``TransactionType``.
   * - ``quantity``
     - Requested quantity.
   * - ``result``
     - ``TransactionResult.Success`` or a ``TransactionResult.Failure`` subtype.

TransactionResult subtypes
~~~~~~~~~~~~~~~~~~~~~~~~~~

.. list-table::
   :header-rows: 1
   :widths: 35 65

   * - Type
     - Meaning
   * - ``Success(price, quantity)``
     - Transaction completed. ``price`` is the total paid/received.
   * - ``Failure.NotEnoughMoney(required, balance)``
     - Player could not afford the purchase.
   * - ``Failure.InventoryFull``
     - No room in the player's inventory.
   * - ``Failure.NotEnoughItems(required, held)``
     - Player doesn't have enough items to sell.
   * - ``Failure.NoBuyPrice``
     - The item has no buy price configured.
   * - ``Failure.NoSellPrice``
     - The item has no sell price configured.
   * - ``Failure.RequirementNotMet(description)``
     - A quest/region/time requirement was not satisfied.
   * - ``Failure.Cancelled``
     - A ``PreTransactionEvent`` listener cancelled the transaction.
   * - ``Failure.EconomyUnavailable``
     - The required economy plugin is not loaded.
   * - ``Failure.InvalidQuantity(requested, min, max)``
     - Quantity is outside the item's configured limits.
   * - ``Failure.NoPermission``
     - Player lacks the item's required permission node.
   * - ``Failure.Unknown(reason)``
     - An unexpected error occurred.
