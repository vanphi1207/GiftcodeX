package me.ihqqq.giftcodeX.gui;

import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.ihqqq.giftcodeX.GiftcodeX;
import me.ihqqq.giftcodeX.gui.base.GiftGUI;
import me.ihqqq.giftcodeX.model.Giftcode;
import me.ihqqq.giftcodeX.model.PlaytimeDuration;
import me.ihqqq.giftcodeX.util.ChatInputRequest;
import me.ihqqq.giftcodeX.util.ClientVersionUtils;
import me.ihqqq.giftcodeX.util.FoliaUtils;
import me.ihqqq.giftcodeX.util.ItemBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public final class CodeEditorGUI extends GiftGUI implements Listener {

    private static final String NS = "giftcodex";

    private static Key key(String path) { return Key.key(NS, path); }

    private static final Key KEY_EXPIRY_CONFIRM    = key("editor/expiry/confirm");
    private static final Key KEY_EXPIRY_CANCEL     = key("editor/expiry/cancel");
    private static final Key KEY_PERM_CONFIRM      = key("editor/permission/confirm");
    private static final Key KEY_PERM_CANCEL       = key("editor/permission/cancel");
    private static final Key KEY_CMDS_CONFIRM      = key("editor/commands/confirm");
    private static final Key KEY_CMDS_CANCEL       = key("editor/commands/cancel");
    private static final Key KEY_MSGS_CONFIRM      = key("editor/messages/confirm");
    private static final Key KEY_MSGS_CANCEL       = key("editor/messages/cancel");
    private static final Key KEY_PLAYTIME_CONFIRM  = key("editor/playtime/confirm");
    private static final Key KEY_PLAYTIME_CANCEL   = key("editor/playtime/cancel");

    private static final int SLOT_INFO       = 4;
    private static final int SLOT_MAX_USES   = 20;
    private static final int SLOT_PLAYER_USE = 21;
    private static final int SLOT_IP_USE     = 23;
    private static final int SLOT_PLAYTIME   = 24;
    private static final int SLOT_TOGGLE     = 19;
    private static final int SLOT_EXPIRY     = 22;
    private static final int SLOT_PERMISSION = 25;
    private static final int SLOT_COMMANDS   = 31;
    private static final int SLOT_MESSAGES   = 30;
    private static final int SLOT_ITEMS      = 32;
    private static final int SLOT_BACK       = 45;
    private static final int SLOT_SAVE       = 53;

    private static final int UNLIMITED_USES = Integer.MAX_VALUE;

    private Giftcode gc;
    private final boolean dialogSupported;

    public CodeEditorGUI(GiftcodeX plugin, Player viewer, Giftcode gc) {
        super(plugin, viewer);
        this.gc = gc;
        this.dialogSupported = ClientVersionUtils.supportsDialog(viewer);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    private String inf() {
        return plugin.getMessageConfig().getInfinitySymbol();
    }

    private boolean isUnlimitedUses() {
        return gc.getMaxUses() >= UNLIMITED_USES;
    }


    @Override
    protected Inventory buildInventory() {
        Inventory inv = Bukkit.createInventory(null, 54, title("code editor • " + gc.getCode()));

        for (int i = 0; i < 54; i++) inv.setItem(i, softPanel());

        inv.setItem(SLOT_INFO,       buildInfoItem());
        inv.setItem(SLOT_MAX_USES,   buildMaxUsesItem());
        inv.setItem(SLOT_PLAYER_USE, buildPlayerUseItem());
        inv.setItem(SLOT_IP_USE,     buildIpLimitItem());
        inv.setItem(SLOT_PLAYTIME,   buildPlaytimeItem());

        boolean on = gc.isEnabled();
        inv.setItem(SLOT_TOGGLE, new ItemBuilder(on ? Material.LIME_DYE : Material.GRAY_DYE)
                .name((on ? C_GOOD : C_BAD) + sc("status • " + (on ? "enabled" : "disabled")))
                .lore(buildToggleLore(on))
                .glow(on)
                .build());

        inv.setItem(SLOT_EXPIRY,     buildExpiryItem());

        boolean hasPerm = gc.hasPermissionRestriction();
        inv.setItem(SLOT_PERMISSION, buildActionItem(Material.PAPER, "set permission",
                loreLine("current", hasPerm ? gc.getPermission() : "none",
                        hasPerm ? C_SPECIAL : C_HINT),
                loreAction("click to edit"),
                inputModeHint()));

        inv.setItem(SLOT_COMMANDS, buildListItem(Material.COMMAND_BLOCK,
                "commands • " + gc.getCommands().size(), gc.getCommands(), C_WARN));

        inv.setItem(SLOT_MESSAGES, buildListItem(Material.BOOK,
                "messages • " + gc.getMessages().size(), gc.getMessages(), C_VALUE));

        inv.setItem(SLOT_ITEMS, buildActionItem(Material.CHEST,
                "item rewards • " + gc.getItemRewards().size(),
                loreLine("saved", String.valueOf(gc.getItemRewards().size()), C_SPECIAL),
                loreAction("click to open item editor")));

        inv.setItem(SLOT_BACK, buildActionItem(Material.ARROW, "back to list",
                loreAction("return without saving")));
        inv.setItem(SLOT_SAVE, buildSaveItem());

        return inv;
    }


    private ItemStack buildPlaytimeItem() {
        PlaytimeDuration pt  = gc.getRequiredPlaytime();
        boolean hasReq       = !pt.isZero();

        List<String> lore = new ArrayList<>();
        lore.add(loreLine("required", hasReq ? pt.toDisplayString() : "none", hasReq ? C_WARN : C_HINT));
        lore.add("");

        // show each field individually
        lore.add(C_LABEL + sc("breakdown:"));
        lore.add(loreLine("  years",        String.valueOf(pt.getYears()),        C_VALUE));
        lore.add(loreLine("  months",       String.valueOf(pt.getMonths()),       C_VALUE));
        lore.add(loreLine("  weeks",        String.valueOf(pt.getWeeks()),        C_VALUE));
        lore.add(loreLine("  days",         String.valueOf(pt.getDays()),         C_VALUE));
        lore.add(loreLine("  hours",        String.valueOf(pt.getHours()),        C_VALUE));
        lore.add(loreLine("  minutes",      String.valueOf(pt.getMinutes()),      C_VALUE));
        lore.add(loreLine("  seconds",      String.valueOf(pt.getSeconds()),      C_VALUE));
        lore.add(loreLine("  milliseconds", String.valueOf(pt.getMilliseconds()), C_VALUE));
        lore.add("");
        lore.add(loreAction("click to edit"));
        lore.add(C_HINT + sc("Q • reset to zero (no requirement)"));
        lore.add(inputModeHint());

        return new ItemBuilder(Material.CLOCK)
                .name(C_VALUE + sc("required playtime"))
                .lore(lore)
                .glow(!hasReq)
                .build();
    }


    private String inputModeHint() {
        return dialogSupported
                ? C_HINT + sc("✦ dialog input")
                : C_WARN + sc("✦ chat input");
    }


    private ItemStack buildInfoItem() {
        int used        = plugin.getCodeManager().globalUseCount(gc.getCode());
        boolean expired = gc.isExpired();
        boolean enabled = gc.isEnabled();

        List<String> lore = new ArrayList<>();
        lore.add(loreLine("status", enabled ? (expired ? "expired" : "enabled") : "disabled",
                enabled ? (expired ? C_BAD : C_GOOD) : C_WARN));
        lore.add(loreLine("uses remaining", isUnlimitedUses() ? inf() : String.valueOf(gc.getMaxUses()), C_WARN));
        lore.add(loreLine("total redeemed", String.valueOf(used), C_VALUE));
        lore.add(loreLine("expired", expired ? "yes" : "no", expired ? C_BAD : C_GOOD));
        lore.add("");
        lore.add(inputModeHint());

        return new ItemBuilder(Material.KNOWLEDGE_BOOK)
                .name(C_VALUE + gc.getCode())
                .lore(lore)
                .build();
    }

    private ItemStack buildMaxUsesItem() {
        String displayVal = isUnlimitedUses() ? inf() : String.valueOf(gc.getMaxUses());

        List<String> lore = new ArrayList<>();
        lore.add(loreLine("value", displayVal, C_WARN));
        lore.add("");
        lore.add(C_HINT + sc("left -1 • right +1 • shift ±10"));
        lore.add(C_HINT + sc("Q • toggle " + inf() + " (unlimited global uses)"));

        return new ItemBuilder(Material.GOLD_INGOT)
                .name(C_VALUE + sc("max uses"))
                .lore(lore)
                .glow(isUnlimitedUses())
                .build();
    }

    private ItemStack buildPlayerUseItem() {
        boolean unlimitedPlayer = gc.isUnlimitedPlayerUses();
        String displayVal = unlimitedPlayer ? inf() : String.valueOf(gc.getPlayerMaxUses());

        List<String> lore = new ArrayList<>();
        lore.add(loreLine("value", displayVal, C_SPECIAL));
        lore.add("");
        lore.add(C_HINT + sc("left -1 • right +1"));
        lore.add(C_HINT + sc("Q • toggle " + inf() + " (unlimited per player)"));

        return new ItemBuilder(Material.PLAYER_HEAD)
                .name(C_VALUE + sc("player limit"))
                .lore(lore)
                .glow(unlimitedPlayer)
                .build();
    }

    private ItemStack buildExpiryItem() {
        boolean hasExpiry    = !gc.getExpiry().isBlank();
        String displayExpiry = hasExpiry ? gc.getExpiry() : inf();

        List<String> lore = new ArrayList<>();
        lore.add(loreLine("current", displayExpiry,
                gc.isExpired() ? C_BAD : hasExpiry ? C_WARN : C_GOOD));
        lore.add("");
        lore.add(loreAction("click to edit"));
        lore.add(C_HINT + sc("Q • set to " + inf() + " (no expiry)"));
        lore.add(C_HINT + sc("format • 2099-12-31T23:59:59"));
        lore.add(inputModeHint());

        return new ItemBuilder(Material.CLOCK)
                .name(C_VALUE + sc("set expiry"))
                .lore(lore)
                .glow(!hasExpiry)
                .build();
    }

    private ItemStack buildIpLimitItem() {
        boolean disabled  = !gc.hasIpRestriction();
        String displayVal = disabled ? inf() : String.valueOf(gc.getMaxUsesPerIp());

        List<String> lore = new ArrayList<>();
        lore.add(loreLine("value", displayVal, C_SPECIAL));
        lore.add("");
        lore.add(C_HINT + sc("left -1 • right +1 • 0 = disabled"));
        lore.add(C_HINT + sc("Q • toggle " + inf() + " (no ip restriction)"));

        return new ItemBuilder(Material.COMPASS)
                .name(C_VALUE + sc("ip limit"))
                .lore(lore)
                .glow(disabled)
                .build();
    }

    private List<String> buildToggleLore(boolean on) {
        List<String> lore = new ArrayList<>();
        lore.add(loreLine("current", on ? "enabled" : "disabled", on ? C_GOOD : C_BAD));
        lore.add("");
        lore.add(loreAction("click to toggle"));
        return lore;
    }

    private ItemStack buildActionItem(Material mat, String name, String... loreDirect) {
        return new ItemBuilder(mat)
                .name(C_VALUE + sc(name))
                .lore(new ArrayList<>(List.of(loreDirect)))
                .build();
    }

    private ItemStack buildListItem(Material mat, String name, List<String> entries, String entryColor) {
        List<String> lore = new ArrayList<>();
        int max   = 5;
        int shown = Math.min(entries.size(), max);
        for (int i = 0; i < shown; i++) lore.add(C_HINT + sc("• ") + entryColor + entries.get(i));
        if (entries.size() > max) lore.add(C_HINT + sc("• +" + (entries.size() - max) + " more..."));
        if (entries.isEmpty())    lore.add(C_HINT + sc("• (none)"));
        lore.add("");
        lore.add(loreAction("click to edit • separate with |"));
        lore.add(inputModeHint());

        return new ItemBuilder(mat)
                .name(C_VALUE + sc(name))
                .lore(lore)
                .build();
    }

    private ItemStack buildSaveItem() {
        List<String> lore = new ArrayList<>();
        lore.add(C_GOOD + sc("✔ saves all pending changes"));
        lore.add("");
        lore.add(loreAction("click to save & close"));
        return new ItemBuilder(Material.EMERALD)
                .name(C_GOOD + sc("save and close"))
                .lore(lore)
                .glow()
                .build();
    }


    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!event.getClickedInventory().equals(inventory)) return;

        ClickType click = event.getClick();
        boolean shift = event.isShiftClick();
        boolean right = event.isRightClick();
        boolean drop  = click == ClickType.DROP || click == ClickType.CONTROL_DROP;

        switch (event.getSlot()) {

            case SLOT_MAX_USES -> {
                if (drop) {
                    gc = isUnlimitedUses()
                            ? gc.withMaxUses(plugin.getConfigManager().getDefaultMaxUses())
                            : gc.withMaxUses(UNLIMITED_USES);
                    viewer.playSound(viewer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                            0.5f, isUnlimitedUses() ? 1.5f : 0.8f);
                } else {
                    if (isUnlimitedUses()) return;
                    adjust(right, shift, 10, gc.getMaxUses(), 0, UNLIMITED_USES - 1,
                            v -> gc = gc.withMaxUses(v));
                }
            }

            case SLOT_PLAYER_USE -> {
                if (drop) {
                    gc = gc.isUnlimitedPlayerUses()
                            ? gc.toBuilder().playerMaxUses(plugin.getConfigManager().getDefaultPlayerMaxUses()).build()
                            : gc.toBuilder().playerMaxUses(-1).build();
                    viewer.playSound(viewer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                            0.5f, gc.isUnlimitedPlayerUses() ? 1.5f : 0.8f);
                } else {
                    if (gc.isUnlimitedPlayerUses()) return;
                    adjust(right, shift, 5, gc.getPlayerMaxUses(), -1, Integer.MAX_VALUE,
                            v -> gc = gc.toBuilder().playerMaxUses(v).build());
                }
            }

            case SLOT_IP_USE -> {
                if (drop) {
                    boolean currentlyDisabled = !gc.hasIpRestriction();
                    gc = gc.toBuilder()
                            .maxUsesPerIp(currentlyDisabled
                                    ? plugin.getConfigManager().getDefaultMaxUsesPerIp()
                                    : 0)
                            .build();
                    viewer.playSound(viewer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                            0.5f, !gc.hasIpRestriction() ? 1.5f : 0.8f);
                } else {
                    adjust(right, shift, 5, gc.getMaxUsesPerIp(), 0, Integer.MAX_VALUE,
                            v -> gc = gc.toBuilder().maxUsesPerIp(v).build());
                }
            }

            case SLOT_PLAYTIME -> {
                if (drop) {
                    gc = gc.withRequiredPlaytime(PlaytimeDuration.zero());
                    plugin.getCodeManager().update(gc);
                    viewer.playSound(viewer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
                    refresh();
                } else {
                    openPlaytimeInput();
                    return;
                }
            }

            case SLOT_TOGGLE -> {
                gc = gc.withEnabled(!gc.isEnabled());
                viewer.playSound(viewer.getLocation(), Sound.UI_BUTTON_CLICK,
                        0.5f, gc.isEnabled() ? 1.2f : 0.8f);
            }

            case SLOT_EXPIRY -> {
                if (drop) {
                    gc = gc.toBuilder().expiry("").build();
                    plugin.getCodeManager().update(gc);
                    viewer.playSound(viewer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
                } else {
                    openExpiryInput();
                    return;
                }
            }

            case SLOT_PERMISSION -> { openPermissionInput(); return; }
            case SLOT_COMMANDS   -> { openCommandsInput();   return; }
            case SLOT_MESSAGES   -> { openMessagesInput();   return; }

            case SLOT_ITEMS -> {
                plugin.getGuiListener().deregister(viewer.getUniqueId());
                new ItemEditorGUI(plugin, viewer, gc).open();
                return;
            }
            case SLOT_BACK -> {
                plugin.getGuiListener().deregister(viewer.getUniqueId());
                new CodeListGUI(plugin, viewer, 0).open();
                return;
            }
            case SLOT_SAVE -> {
                plugin.getCodeManager().update(gc);
                viewer.playSound(viewer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
                viewer.sendMessage(C_GOOD + "Changes saved for code " + C_WARN + gc.getCode() + C_GOOD + ".");
                close();
                return;
            }
            default -> { return; }
        }
        refresh();
    }

    private void adjust(boolean right, boolean shift, int shiftStep, int current, int min, int max,
                        java.util.function.IntConsumer setter) {
        int delta  = shift ? shiftStep : 1;
        int newVal = Math.max(min, Math.min(max, current + (right ? delta : -delta)));
        setter.accept(newVal);
    }



    private void openPlaytimeInput() {
        if (dialogSupported) {
            openPlaytimeDialog();
        } else {
            PlaytimeDuration cur = gc.getRequiredPlaytime();
            closeThenChat(
                    "&fEnter the required playtime. Format:\n"
                            + "&8  &eyears months weeks days hours minutes seconds milliseconds\n"
                            + "&8  Example: &b0 0 0 1 2 30 0 0 &8(= 1 day, 2 hours, 30 minutes)\n"
                            + "&8  Current: &7" + cur.toDisplayString()
                            + "\n&8  Or type &c0 &8to reset to no requirement.",
                    raw -> {
                        if (raw == null) { reopenEditor(); return; }
                        PlaytimeDuration parsed = parsePlaytimeChatInput(raw.trim());
                        if (parsed != null) {
                            gc = gc.withRequiredPlaytime(parsed);
                            plugin.getCodeManager().update(gc);
                        } else {
                            viewer.sendMessage(C_BAD + "Invalid format. Expected 8 non-negative numbers separated by spaces.");
                        }
                        reopenEditor();
                    }
            );
        }
    }

    private void openPlaytimeDialog() {
        PlaytimeDuration cur = gc.getRequiredPlaytime();

        viewer.showDialog(Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Component.text("Set Required Playtime", NamedTextColor.GOLD))
                        .body(List.of(
                                DialogBody.plainMessage(Component.text(
                                        "Current: " + cur.toDisplayString(), NamedTextColor.YELLOW)),
                                DialogBody.plainMessage(Component.text(
                                        "Leave all fields at 0 to disable the playtime requirement.",
                                        NamedTextColor.GRAY))
                        ))
                        .inputs(List.of(
                                DialogInput.text("years",
                                                Component.text("Years", NamedTextColor.AQUA))
                                        .initial(String.valueOf(cur.getYears())).maxLength(6).width(80).build(),
                                DialogInput.text("months",
                                                Component.text("Months", NamedTextColor.AQUA))
                                        .initial(String.valueOf(cur.getMonths())).maxLength(6).width(80).build(),
                                DialogInput.text("weeks",
                                                Component.text("Weeks", NamedTextColor.AQUA))
                                        .initial(String.valueOf(cur.getWeeks())).maxLength(6).width(80).build(),
                                DialogInput.text("days",
                                                Component.text("Days", NamedTextColor.AQUA))
                                        .initial(String.valueOf(cur.getDays())).maxLength(6).width(80).build(),
                                DialogInput.text("hours",
                                                Component.text("Hours", NamedTextColor.AQUA))
                                        .initial(String.valueOf(cur.getHours())).maxLength(6).width(80).build(),
                                DialogInput.text("minutes",
                                                Component.text("Minutes", NamedTextColor.AQUA))
                                        .initial(String.valueOf(cur.getMinutes())).maxLength(6).width(80).build(),
                                DialogInput.text("seconds",
                                                Component.text("Seconds", NamedTextColor.AQUA))
                                        .initial(String.valueOf(cur.getSeconds())).maxLength(6).width(80).build(),
                                DialogInput.text("milliseconds",
                                                Component.text("Milliseconds", NamedTextColor.AQUA))
                                        .initial(String.valueOf(cur.getMilliseconds())).maxLength(6).width(80).build()
                        ))
                        .build())
                .type(confirmationType(KEY_PLAYTIME_CONFIRM, KEY_PLAYTIME_CANCEL))
        ));
    }

    private static PlaytimeDuration parsePlaytimeChatInput(String raw) {
        if (raw.equals("0")) return PlaytimeDuration.zero();
        String[] parts = raw.split("\\s+");
        if (parts.length != 8) return null;
        try {
            return new PlaytimeDuration.Builder()
                    .years(        Math.max(0, Integer.parseInt(parts[0])))
                    .months(       Math.max(0, Integer.parseInt(parts[1])))
                    .weeks(        Math.max(0, Integer.parseInt(parts[2])))
                    .days(         Math.max(0, Integer.parseInt(parts[3])))
                    .hours(        Math.max(0, Integer.parseInt(parts[4])))
                    .minutes(      Math.max(0, Integer.parseInt(parts[5])))
                    .seconds(      Math.max(0, Integer.parseInt(parts[6])))
                    .milliseconds( Math.max(0, Integer.parseInt(parts[7])))
                    .build();
        } catch (NumberFormatException e) {
            return null;
        }
    }


    private void openExpiryInput() {
        if (dialogSupported) {
            openExpiryDialog();
        } else {
            closeThenChat(
                    "&fEnter the new expiry date &7(format: &e2099-12-31T23:59:59&7)&f, "
                            + "or leave blank for &b" + inf() + " &f(no expiry):",
                    raw -> {
                        gc = gc.toBuilder().expiry(raw == null || raw.isBlank() ? "" : raw.trim()).build();
                        plugin.getCodeManager().update(gc);
                        reopenEditor();
                    }
            );
        }
    }

    private void openPermissionInput() {
        if (dialogSupported) {
            openPermissionDialog();
        } else {
            closeThenChat(
                    "&fEnter the permission node for code &e" + gc.getCode()
                            + "&f, or leave blank to &cremove &fthe requirement:",
                    raw -> {
                        gc = gc.withPermission(raw == null ? "" : raw.trim());
                        plugin.getCodeManager().update(gc);
                        reopenEditor();
                    }
            );
        }
    }

    private void openCommandsInput() {
        if (dialogSupported) {
            openCommandsDialog();
        } else {
            closeThenChat(
                    "&fEnter commands separated by &e|&f. Use &b%player% &fas the player name placeholder.\n"
                            + "&8  Current: &7" + String.join(" | ", gc.getCommands()),
                    raw -> {
                        if (raw != null && !raw.isBlank()) {
                            gc = gc.withCommands(Arrays.asList(raw.split("\\|")));
                            plugin.getCodeManager().update(gc);
                        }
                        reopenEditor();
                    }
            );
        }
    }

    private void openMessagesInput() {
        if (dialogSupported) {
            openMessagesDialog();
        } else {
            closeThenChat(
                    "&fEnter messages separated by &e|&f. Supports &a& colour codes&f.\n"
                            + "&8  Current: &7" + String.join(" | ", gc.getMessages()),
                    raw -> {
                        if (raw != null && !raw.isBlank()) {
                            gc = gc.withMessages(Arrays.asList(raw.split("\\|")));
                            plugin.getCodeManager().update(gc);
                        }
                        reopenEditor();
                    }
            );
        }
    }


    private void closeThenChat(String prompt, java.util.function.Consumer<String> handler) {
        plugin.getGuiListener().deregister(viewer.getUniqueId());
        FoliaUtils.runForPlayer(plugin, viewer, () -> {
            viewer.closeInventory();
            ChatInputRequest.prompt(plugin, viewer, prompt, handler);
        });
    }


    private void openExpiryDialog() {
        viewer.showDialog(Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Component.text("Set Expiry Date", NamedTextColor.GOLD))
                        .body(List.of(
                                DialogBody.plainMessage(Component.text("Current: ").append(
                                        Component.text(gc.getExpiry().isBlank() ? inf() : gc.getExpiry(),
                                                NamedTextColor.WHITE))),
                                DialogBody.plainMessage(Component.text(
                                        "Format: 2099-12-31T23:59:59  |  Leave blank for " + inf() + " (no expiry)",
                                        NamedTextColor.GRAY))
                        ))
                        .inputs(List.of(
                                DialogInput.text("expiry",
                                                Component.text("Expiry Date/Time", NamedTextColor.YELLOW))
                                        .initial(gc.getExpiry()).maxLength(19).width(300).build()
                        ))
                        .build())
                .type(confirmationType(KEY_EXPIRY_CONFIRM, KEY_EXPIRY_CANCEL))
        ));
    }

    private void openPermissionDialog() {
        viewer.showDialog(Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Component.text("Set Permission", NamedTextColor.AQUA))
                        .body(List.of(
                                DialogBody.plainMessage(Component.text("Current: ").append(
                                        Component.text(gc.hasPermissionRestriction()
                                                ? gc.getPermission() : "None", NamedTextColor.WHITE))),
                                DialogBody.plainMessage(Component.text(
                                        "Leave blank to remove the permission requirement.", NamedTextColor.GRAY))
                        ))
                        .inputs(List.of(
                                DialogInput.text("permission",
                                                Component.text("Permission Node", NamedTextColor.YELLOW))
                                        .initial(gc.getPermission()).maxLength(100).width(300).build()
                        ))
                        .build())
                .type(confirmationType(KEY_PERM_CONFIRM, KEY_PERM_CANCEL))
        ));
    }

    private void openCommandsDialog() {
        viewer.showDialog(Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Component.text("Edit Commands", NamedTextColor.RED))
                        .body(List.of(
                                DialogBody.plainMessage(Component.text(
                                        "Enter commands separated by  |", NamedTextColor.GRAY)),
                                DialogBody.plainMessage(Component.text(
                                        "Use %player% as player name placeholder.", NamedTextColor.GRAY))
                        ))
                        .inputs(List.of(
                                DialogInput.text("commands",
                                                Component.text("Commands (| separated)", NamedTextColor.YELLOW))
                                        .initial(String.join("|", gc.getCommands()))
                                        .maxLength(500).width(350).build()
                        ))
                        .build())
                .type(confirmationType(KEY_CMDS_CONFIRM, KEY_CMDS_CANCEL))
        ));
    }

    private void openMessagesDialog() {
        viewer.showDialog(Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Component.text("Edit Messages", NamedTextColor.YELLOW))
                        .body(List.of(
                                DialogBody.plainMessage(Component.text(
                                        "Enter messages separated by  |", NamedTextColor.GRAY)),
                                DialogBody.plainMessage(Component.text(
                                        "Supports & colour codes.", NamedTextColor.GRAY))
                        ))
                        .inputs(List.of(
                                DialogInput.text("messages",
                                                Component.text("Messages (| separated)", NamedTextColor.YELLOW))
                                        .initial(String.join("|", gc.getMessages()))
                                        .maxLength(500).width(350).build()
                        ))
                        .build())
                .type(confirmationType(KEY_MSGS_CONFIRM, KEY_MSGS_CANCEL))
        ));
    }

    private static DialogType confirmationType(Key confirm, Key cancel) {
        return DialogType.confirmation(
                ActionButton.builder(Component.text("✔ Confirm", TextColor.color(0xAEFFC1)))
                        .action(DialogAction.customClick(confirm, null)).build(),
                ActionButton.builder(Component.text("✕ Cancel", TextColor.color(0xFFA0B1)))
                        .action(DialogAction.customClick(cancel, null)).build()
        );
    }


    @EventHandler
    public void onDialogClick(PlayerCustomClickEvent event) {
        var connection = event.getCommonConnection();
        if (!(connection instanceof PlayerGameConnection gameConnection)) return;
        Player player = gameConnection.getPlayer();
        if (!player.getUniqueId().equals(viewer.getUniqueId())) return;

        Key id   = event.getIdentifier();
        var view = event.getDialogResponseView();

        if (id.equals(KEY_PLAYTIME_CONFIRM) && view != null) {
            int years  = parseField(view.getText("years"));
            int months = parseField(view.getText("months"));
            int weeks  = parseField(view.getText("weeks"));
            int days   = parseField(view.getText("days"));
            int hours  = parseField(view.getText("hours"));
            int mins   = parseField(view.getText("minutes"));
            int secs   = parseField(view.getText("seconds"));
            int ms     = parseField(view.getText("milliseconds"));

            PlaytimeDuration newPt = new PlaytimeDuration.Builder()
                    .years(years).months(months).weeks(weeks).days(days)
                    .hours(hours).minutes(mins).seconds(secs).milliseconds(ms)
                    .build();
            gc = gc.withRequiredPlaytime(newPt);
            plugin.getCodeManager().update(gc);

        } else if (id.equals(KEY_EXPIRY_CONFIRM) && view != null) {
            String raw = view.getText("expiry");
            gc = gc.toBuilder().expiry(raw == null ? "" : raw.trim()).build();
            plugin.getCodeManager().update(gc);
        } else if (id.equals(KEY_PERM_CONFIRM) && view != null) {
            String raw = view.getText("permission");
            gc = gc.withPermission(raw == null ? "" : raw.trim());
            plugin.getCodeManager().update(gc);
        } else if (id.equals(KEY_CMDS_CONFIRM) && view != null) {
            String raw = view.getText("commands");
            if (raw != null && !raw.isBlank()) {
                gc = gc.withCommands(Arrays.asList(raw.split("\\|")));
                plugin.getCodeManager().update(gc);
            }
        } else if (id.equals(KEY_MSGS_CONFIRM) && view != null) {
            String raw = view.getText("messages");
            if (raw != null && !raw.isBlank()) {
                gc = gc.withMessages(Arrays.asList(raw.split("\\|")));
                plugin.getCodeManager().update(gc);
            }
        }

        reopenEditor();
    }

    private static int parseField(String raw) {
        if (raw == null || raw.isBlank()) return 0;
        try { return Math.max(0, Integer.parseInt(raw.trim())); }
        catch (NumberFormatException e) { return 0; }
    }


    private void reopenEditor() {
        FoliaUtils.runForPlayer(plugin, viewer, () -> {
            plugin.getGuiListener().deregister(viewer.getUniqueId());
            new CodeEditorGUI(plugin, viewer, gc).open();
        });
    }

    private void refresh() {
        Inventory fresh = buildInventory();
        for (int i = 0; i < 54; i++) inventory.setItem(i, fresh.getItem(i));
    }
}