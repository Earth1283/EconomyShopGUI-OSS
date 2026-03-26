Installation
============

Requirements
------------

.. list-table::
   :header-rows: 1
   :widths: 25 75

   * - Software
     - Minimum version
   * - PaperMC (or Folia)
     - 1.21
   * - Java
     - 21
   * - Vault *(optional)*
     - Any current release
   * - PlaceholderAPI *(optional)*
     - 2.11+

Building from source
--------------------

Clone the repository and build with the Gradle wrapper::

   git clone https://github.com/Earth1283/EconomyShopGUI-OSS.git
   cd EconomyShopGUI-OSS
   ./gradlew build

The output JAR is placed in ``build/libs/EconomyShopGUI-OSS-<version>.jar``.

.. note::
   The shadow JAR (fat JAR with all dependencies bundled) is built
   automatically as part of the ``build`` task. Always use the shadow JAR
   when deploying to a server — the plain JAR will not work on its own.

Running a test server
---------------------

The build includes the `run-paper <https://github.com/jpenilla/run-paper>`_
Gradle plugin for rapid local testing::

   ./gradlew runServer

This downloads a Paper 1.21 server to ``run/`` on first use and starts it with
the plugin's shadow JAR automatically installed.

Deploying to a production server
---------------------------------

1. Stop the server.
2. Copy ``build/libs/EconomyShopGUI-OSS-<version>.jar`` into the server's
   ``plugins/`` directory.
3. Install `Vault <https://www.spigotmc.org/resources/vault.34315/>`_ and an
   economy provider (e.g. EssentialsX) if you have not already.
4. Start the server. The plugin creates its configuration files in
   ``plugins/EconomyShopGUI-OSS/`` on first launch.
5. Edit ``config.yml`` and add shop sections as described in
   :doc:`configuration` and :doc:`shop-format`.
6. Run ``/sreload`` to apply any configuration changes without restarting.

Default data folder layout
---------------------------

After the first start the following files are created automatically::

   plugins/EconomyShopGUI-OSS/
   ├── config.yml           ← main plugin settings
   ├── lang-en.yml          ← English language strings
   ├── shops/               ← one .yml per shop section (items + pages)
   ├── sections/            ← one .yml per section (metadata + display)
   ├── layouts/             ← downloaded or installed layout templates
   └── stands.json          ← persisted shop-stand data (created on first stand)

Updating
--------

1. Stop the server.
2. Replace the old JAR with the new one.
3. Start the server. The plugin migrates ``config.yml`` automatically when
   ``config-version`` changes. Check the console for any migration warnings.
4. If you maintain a custom ``lang-*.yml``, compare it with the bundled
   ``lang-en.yml`` to find any newly added keys.

Uninstalling
------------

1. Stop the server and remove the JAR from ``plugins/``.
2. Optionally delete the ``plugins/EconomyShopGUI-OSS/`` data folder.
   This removes all shop sections, transaction logs, and stand data.
