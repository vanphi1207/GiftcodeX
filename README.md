<img width="2048" height="441" alt="minecraft_title" src="https://github.com/user-attachments/assets/69e43a1a-b547-4401-a8b3-d272811f2726" />



 
# GiftcodeX

> Advanced gift code management plugin for Minecraft servers — GUI editor, PlaceholderAPI support, MySQL/H2 database, multi-language, Folia compatible.

<img width="1920" height="991" alt="2026-04-05_13 55 53" src="https://github.com/user-attachments/assets/43d946ad-18dd-4d73-9af3-f612d50225a8" />

<img width="1920" height="991" alt="2026-04-05_13 57 01" src="https://github.com/user-attachments/assets/d3f22ba4-888a-4bcd-ba29-201cc4f45d45" />


---

![Bstats](https://bstats.org/signatures/bukkit/GiftcodeX.svg)

## Table of Contents

- [Requirements](#requirements)
- [Installation](#installation)
- [Commands](#commands)
- [Permissions](#permissions)
- [Configuration](#configuration)
  - [config.yml](#configyml)
  - [messages\_en.yml / messages\_vi.yml](#messages_enyml--messages_viyml)
  - [codes.yml](#codesyml)
- [PlaceholderAPI](#placeholderapi)
- [ViaVersion Support](#viaversion-support)
- [Author](#author)
- [Support](#support)

---

## Requirements

| Requirement | Version |
|---|---|
| Java | 21+ |
| Paper / Folia | 1.21+ |
| PlaceholderAPI | 2.11+ *(optional)* |
| ViaVersion | Latest *(optional)* |

> **Note:** Spigot is **not** supported. You must use [Paper](https://papermc.io/downloads/paper) or [Folia](https://papermc.io/downloads/folia).

> **ViaVersion:** Optionally install [ViaVersion](https://www.spigotmc.org/resources/viaversion.19254/) to support players joining on older clients (below 1.21.6). The plugin automatically detects legacy clients and switches from Dialog input to chat input mode.

---

## Installation

1. Download the latest `GiftcodeX-x.x.jar` from [Releases](https://github.com/vanphi1207/GiftCodeX/releases/latest).
2. Place the `.jar` file into your server's `plugins/` folder.
3. Start or restart your server.
4. Configuration files are generated automatically in `plugins/GiftcodeX/`.
5. *(Optional)* Edit `config.yml` and your language file, then run `/gcx reload`.

---

## Commands

All admin commands require the `giftcodex.admin` permission. Players use `/redeem` to claim codes.

### Admin — `/giftcodex` (aliases: `/gcx`, `/gc`)

| Command | Description |
|---|---|
| `/gcx help` | Show command help |
| `/gcx create <code> [-g]` | Create a gift code. Add `-g` to open the GUI editor immediately |
| `/gcx delete <code>` | Permanently delete a code and purge all player records |
| `/gcx enable <code>` | Enable a disabled code |
| `/gcx disable <code>` | Disable a code without deleting it |
| `/gcx gui` | Browse all codes in the GUI (text list when run from console) |
| `/gcx edit <code>` | Open the GUI settings editor for a code |
| `/gcx items <code>` | Open the GUI item reward editor for a code |
| `/gcx assign <code> <player>` | Give a code's rewards directly to a player without consuming a use |
| `/gcx setperm <code> <perm\|none>` | Set or clear the required permission for a code |
| `/gcx info <code>` | Print detailed information about a code |
| `/gcx random <prefix> [amount] [-c <template>]` | Bulk-generate random codes. Use `-c <template>` to copy all settings from an existing code |
| `/gcx reload` | Reload `config.yml`, language file, and `codes.yml` without restarting |

### Player

| Command | Description |
|---|---|
| `/redeem <code>` | Redeem a gift code |

---

## Permissions

| Permission | Default | Description |
|---|---|---|
| `giftcodex.admin` | OP | Full access to all admin commands |
| `giftcodex.player` | Everyone | Allows use of `/redeem` |

> Custom permission nodes (e.g. `giftcodex.vip`) can be assigned to individual codes via `/gcx setperm` or the GUI editor. Any valid permission string is accepted.

---

## Configuration

### config.yml

```yaml
# Language for messages. Available: en, vi
# Add your own by creating messages_<lang>.yml in the plugin folder.
language: en

# Database backend: H2 (embedded, no setup needed) or MYSQL
database:
  type: H2
  mysql:
    host: localhost
    port: 3306
    database: giftcodex
    username: root
    password: ""

# Check for new releases on GitHub (notifies OPs on join)
check-update: true

# GUI settings
gui:
  reward-color: "&a"   # Colour prefix applied to reward messages in the item editor

# Default values applied when creating a new code via /gcx create
defaults:
  expiry: "2099-12-31T23:59:59"   # ISO-8601 datetime. Leave blank for no expiry.
  max-uses: 100                    # Global use limit
  player-max-uses: 1               # Per-player limit (-1 = unlimited)
  max-uses-per-ip: 1               # Per-IP limit (0 = disabled)
  required-playtime: 0             # Minutes of playtime required (0 = none)
  cooldown-seconds: 0              # Seconds between redemptions per player (0 = disabled)
```

### messages\_en.yml / messages\_vi.yml

Messages are stored in language files inside the plugin folder. Set `language: en` or `language: vi` in `config.yml` to choose which file is loaded. All messages support `&` colour codes and hex colours (`&#rrggbb`).

**Available keys and their placeholders:**

| Key | Placeholders | Description |
|---|---|---|
| `prefix` | — | Prepended to most admin messages |
| `infinity-symbol` | — | Symbol shown for unlimited / no-expiry values (default: `∞`) |
| `redeemed` | — | Shown to player on successful redemption |
| `invalid-code` | — | Code does not exist |
| `code-disabled` | — | Code is disabled |
| `code-expired` | — | Code has passed its expiry date |
| `max-uses-reached` | — | Global use limit exhausted |
| `already-redeemed` | — | Player has reached their personal use limit |
| `ip-limit-reached` | — | Too many redemptions from the same IP |
| `not-enough-playtime` | `{required}`, `{current}` | Player does not have enough playtime |
| `no-permission` | — | Player lacks the required permission node |
| `on-cooldown` | `{remaining}` | Player must wait before redeeming again. `{remaining}` is formatted as `1d 2h 3m 4s` |
| `assigned` | — | Shown to player when rewards are assigned via `/gcx assign` |
| `code-created` | `{code}` | Admin feedback after creating a code |
| `code-deleted` | `{code}` | Admin feedback after deleting a code |
| `code-enabled` | `{code}` | Admin feedback after enabling a code |
| `code-disabled-admin` | `{code}` | Admin feedback after disabling a code |
| `code-not-found` | `{code}` | Code not found during an admin command |
| `code-already-exists` | `{code}` | A code with that name already exists |
| `player-not-found` | `{player}` | Target player is offline or does not exist |
| `no-permission-admin` | — | Admin lacks `giftcodex.admin` |
| `plugin-reloaded` | — | Confirmation after `/gcx reload` |
| `random-generated` | `{amount}`, `{prefix}` | Confirmation after `/gcx random` |
| `permission-set` | `{code}`, `{permission}` | Confirmation after `/gcx setperm` |
| `permission-cleared` | `{code}` | Confirmation after `/gcx setperm <code> none` |
| `items-saved` | `{count}`, `{code}` | Confirmation after saving item rewards |

> Run `/gcx reload` after editing the language file — no restart needed.

### codes.yml

Codes are stored and managed automatically. You can also edit this file manually, then run `/gcx reload`.

```yaml
SUMMER24:
  commands:
    - "give %player% diamond 3"
    - "give %player% golden_apple 1"
  messages:
    - "&aYou received &e3 Diamonds &aand a &eGolden Apple&a!"
  max-uses: 1000
  expiry: "2024-08-31T23:59:59"
  enabled: true
  player-max-uses: 1
  max-uses-per-ip: 2
  cooldown-seconds: 0
  required-playtime:
    years: 0
    months: 0
    weeks: 0
    days: 0
    hours: 1
    minutes: 30
    seconds: 0
    milliseconds: 0
  permission: ""
  items: []
```

**Field reference:**

| Field | Type | Description |
|---|---|---|
| `commands` | List | Console commands executed on redemption. `%player%` is replaced with the player's name |
| `messages` | List | Messages sent to the player. Supports `&` colour codes |
| `max-uses` | Integer | Remaining global uses. Set to `999999999` for effectively unlimited |
| `expiry` | String | ISO-8601 expiry datetime (`yyyy-MM-dd'T'HH:mm:ss`). Leave empty for no expiry |
| `enabled` | Boolean | `true` / `false`. Disabled codes cannot be redeemed |
| `player-max-uses` | Integer | Per-player redemption limit. `-1` = unlimited |
| `max-uses-per-ip` | Integer | Per-IP redemption limit. `0` = disabled |
| `cooldown-seconds` | Long | Seconds a player must wait before redeeming this code again. `0` = disabled |
| `required-playtime` | Section | Minimum playtime using a duration model (`years`, `months`, `weeks`, `days`, `hours`, `minutes`, `seconds`, `milliseconds`). Plain integer values (minutes) from older versions are auto-migrated on reload |
| `permission` | String | Required permission node. Leave empty for none |
| `items` | List | ItemStack-serialized item rewards. Managed via `/gcx items <code>` or the GUI |

**Redemption check order:**

When a player runs `/redeem <code>`, the plugin checks conditions in this exact order:

1. Code exists
2. Code is enabled
3. Code has not expired
4. Player has the required permission
5. Player has enough playtime
6. Player has not exceeded their personal use limit
7. **Player is not on cooldown** ← stops here if cooldown has not elapsed, shows `{remaining}`
8. Player's IP has not exceeded the IP limit
9. All checks passed → execute commands, send messages, give items

---

## PlaceholderAPI

Install [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) to use these placeholders anywhere on your server (scoreboards, chat, holograms, etc.).

### Global placeholders

| Placeholder | Description |
|---|---|
| `%giftcodex_total_codes%` | Total number of registered gift codes |
| `%giftcodex_player_totalused%` | Number of distinct codes the current player has redeemed |

### Per-code placeholders

Replace `<code>` with the actual code name (e.g. `%giftcodex_code_enabled_SUMMER24%`).

| Placeholder | Description |
|---|---|
| `%giftcodex_code_exists_<code>%` | `true` / `false` — whether the code exists |
| `%giftcodex_code_enabled_<code>%` | `true` / `false` — whether the code is enabled |
| `%giftcodex_code_expired_<code>%` | `true` / `false` — whether the code has expired |
| `%giftcodex_code_maxuses_<code>%` | Remaining global uses |
| `%giftcodex_code_used_<code>%` | Total redemptions across all players |
| `%giftcodex_code_expiry_<code>%` | Expiry datetime string, or the configured infinity symbol |
| `%giftcodex_code_permission_<code>%` | Required permission node, or `None` |
| `%giftcodex_code_playtime_<code>%` | Required playtime as a human-readable string (e.g. `1d 2h 30m`) |
| `%giftcodex_code_playtime_minutes_<code>%` | Required playtime converted to total minutes |
| `%giftcodex_code_playtime_ms_<code>%` | Required playtime in milliseconds |
| `%giftcodex_code_playeruses_<code>%` | Times *the current player* has redeemed this code |
| `%giftcodex_code_canuseip_<code>%` | `true` / `false` — whether the current player's IP is still under the limit |
| `%giftcodex_code_playerlimit_<code>%` | Per-player use limit, or the infinity symbol if unlimited |
| `%giftcodex_code_iplimit_<code>%` | Per-IP use limit, or the infinity symbol if disabled |
| `%giftcodex_code_cooldown_<code>%` | Total cooldown duration in seconds (`0` if disabled) |
| `%giftcodex_code_cooldownleft_<code>%` | Time remaining on cooldown for the current player (e.g. `22h 15m 3s`), or `0s` if not on cooldown |

---

## ViaVersion Support

When [ViaVersion](https://www.spigotmc.org/resources/viaversion.19254/) is installed, GiftcodeX automatically detects each admin's client version:

- **1.21.6+ clients** use the native **Dialog** input (a popup form with labeled fields).
- **Legacy clients (below 1.21.6)** fall back to **chat input** mode automatically.

The GUI editor displays a hint in each item's lore indicating which input mode is active for the current viewer (`dialog input` or `chat input`). No configuration is required.

---

## Author

| | |
|---|---|
| **Name** | ihqqq |
| **Facebook** | [facebook.com/ihqqqq](https://www.facebook.com/ihqqqq/) |

---

## Support

- **Issues & bug reports:** [GitHub Issues](https://github.com/vanphi1207/GiftCodeX/issues)
- **Discord server:** [discord.gg/YQtCC7BV](https://discord.gg/YQtCC7BV)
