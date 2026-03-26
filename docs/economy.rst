Economy
=======

EconomyShopGUI-OSS supports multiple economy backends.  Each shop section can
use a different economy type; the default is configured in ``config.yml``.

Supported types
---------------

.. list-table::
   :header-rows: 1
   :widths: 20 25 55

   * - Type key
     - Required plugin
     - Notes
   * - ``vault``
     - `Vault <https://www.spigotmc.org/resources/vault.34315/>`_ + any provider
     - The standard multi-economy abstraction.  Works with EssentialsX, CMI,
       and dozens of other economy plugins.
   * - ``xp``
     - None
     - Uses Minecraft experience points.  Always available.  Point values are
       derived from total accumulated XP (level + progress bar).
   * - ``playerpoints``
     - `PlayerPoints <https://www.spigotmc.org/resources/playerpoints.80745/>`_
     - Integer-only point values.  Decimal amounts are truncated.
   * - ``gems``
     - `GemsEconomy <https://github.com/Xanium/GemsEconomy>`_
     - Uses the default currency configured in GemsEconomy.

Setting the default economy
----------------------------

In ``config.yml``::

   economy: vault

This is used for any section that does not declare its own ``economy:`` key.

Per-section economy
-------------------

Override the economy for a specific section in ``sections/<name>.yml``::

   economy: xp

This means players pay XP to buy from this section even if the server default
is Vault.

Price formatting
----------------

Prices are formatted according to two settings in ``config.yml``:

.. code-block:: yaml

   locale: en-US            # Java locale tag for decimal/thousands separator
   currency-format: "#,##0.00"   # Java DecimalFormat pattern

Examples:

.. list-table::
   :header-rows: 1
   :widths: 20 20 60

   * - Locale
     - Pattern
     - Output for 1234.5
   * - ``en-US``
     - ``#,##0.00``
     - ``1,234.50``
   * - ``de-DE``
     - ``#.##0,00``
     - ``1.234,50``
   * - ``en-US``
     - ``0``
     - ``1235`` (truncated)

Vault additionally prepends the configured currency symbol (e.g. ``$``).

Price abbreviations
-------------------

Large prices are abbreviated to improve readability:

.. code-block:: yaml

   abbreviations:
     enabled: true
     thresholds:
       k: 1_000
       m: 1_000_000
       b: 1_000_000_000
       t: 1_000_000_000_000
       q: 1_000_000_000_000_000

Examples: ``1500`` → ``1.5k``, ``2000000`` → ``2m``.

Set ``enabled: false`` to always show the full number.

Vault setup
-----------

1. Download `Vault <https://www.spigotmc.org/resources/vault.34315/>`_ and
   place it in ``plugins/``.
2. Install an economy provider.  Common choices:

   - **EssentialsX** — ``/bal``, ``/pay`` and full economy via Vault.
   - **CMI** — built-in Vault economy.
   - **iConomy** — lightweight, Vault-compatible.

3. Restart the server.  EconomyShopGUI-OSS will detect Vault automatically.

XP economy notes
----------------

* The "balance" shown is total accumulated XP, not just the current bar.
* Purchases deduct XP from the player's total; if this reduces the level
  display it is intentional and correct.
* Deposits use ``Player.giveExp()`` with true-experience mode so levels scale
  correctly.

Troubleshooting
---------------

**"Economy type Vault is not available. Falling back to XP economy."**
   Vault is installed but no economy provider has registered with it.
   Check that your economy plugin (e.g. EssentialsX) loaded without errors.

**Prices shown as raw numbers without currency symbol**
   The economy plugin's ``format()`` method is returning plain numbers.
   Check the economy plugin's own configuration for the currency symbol setting.
