# EconomyShopGUI-OSS

> Open-source, fully-featured GUI shop plugin for PaperMC — buy, sell, and manage items through a clean inventory interface with powerful admin tooling.

---

## ✨ Features

### 🛒 Shop GUI
- Paginated section menus — unlimited sections, unlimited pages per section
- One-click **buy** and **sell** with configurable stack sizes and quantity caps
- `/sellall` — bulk-sell from your full inventory, just your hand, or by item type
- `/sellgui` — drag-and-drop sell screen; Shulker Box contents counted automatically
- Middle-click any shop item to instant-sell all matching items from your inventory

### 💰 Economy Backends
| Backend | Requirement |
|---------|------------|
| **Vault** *(default)* | Any Vault-compatible economy (EssentialsX, CMI, etc.) |
| **XP** | Built-in — no extra plugins needed |
| **PlayerPoints** | PlayerPoints plugin |
| **GemsEconomy** | GemsEconomy plugin |

### 📊 Price Modifiers
- **Seasonal prices** — set spring / summer / fall / winter multipliers per item or section; optionally driven by the **RealisticSeasons** plugin
- **Per-section discounts and multipliers**
- **Global price multipliers**

### 🔒 Item Requirements
- **Quest completion** — requires a quest to be finished (Quests plugin)
- **WorldGuard region** — restrict an item to players standing in a named region
- **Time of day** — only purchasable during a configured in-game hour window

### 🏪 Physical Shop Stands
Place a real in-world shop stand (ArmorStand entity) that opens a shop section when right-clicked. Stands survive server restarts.

### 🛠️ Admin Tooling
- **`/eshop additem`** / **`edititem`** / **`delitem`** — live item management without reloading
- **`/eshop addsection`** / **`delsection`** / **`setslot`** — section management
- **`/eshop log`** — query the full SQLite transaction log
- **`/eshop install <code>`** — download and install a community layout from the marketplace
- **`/eshop upload`** — share your shop layout with the community
- **`/sreload`** — hot-reload all YAML configuration without restarting the server

### 🔌 Integrations
- **PlaceholderAPI** — expose balances, buy prices, and sell prices to any PAPI-compatible plugin
- **Geyser / Floodgate** — inventory automatically resized for Bedrock Edition players
- **bStats** — anonymous usage metrics (can be disabled)
- **Update checker** — notifies ops on join when a new version is available

### 👩‍💻 Developer API
```kotlin
val hook = EconomyShopGUIHook.get() ?: return

// Read shop data
val section = hook.getSection("tools")
val item    = hook.getItem("tools", "diamond_sword")

// Execute a transaction
val result = hook.buy(player, "tools", "diamond_sword", quantity = 3)

// Listen to events
@EventHandler
fun onPreBuy(event: PreTransactionEvent) {
    if (player.hasPermission("myplugin.vip")) event.finalPrice *= 0.95
}
```
Full API reference: [docs/api.rst](docs/api.rst)

---

## 📦 Installation

1. Install [Vault](https://www.spigotmc.org/resources/vault.34315/) and an economy provider (e.g. **EssentialsX**).
2. Drop `EconomyShopGUI-OSS-<version>.jar` into your `plugins/` folder.
3. Start the server — config files are created automatically under `plugins/EconomyShopGUI-OSS/`.
4. Add sections by creating YAML files in `plugins/EconomyShopGUI-OSS/sections/` and `shops/`.
5. Run `/sreload` and open with `/shop`.

### Requirements

| | Minimum |
|---|---|
| Server | PaperMC **1.21** (or Folia) |
| Java | **21** |
| Economy | Vault + any economy provider *(XP requires nothing extra)* |

---

## ⌨️ Commands

| Command | Permission | Description |
|---------|-----------|-------------|
| `/shop [section] [page]` | `economyshopgui.shop` | Open the shop (or jump to a section) |
| `/sellall [inventory\|item\|hand]` | `economyshopgui.sellall` | Bulk sell from inventory |
| `/sellgui` | `economyshopgui.sellgui` | Drag-and-drop sell screen |
| `/shopgive <item> <player>` | `economyshopgui.admin` | Give a shop item to a player |
| `/sreload` | `economyshopgui.admin` | Reload all configuration |
| `/eshop <subcommand>` | `economyshopgui.admin` | In-game editor and admin tools |

---

## 🎨 Text Formatting

All configuration text uses [MiniMessage](https://docs.advntr.dev/minimessage) syntax — gradients, hover events, click events, and more. Zero legacy `§`/`&` color codes.

```yaml
name: "<gradient:gold:yellow>Premium Tools</gradient>"
lore:
  - "<gray>Buy:  <gold><buy_price>"
  - "<gray>Sell: <gold><sell_price>"
```

---

## 📖 Documentation

Full documentation is included in the [docs/](docs/) directory and covers every configuration key, command, event, placeholder, and the complete developer API.

---

## 🤝 Contributing

Source is on [GitHub](https://github.com/Earth1283/EconomyShopGUI-OSS). PRs welcome — see [docs/contributing.rst](docs/contributing.rst).

Build from source:
```bash
git clone https://github.com/Earth1283/EconomyShopGUI-OSS.git
cd EconomyShopGUI-OSS
./gradlew build        # produces the shadow JAR in build/libs/
./gradlew runServer    # spins up a local Paper 1.21 test server
```
