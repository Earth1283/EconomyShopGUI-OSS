Shop Stands
===========

Shop stands are physical blocks in the world that open a shop section when
right-clicked.  They provide an immersive alternative to the ``/shop`` command.

Stands are persisted in ``stands.yml`` inside the plugin data folder and
survive server restarts.

----

Creating a Stand
----------------

Use the ``/eshop shopstands`` sub-menu or the command line:

.. code-block:: text

   /eshop additem <section> <page> <slot>

Then right-click the block you want to become a stand.  Alternatively, place
a stand at your current target block:

.. code-block:: text

   /eshop shopstands place <section> [item]

----

Removing a Stand
----------------

Right-click a stand block while sneaking, or use:

.. code-block:: text

   /eshop shopstands remove

while looking at the stand.

----

Listing Stands
--------------

.. code-block:: text

   /eshop shopstands list

Outputs a list of all stands with their IDs, sections, and world coordinates.

----

Persistence Format
------------------

Stands are stored in ``plugins/EconomyShopGUI-OSS/stands.yml``:

.. code-block:: yaml

   stands:
     550e8400-e29b-41d4-a716-446655440000:
       section: tools
       item: diamond_sword      # optional — omit to open the section
       world: world
       x: 100
       y: 64
       z: 200

Each entry uses a UUID as the key to prevent key collisions if multiple stands
share the same section.

----

Configuration
-------------

Enable or disable stands globally in ``config.yml``:

.. code-block:: yaml

   shop-stands:
     enabled: true
     holograms: true      # requires HolographicDisplays or DecentHolograms (future)
