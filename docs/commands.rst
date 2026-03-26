Commands
========

All commands use MiniMessage-formatted feedback messages that can be
customised in ``lang-<language>.yml``.

----

/shop
-----

Open the main shop menu or jump directly to a section.

.. code-block:: text

   /shop
   /shop <section>
   /shop <section> <page>

.. list-table::
   :header-rows: 1
   :widths: 30 70

   * - Argument
     - Description
   * - ``<section>``
     - Section ID as defined in ``sections/<id>.yml``.
   * - ``<page>``
     - 1-indexed page number within the section.

**Permission:** ``economyshopgui.shop`` (default: all players)

----

/sellall
--------

Sell items from the player's inventory in bulk.

.. code-block:: text

   /sellall
   /sellall inventory
   /sellall hand

.. list-table::
   :header-rows: 1
   :widths: 20 80

   * - Mode
     - Behaviour
   * - *(none)* / ``inventory``
     - Sell every sellable item across the full inventory.
   * - ``hand``
     - Sell only the item currently held in the main hand.

**Permission:** ``economyshopgui.sellall`` (default: all players)

----

/sellgui
--------

Open the drag-and-drop sell GUI.  Players drag items from their inventory
into the top area; items are sold automatically when the inventory is closed
or the confirm button is clicked.  Unsellable items are returned.

.. code-block:: text

   /sellgui

**Permission:** ``economyshopgui.sellgui`` (default: all players)

----

/shopgive
---------

Give a shop item directly to a player's inventory.  Admin-only.

.. code-block:: text

   /shopgive <section.item> <player> [qty]

**Example:**

.. code-block:: text

   /shopgive tools.diamond_sword Steve 5

**Permission:** ``economyshopgui.admin`` (default: op)

----

/sreload
--------

Reload ``config.yml``, the language file, and all shop YAML without
restarting the server.  Active GUI sessions are not closed automatically;
players see the new data the next time they open a screen.

.. code-block:: text

   /sreload

**Permission:** ``economyshopgui.admin``

----

/eshop
------

In-game shop editor with subcommands.  All changes are persisted to YAML
and the shop is reloaded automatically.

.. code-block:: text

   /eshop help
   /eshop additem <section> <page> <slot> [buy] [sell]
   /eshop edititem <section> <item> <buy|sell|slot> <value>
   /eshop delitem <section> <item>
   /eshop addsection <id> [slot]
   /eshop delsection <id>
   /eshop setslot <section> <slot>
   /eshop reload
   /eshop log [limit]
   /eshop shopstands

.. list-table::
   :header-rows: 1
   :widths: 35 65

   * - Subcommand
     - Description
   * - ``additem``
     - Adds the item in your main hand to a section at the given page/slot.
   * - ``edititem``
     - Modify ``buy``, ``sell``, or ``slot`` of an existing item.
   * - ``delitem``
     - Remove an item from a section.
   * - ``addsection``
     - Create a new section YAML file.
   * - ``delsection``
     - Delete a section and its shop file.
   * - ``setslot``
     - Move a section icon to a different main-menu slot.
   * - ``reload``
     - Alias for ``/sreload``.
   * - ``log [limit]``
     - Display the last N transaction records (default 20).
   * - ``shopstands``
     - Manage physical shop stands in the world.

**Permission:** ``economyshopgui.admin``

----

Permissions
-----------

.. list-table::
   :header-rows: 1
   :widths: 40 40 20

   * - Node
     - Description
     - Default
   * - ``economyshopgui.admin``
     - Full access to all admin commands.
     - op
   * - ``economyshopgui.shop``
     - Use ``/shop``.
     - true
   * - ``economyshopgui.sellall``
     - Use ``/sellall``.
     - true
   * - ``economyshopgui.sellgui``
     - Use ``/sellgui``.
     - true

Child permissions (granted automatically by ``economyshopgui.admin``):
``economyshopgui.shop``, ``economyshopgui.sellall``, ``economyshopgui.sellgui``.
