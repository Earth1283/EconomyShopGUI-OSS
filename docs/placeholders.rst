Placeholders
============

EconomyShopGUI-OSS provides a `PlaceholderAPI`_ expansion when PlaceholderAPI
is installed.  All placeholders use the identifier ``economyshopgui``.

.. _PlaceholderAPI: https://www.spigotmc.org/resources/placeholderapi.6245/

Registering
-----------

The expansion registers automatically on startup when PlaceholderAPI is present.
No ``/papi ecloud`` command is needed.

----

Available Placeholders
----------------------

.. list-table::
   :header-rows: 1
   :widths: 50 50

   * - Placeholder
     - Description
   * - ``%economyshopgui_balance%``
     - Player's balance in the default economy, formatted with the
       configured currency format.
   * - ``%economyshopgui_buy_<section>_<item>%``
     - Current buy price of the item (after modifiers), formatted.
       Returns ``N/A`` if the item has no buy price.
   * - ``%economyshopgui_sell_<section>_<item>%``
     - Current sell price of the item (after modifiers), formatted.
       Returns ``N/A`` if the item has no sell price.
   * - ``%economyshopgui_section_count%``
     - Number of currently enabled sections.
   * - ``%economyshopgui_item_count%``
     - Total number of items across all enabled sections.

Examples
--------

.. code-block:: text

   Your balance: %economyshopgui_balance%
   Diamond buy price: %economyshopgui_buy_materials_diamond%
   Diamond sell price: %economyshopgui_sell_materials_diamond%
   Total shop items: %economyshopgui_item_count%

Notes
-----

- The ``<section>`` and ``<item>`` parts use an underscore separator because
  PlaceholderAPI splits on ``%``.  If your section or item ID contains
  underscores, the first underscore after the prefix is used as the delimiter.
- Prices reflect the base price; per-player price modifiers (if any) are not
  applied in PAPI placeholders.
- Values are resolved synchronously on the calling thread.  For high-frequency
  scoreboards, consider setting ``placeholder-cache-seconds`` in ``config.yml``
  to cache the results.
