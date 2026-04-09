package me.ihqqq.giftcodeX.command;

import me.ihqqq.giftcodeX.GiftcodeX;
import me.ihqqq.giftcodeX.gui.CodeEditorGUI;
import me.ihqqq.giftcodeX.gui.CodeListGUI;
import me.ihqqq.giftcodeX.gui.ItemEditorGUI;
import me.ihqqq.giftcodeX.model.Giftcode;
import me.ihqqq.giftcodeX.model.PlaytimeDuration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class GiftcodeCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of(
            "help", "create", "delete", "enable", "disable", "gui",
            "edit", "items", "assign", "setperm", "info", "random", "reload"
    );

    private final GiftcodeX plugin;

    public GiftcodeCommand(GiftcodeX plugin) {
        this.plugin = plugin;
    }


    private String inf() {
        return plugin.getMessageConfig().getInfinitySymbol();
    }

    private String displayMaxUses(int maxUses) {
        return maxUses >= Integer.MAX_VALUE ? inf() : String.valueOf(maxUses);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub = args.length == 0 ? "help" : args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("help")) { sendHelp(sender); return true; }

        if (!sender.hasPermission("giftcodex.admin")) {
            sender.sendMessage(msg("no-permission-admin"));
            return true;
        }

        switch (sub) {
            case "create"           -> handleCreate(sender, args);
            case "delete", "del"    -> handleDelete(sender, args);
            case "enable"           -> handleSetEnabled(sender, args, true);
            case "disable"          -> handleSetEnabled(sender, args, false);
            case "gui", "list"      -> handleGui(sender);
            case "edit"             -> handleEdit(sender, args);
            case "items"            -> handleItems(sender, args);
            case "assign"           -> handleAssign(sender, args);
            case "setperm"          -> handleSetPerm(sender, args);
            case "info"             -> handleInfo(sender, args);
            case "random"           -> handleRandom(sender, args);
            case "reload"           -> handleReload(sender);
            default                 -> sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /gcx help.");
        }
        return true;
    }


    private void handleCreate(CommandSender sender, String[] args) {
        if (args.length < 2) { sender.sendMessage(usage("/gcx create <code> [-g]")); return; }
        String code = args[1];
        if (plugin.getCodeManager().exists(code)) {
            sender.sendMessage(msg("code-already-exists", Map.of("code", code)));
            return;
        }
        boolean openGui = args.length >= 3 && "-g".equalsIgnoreCase(args[2]);
        Giftcode newCode = new Giftcode.Builder(code)
                .commands(List.of("give %player% diamond 1"))
                .messages(List.of("&aYou received &e1 Diamond&a!"))
                .maxUses(plugin.getConfigManager().getDefaultMaxUses())
                .expiry(plugin.getConfigManager().getDefaultExpiry())
                .playerMaxUses(plugin.getConfigManager().getDefaultPlayerMaxUses())
                .maxUsesPerIp(plugin.getConfigManager().getDefaultMaxUsesPerIp())
                .requiredPlaytime(PlaytimeDuration.zero())
                .enabled(true)
                .build();
        plugin.getCodeManager().create(newCode);
        sender.sendMessage(msg("code-created", Map.of("code", code)));
        if (openGui && sender instanceof Player player) new CodeEditorGUI(plugin, player, newCode).open();
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) { sender.sendMessage(usage("/gcx delete <code>")); return; }
        String code = args[1];
        if (!plugin.getCodeManager().delete(code)) {
            sender.sendMessage(msg("code-not-found", Map.of("code", code)));
        } else {
            sender.sendMessage(msg("code-deleted", Map.of("code", code)));
        }
    }

    private void handleSetEnabled(CommandSender sender, String[] args, boolean enabled) {
        if (args.length < 2) {
            sender.sendMessage(usage("/gcx " + (enabled ? "enable" : "disable") + " <code>"));
            return;
        }
        String code = args[1];
        plugin.getCodeManager().find(code).ifPresentOrElse(
                gc -> {
                    plugin.getCodeManager().update(gc.withEnabled(enabled));
                    sender.sendMessage(msg(enabled ? "code-enabled" : "code-disabled-admin", Map.of("code", code)));
                },
                () -> sender.sendMessage(msg("code-not-found", Map.of("code", code)))
        );
    }

    private void handleGui(CommandSender sender) {
        if (sender instanceof Player player) {
            new CodeListGUI(plugin, player).open(0);
            return;
        }
        List<String> codes = plugin.getCodeManager().listCodes();
        sender.sendMessage(ChatColor.GOLD + "─── Gift codes (" + codes.size() + ") ───");
        for (String code : codes) {
            plugin.getCodeManager().find(code).ifPresent(gc -> {
                int used   = plugin.getCodeManager().globalUseCount(code);
                String status = gc.isEnabled() ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF";
                sender.sendMessage(ChatColor.YELLOW + code
                        + ChatColor.GRAY + " | uses: " + ChatColor.AQUA + displayMaxUses(gc.getMaxUses())
                        + ChatColor.GRAY + " | used: " + ChatColor.AQUA + used
                        + ChatColor.GRAY + " | status: " + status);
            });
        }
    }

    private void handleEdit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage(ChatColor.RED + "Only players can open the GUI editor."); return; }
        if (args.length < 2) { sender.sendMessage(usage("/gcx edit <code>")); return; }
        String code = args[1];
        plugin.getCodeManager().find(code).ifPresentOrElse(
                gc -> new CodeEditorGUI(plugin, player, gc).open(),
                () -> sender.sendMessage(msg("code-not-found", Map.of("code", code)))
        );
    }

    private void handleItems(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage(ChatColor.RED + "Only players can open the item editor."); return; }
        if (args.length < 2) { sender.sendMessage(usage("/gcx items <code>")); return; }
        String code = args[1];
        plugin.getCodeManager().find(code).ifPresentOrElse(
                gc -> new ItemEditorGUI(plugin, player, gc).open(),
                () -> sender.sendMessage(msg("code-not-found", Map.of("code", code)))
        );
    }

    private void handleAssign(CommandSender sender, String[] args) {
        if (args.length < 3) { sender.sendMessage(usage("/gcx assign <code> <player>")); return; }
        String code = args[1], playerName = args[2];
        if (!plugin.getCodeManager().exists(code)) {
            sender.sendMessage(msg("code-not-found", Map.of("code", code)));
            return;
        }
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            sender.sendMessage(msg("player-not-found", Map.of("player", playerName)));
            return;
        }
        plugin.getCodeManager().assign(target, code);
        sender.sendMessage(ChatColor.GREEN + "Assigned " + ChatColor.YELLOW + code
                + ChatColor.GREEN + " to " + ChatColor.AQUA + target.getName() + ChatColor.GREEN + ".");
        target.sendMessage(msg("assigned"));
    }

    private void handleSetPerm(CommandSender sender, String[] args) {
        if (args.length < 3) { sender.sendMessage(usage("/gcx setperm <code> <permission|none>")); return; }
        String code = args[1], perm = args[2];
        plugin.getCodeManager().find(code).ifPresentOrElse(gc -> {
            boolean clearing = "none".equalsIgnoreCase(perm) || "-".equals(perm);
            plugin.getCodeManager().update(gc.withPermission(clearing ? "" : perm));
            sender.sendMessage(clearing
                    ? msg("permission-cleared", Map.of("code", code))
                    : msg("permission-set",     Map.of("code", code, "permission", perm)));
        }, () -> sender.sendMessage(msg("code-not-found", Map.of("code", code))));
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) { sender.sendMessage(usage("/gcx info <code>")); return; }
        String code = args[1];
        plugin.getCodeManager().find(code).ifPresentOrElse(gc -> {
            int used = plugin.getCodeManager().globalUseCount(code);
            PlaytimeDuration pt = gc.getRequiredPlaytime();

            sender.sendMessage(ChatColor.GOLD + "── Code: " + ChatColor.YELLOW + code + ChatColor.GOLD + " ──");
            sender.sendMessage(info("Enabled",            gc.isEnabled()                ? "&aYes" : "&cNo"));
            sender.sendMessage(info("Expired",            gc.isExpired()                ? "&cYes" : "&aNo"));
            sender.sendMessage(info("Max Uses (global)",  displayMaxUses(gc.getMaxUses())));
            sender.sendMessage(info("Used (global)",      String.valueOf(used)));
            sender.sendMessage(info("Per-player limit",   gc.isUnlimitedPlayerUses()    ? inf() : String.valueOf(gc.getPlayerMaxUses())));
            sender.sendMessage(info("Per-IP limit",       gc.hasIpRestriction()         ? String.valueOf(gc.getMaxUsesPerIp()) : "Disabled"));
            sender.sendMessage(info("Expiry",             gc.getExpiry().isBlank()       ? inf() : gc.getExpiry()));
            sender.sendMessage(info("Permission",         gc.hasPermissionRestriction()  ? gc.getPermission() : "None"));

            // Playtime — show human-readable + total-minutes breakdown
            if (pt.isZero()) {
                sender.sendMessage(info("Playtime req.", "None"));
            } else {
                sender.sendMessage(info("Playtime req.", pt.toDisplayString()
                        + " &8(≈ " + pt.toTotalMinutes() + " min)"));
                sender.sendMessage(info("  breakdown",
                        pt.getYears()        + "y " +
                                pt.getMonths()       + "mo " +
                                pt.getWeeks()        + "w " +
                                pt.getDays()         + "d " +
                                pt.getHours()        + "h " +
                                pt.getMinutes()      + "m " +
                                pt.getSeconds()      + "s " +
                                pt.getMilliseconds() + "ms"));
            }

            sender.sendMessage(info("Commands",           String.valueOf(gc.getCommands().size())));
            sender.sendMessage(info("Item rewards",       String.valueOf(gc.getItemRewards().size())));
        }, () -> sender.sendMessage(msg("code-not-found", Map.of("code", code))));
    }

    private void handleRandom(CommandSender sender, String[] args) {
        if (args.length < 2) { sender.sendMessage(usage("/gcx random <prefix> [amount] [-c <template>]")); return; }
        String prefix   = args[1];
        int    amount   = 10;
        String template = null;

        for (int i = 2; i < args.length; i++) {
            if (args[i].matches("\\d+")) {
                try { amount = Math.max(1, Math.min(1000, Integer.parseInt(args[i]))); } catch (NumberFormatException ignored) {}
            } else if ("-c".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                template = args[++i];
            }
        }

        int made = plugin.getCodeManager().generateRandomFromTemplate(prefix, amount, template);
        if (made < 0) {
            sender.sendMessage(msg("code-not-found", Map.of("code", template != null ? template : "?")));
        } else {
            sender.sendMessage(msg("random-generated", Map.of("amount", String.valueOf(made), "prefix", prefix)));
        }
    }

    private void handleReload(CommandSender sender) {
        plugin.getConfigManager().reload();
        plugin.getMessageConfig().reload();
        plugin.getCodeManager().reloadFromDisk();
        sender.sendMessage(msg("plugin-reloaded"));
    }


    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "━━━ GiftcodeX Commands ━━━");
        helpLine(sender, "/gcx help",                                   "Show this help");
        helpLine(sender, "/gcx create <code> [-g]",                     "Create a code (optionally open editor)");
        helpLine(sender, "/gcx delete <code>",                          "Delete a code");
        helpLine(sender, "/gcx enable|disable <code>",                  "Toggle a code on/off");
        helpLine(sender, "/gcx gui",                                     "Open the code browser GUI");
        helpLine(sender, "/gcx edit <code>",                            "Open settings GUI editor");
        helpLine(sender, "/gcx items <code>",                           "Open item rewards editor");
        helpLine(sender, "/gcx assign <code> <player>",                 "Give a code to a player");
        helpLine(sender, "/gcx setperm <code> <perm|none>",             "Set/clear permission requirement");
        helpLine(sender, "/gcx info <code>",                            "Show code information");
        helpLine(sender, "/gcx random <prefix> [amount] [-c <tmpl>]",   "Generate random codes");
        helpLine(sender, "/gcx reload",                                  "Reload configuration");
        helpLine(sender, "/redeem <code>",                              "Redeem a gift code");
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("giftcodex.admin")) return List.of();

        if (args.length == 1) {
            return filter(SUBCOMMANDS, args[0]);
        }

        List<String> codes = plugin.getCodeManager().listCodes();
        String sub = args[0].toLowerCase();

        return switch (sub) {
            case "delete", "del", "enable", "disable", "edit", "items", "info", "setperm" -> {
                if (args.length == 2) yield filter(codes, args[1]);
                if (sub.equals("setperm") && args.length == 3) yield filter(List.of("none", "giftcodex.use."), args[2]);
                yield List.of();
            }
            case "assign" -> {
                if (args.length == 2) yield filter(codes, args[1]);
                if (args.length == 3) yield Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(n -> n.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
                yield List.of();
            }
            case "create" -> args.length == 3 ? List.of("-g") : List.of();
            case "random" -> {
                if (args.length == 3) yield List.of("10", "50", "100");
                if (args.length == 4) yield List.of("-c");
                if (args.length == 5 && "-c".equalsIgnoreCase(args[3])) yield filter(codes, args[4]);
                yield List.of();
            }
            default -> List.of();
        };
    }


    private String msg(String key, Map<String, String> placeholders) {
        return plugin.getMessageConfig().get(key, placeholders);
    }

    private String msg(String key) {
        return plugin.getMessageConfig().get(key);
    }

    private static String usage(String cmd) {
        return ChatColor.RED + "Usage: " + ChatColor.YELLOW + cmd;
    }

    private static void helpLine(CommandSender s, String cmd, String desc) {
        s.sendMessage(ChatColor.YELLOW + cmd + ChatColor.GRAY + " - " + desc);
    }

    private static String info(String label, String value) {
        return ChatColor.GRAY + "  " + label + ": " + ChatColor.WHITE + value.replace("&", "§");
    }

    private static List<String> filter(List<String> source, String prefix) {
        String lp = prefix.toLowerCase();
        return source.stream().filter(s -> s.toLowerCase().startsWith(lp)).collect(Collectors.toList());
    }
}