Contributing
============

Development setup
-----------------

Prerequisites: Java 21, Git.
No separate Kotlin installation is needed — the Gradle wrapper handles it.

::

   git clone https://github.com/Earth1283/EconomyShopGUI-OSS.git
   cd EconomyShopGUI-OSS
   ./gradlew build          # compile + run tests + produce shadow JAR
   ./gradlew runServer      # start a local Paper 1.21 test server

The project is set up for IntelliJ IDEA.  Open the root directory as a Gradle
project; IntelliJ will automatically configure the Kotlin/JVM toolchain.

Code style guide
----------------

Language
~~~~~~~~

* **Kotlin only.** No Java source files.
* Target JVM 21; use language features up to Kotlin 2.x freely.

Text and formatting
~~~~~~~~~~~~~~~~~~~

* **MiniMessage everywhere.** Never use ``ChatColor``, ``§`` codes, or
  ``&``-style legacy codes.  The only text entry point is
  ``ColorUtils.parse(String): Component``.
* Store user-visible strings as MiniMessage in ``lang-en.yml`` and the
  :class:`Lang` enum — never hard-code them at call sites.

Immutability and data classes
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* All domain model types (``ShopItem``, ``ShopSection``, etc.) must be
  ``data class`` with ``val`` properties.
* Compute derived values (e.g. effective price after modifiers) via methods,
  not by mutating the model.

Sealed classes for variants
~~~~~~~~~~~~~~~~~~~~~~~~~~~

Use ``sealed class`` / ``sealed interface`` for any type that has a known,
closed set of subtypes::

   sealed class TransactionResult {
       data class Success(val earned: Double) : TransactionResult()
       sealed class Failure : TransactionResult() {
           object InsufficientFunds : Failure()
           object Cancelled         : Failure()
           object InventoryFull     : Failure()
       }
   }

This eliminates ``else`` branches and makes exhaustive ``when`` expressions
the default, so newly added subtypes cause compile errors at every call site.

Manager and singleton pattern
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* Plugin-scoped managers are ``by lazy`` properties on
  :class:`EconomyShopGUIOSS`.
* Stateless utilities (``ColorUtils``, ``SchedulerUtils``) are Kotlin
  ``object`` singletons.
* Avoid passing the plugin instance deep into business logic.  Prefer explicit
  constructor injection of the specific manager a class needs.

Scheduling
~~~~~~~~~~

* Use :class:`SchedulerUtils` for all async/sync work.  Never touch
  ``BukkitScheduler`` or Folia APIs directly.
* Reserve ``kotlinx.coroutines`` (``Dispatchers.IO``) for pure I/O:
  HTTP calls, SQLite writes, file exports.  Do not use coroutines for
  any code that reads or writes Minecraft world state.

Extension functions
~~~~~~~~~~~~~~~~~~~

Prefer extension functions over utility classes with static methods::

   // Good
   fun ItemStack.isSimilarIgnoringLore(other: ItemStack): Boolean { … }

   // Avoid
   object ItemUtils {
       @JvmStatic fun isSimilarIgnoringLore(a: ItemStack, b: ItemStack): Boolean { … }
   }

Commit and PR conventions
--------------------------

* Commits: imperative mood, present tense — ``Add XPEconomy integration``,
  not ``Added XP economy``.
* Each PR should address a single feature or bug fix.
* Run ``./gradlew build`` before opening a PR; CI will reject failing builds.
* Add or update RST documentation in ``docs/`` for any user-visible change.

Documentation
-------------

All docs live in ``docs/`` and use
`reStructuredText <https://www.sphinx-doc.org/en/master/usage/restructuredtext/>`_
compatible with Sphinx.

Build locally with Sphinx (optional)::

   pip install sphinx furo
   sphinx-build -b html docs docs/_build/html
   open docs/_build/html/index.html

When adding a new ``.rst`` file, add it to the ``toctree`` in ``docs/index.rst``.

Reporting issues
----------------

Use the GitHub issue tracker at
``https://github.com/Earth1283/EconomyShopGUI-OSS/issues``.

Please include:

* Server software and version (e.g. Paper 1.21.1 build 123)
* Java version (``java -version``)
* Plugin version
* Relevant ``config.yml`` and shop YAML snippets
* Full stack trace from ``latest.log`` if applicable
