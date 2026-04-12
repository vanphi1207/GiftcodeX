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
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public final class DialogInputHandler implements Listener {

    private final GiftcodeX plugin;
    private final Player viewer;

    private Key pendingConfirmKey;
    private Key pendingCancelKey;
    private Consumer<Map<String, String>> pendingConfirmCallback;
    private Runnable pendingCancelCallback;

    public DialogInputHandler(GiftcodeX plugin, Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void unregister() {
        HandlerList.unregisterAll(this);
    }

    private void showDialog(Dialog dialog, Key confirmKey, Key cancelKey,
                            Consumer<Map<String, String>> onConfirm, Runnable onCancel) {
        this.pendingConfirmKey      = confirmKey;
        this.pendingCancelKey       = cancelKey;
        this.pendingConfirmCallback = onConfirm;
        this.pendingCancelCallback  = onCancel;
        viewer.showDialog(dialog);
    }

    public void openPlaytimeDialog(long years, long months, long weeks, long days,
                                   long hours, long minutes, long seconds, long milliseconds,
                                   String currentDisplay,
                                   Key confirmKey, Key cancelKey,
                                   Consumer<Map<String, String>> onConfirm, Runnable onCancel) {
        Dialog dialog = Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Component.text("Set Required Playtime", NamedTextColor.GOLD))
                        .body(List.of(
                                DialogBody.plainMessage(Component.text(
                                        "Current: " + currentDisplay, NamedTextColor.YELLOW)),
                                DialogBody.plainMessage(Component.text(
                                        "Leave all fields at 0 to disable the playtime requirement.",
                                        NamedTextColor.GRAY))
                        ))
                        .inputs(List.of(
                                DialogInput.text("years",
                                                Component.text("Years", NamedTextColor.AQUA))
                                        .initial(String.valueOf(years)).maxLength(6).width(80).build(),
                                DialogInput.text("months",
                                                Component.text("Months", NamedTextColor.AQUA))
                                        .initial(String.valueOf(months)).maxLength(6).width(80).build(),
                                DialogInput.text("weeks",
                                                Component.text("Weeks", NamedTextColor.AQUA))
                                        .initial(String.valueOf(weeks)).maxLength(6).width(80).build(),
                                DialogInput.text("days",
                                                Component.text("Days", NamedTextColor.AQUA))
                                        .initial(String.valueOf(days)).maxLength(6).width(80).build(),
                                DialogInput.text("hours",
                                                Component.text("Hours", NamedTextColor.AQUA))
                                        .initial(String.valueOf(hours)).maxLength(6).width(80).build(),
                                DialogInput.text("minutes",
                                                Component.text("Minutes", NamedTextColor.AQUA))
                                        .initial(String.valueOf(minutes)).maxLength(6).width(80).build(),
                                DialogInput.text("seconds",
                                                Component.text("Seconds", NamedTextColor.AQUA))
                                        .initial(String.valueOf(seconds)).maxLength(6).width(80).build(),
                                DialogInput.text("milliseconds",
                                                Component.text("Milliseconds", NamedTextColor.AQUA))
                                        .initial(String.valueOf(milliseconds)).maxLength(6).width(80).build()
                        ))
                        .build())
                .type(confirmationType(confirmKey, cancelKey))
        );
        showDialog(dialog, confirmKey, cancelKey, onConfirm, onCancel);
    }

    public void openExpiryDialog(String currentExpiry, String infinitySymbol,
                                 Key confirmKey, Key cancelKey,
                                 Consumer<Map<String, String>> onConfirm, Runnable onCancel) {
        Dialog dialog = Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Component.text("Set Expiry Date", NamedTextColor.GOLD))
                        .body(List.of(
                                DialogBody.plainMessage(Component.text("Current: ").append(
                                        Component.text(currentExpiry.isBlank()
                                                ? infinitySymbol : currentExpiry, NamedTextColor.WHITE))),
                                DialogBody.plainMessage(Component.text(
                                        "Format: 2099-12-31T23:59:59  |  Leave blank for "
                                                + infinitySymbol + " (no expiry)", NamedTextColor.GRAY))
                        ))
                        .inputs(List.of(
                                DialogInput.text("expiry",
                                                Component.text("Expiry Date/Time", NamedTextColor.YELLOW))
                                        .initial(currentExpiry).maxLength(19).width(300).build()
                        ))
                        .build())
                .type(confirmationType(confirmKey, cancelKey))
        );
        showDialog(dialog, confirmKey, cancelKey, onConfirm, onCancel);
    }

    public void openPermissionDialog(String currentPermission,
                                     Key confirmKey, Key cancelKey,
                                     Consumer<Map<String, String>> onConfirm, Runnable onCancel) {
        Dialog dialog = Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Component.text("Set Permission", NamedTextColor.AQUA))
                        .body(List.of(
                                DialogBody.plainMessage(Component.text("Current: ").append(
                                        Component.text(currentPermission.isEmpty()
                                                ? "None" : currentPermission, NamedTextColor.WHITE))),
                                DialogBody.plainMessage(Component.text(
                                        "Leave blank to remove the permission requirement.",
                                        NamedTextColor.GRAY))
                        ))
                        .inputs(List.of(
                                DialogInput.text("permission",
                                                Component.text("Permission Node", NamedTextColor.YELLOW))
                                        .initial(currentPermission).maxLength(100).width(300).build()
                        ))
                        .build())
                .type(confirmationType(confirmKey, cancelKey))
        );
        showDialog(dialog, confirmKey, cancelKey, onConfirm, onCancel);
    }

    public void openCommandsDialog(String currentCommands,
                                   Key confirmKey, Key cancelKey,
                                   Consumer<Map<String, String>> onConfirm, Runnable onCancel) {
        Dialog dialog = Dialog.create(b -> b.empty()
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
                                        .initial(currentCommands).maxLength(500).width(350).build()
                        ))
                        .build())
                .type(confirmationType(confirmKey, cancelKey))
        );
        showDialog(dialog, confirmKey, cancelKey, onConfirm, onCancel);
    }

    public void openMessagesDialog(String currentMessages,
                                   Key confirmKey, Key cancelKey,
                                   Consumer<Map<String, String>> onConfirm, Runnable onCancel) {
        Dialog dialog = Dialog.create(b -> b.empty()
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
                                        .initial(currentMessages).maxLength(500).width(350).build()
                        ))
                        .build())
                .type(confirmationType(confirmKey, cancelKey))
        );
        showDialog(dialog, confirmKey, cancelKey, onConfirm, onCancel);
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

        if (pendingConfirmKey != null && id.equals(pendingConfirmKey)) {
            if (pendingConfirmCallback != null && view != null) {
                java.util.Map<String, String> fields = new java.util.HashMap<>();
                for (String key : List.of("years", "months", "weeks", "days",
                        "hours", "minutes", "seconds", "milliseconds",
                        "expiry", "permission", "commands", "messages")) {
                    String val = view.getText(key);
                    if (val != null) fields.put(key, val);
                }
                pendingConfirmCallback.accept(fields);
            }
            clearPending();
        } else if (pendingCancelKey != null && id.equals(pendingCancelKey)) {
            if (pendingCancelCallback != null) pendingCancelCallback.run();
            clearPending();
        }
    }

    private void clearPending() {
        pendingConfirmKey      = null;
        pendingCancelKey       = null;
        pendingConfirmCallback = null;
        pendingCancelCallback  = null;
    }
}
