# GiftcodeX

> Advanced gift code management plugin for Minecraft servers — GUI editor, PlaceholderAPI support, MySQL/H2 database, Folia compatible.

<img width="1920" height="991" alt="image" src="https://github.com/user-attachments/assets/2371861d-6144-4954-9915-5283708a37e9" />
<img width="1920" height="991" alt="image" src="https://github.com/user-attachments/assets/555bdb81-29e7-4b5c-a94b-59ddf95e665f" />


---

## Table of Contents

- [Requirements](#requirements)
- [Installation](#installation)
- [Commands](#commands)
- [Permissions](#permissions)
- [Configuration](#configuration)
    - [config.yml](#configyml)
    - [messages.yml](#messagesyml)
    - [codes.yml](#codesyml)
- [PlaceholderAPI](#placeholderapi)
- [Author](#author)
- [Support](#support)

---

## Requirements

| Requirement | Version |
|---|---|
| Java | 21+ |
| Paper / Folia | 1.21.6+ |
| PlaceholderAPI | 2.11+ *(optional)* |

> **Note:** Spigot is **not** supported. You must use [Paper](https://papermc.io/downloads/paper) or [Folia](https://papermc.io/downloads/folia).

---

## Installation

1. Download the latest `GiftcodeX-x.x.jar` from [Releases](https://github.com/vanphi1207/GiftCodeX/releases/latest).
2. Place the `.jar` file into your server's `plugins/` folder.
3. Start or restart your server.
4. Configuration files are generated automatically in `plugins/GiftcodeX/`.
5. *(Optional)* Edit `config.yml` and `messages.yml`, then run `/gcx reload`.

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
| `/gcx list` | List all codes (opens GUI for players, text list for console) |
| `/gcx edit <code>` | Open the GUI settings editor for a code |
| `/gcx items <code>` | Open the GUI item reward editor for a code |
| `/gcx assign <code> <player>` | Give a code's rewards directly to a player |
| `/gcx setperm <code> <perm\|none>` | Set or clear the required permission for a code |
| `/gcx info <code>` | Print detailed information about a code |
| `/gcx random <prefix> [amount] [-c <template>]` | Bulk-generate random codes. Use `-c <template>` to copy settings from an existing code |
| `/gcx reload` | Reload `config.yml`, `messages.yml`, and `codes.yml` without restarting |

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
| `giftcodex.vip` | OP | Example custom permission for VIP-restricted codes |

---

## Configuration

### config.yml

```yaml
# Database type: H2 (embedded, no setup needed) or MYSQL
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
  reward-color: "&a"   # Colour prefix applied to reward messages

# Default values applied when creating a new code
defaults:
  expiry: "2099-12-31T23:59:59"   # Leave blank for no expiry
  max-uses: 100                    # Global use limit
  player-max-uses: 1               # Per-player limit (-1 = unlimited)
  max-uses-per-ip: 1               # Per-IP limit (0 = disabled)
  required-playtime: 0             # Minutes of playtime required (0 = none)
```

### messages.yml

All messages support `&` colour codes.
```yaml
prefix: "&8[&bGiftcodeX&8] "

redeemed:            "&aCode redeemed successfully!"
invalid-code:        "&cThis gift code does not exist."
code-disabled:       "&cThis gift code is currently disabled."
code-expired:        "&cThis gift code has expired."
max-uses-reached:    "&cThis gift code has reached its global usage limit."
already-redeemed:    "&cYou have already used this code the maximum allowed number of times."
ip-limit-reached:    "&cThis gift code has been used too many times from your IP address."
not-enough-playtime: "&cYou need at least &e{required} &cminutes of playtime. &7(You have: &e{current} &7min)"
no-permission:       "&cYou do not have permission to use this gift code."
```

> Run `/gcx reload` after editing messages.yml — no restart needed.

### codes.yml

Codes are stored and managed automatically. You can also edit this file manually.

```yaml
SUMMER24:
  commands:
    - "give %player% diamond 3"
    - "give %player% golden_apple 1"
  messages:
    - "&aYou received &e3 Diamonds &aand a &eGolden Apple&a!"
  max-uses: 1000               # Remaining global uses
  expiry: "2024-08-31T23:59:59"
  enabled: true
  player-max-uses: 1
  max-uses-per-ip: 2
  required-playtime: 0        # 0 = no requirement
  permission: ""              # Leave blank for no permission required
  items: []                   # Set item rewards via /gcx items <code>
```

## PlaceholderAPI

Install [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) to use the following placeholders anywhere on your server.

| Placeholder | Description |
|---|---|
| `%giftcodex_total_codes%` | Total number of registered gift codes |
| `%giftcodex_player_totalused%` | Number of codes the player has redeemed |
| `%giftcodex_code_exists_<code>%` | `true` / `false` — whether the code exists |
| `%giftcodex_code_enabled_<code>%` | `true` / `false` — whether the code is enabled |
| `%giftcodex_code_expired_<code>%` | `true` / `false` — whether the code has expired |
| `%giftcodex_code_maxuses_<code>%` | Remaining global uses |
| `%giftcodex_code_used_<code>%` | Total times the code has been redeemed (all players) |
| `%giftcodex_code_expiry_<code>%` | Expiry date string, or `Never` |
| `%giftcodex_code_permission_<code>%` | Required permission, or `None` |
| `%giftcodex_code_playtime_<code>%` | Required playtime in minutes |
| `%giftcodex_code_playeruses_<code>%` | Times *this player* has redeemed the code |
| `%giftcodex_code_canuseip_<code>%` | `true` / `false` — whether the player's IP is still under the limit |

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


