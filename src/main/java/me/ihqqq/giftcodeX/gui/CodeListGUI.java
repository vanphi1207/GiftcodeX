package me.ihqqq.giftcodeX.gui;

import me.ihqqq.giftcodeX.GiftcodeX;
import me.ihqqq.giftcodeX.gui.base.GiftGUI;
import me.ihqqq.giftcodeX.model.Giftcode;
import me.ihqqq.giftcodeX.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CodeListGUI extends GiftGUI {

    private static final int PAGE_SIZE = 45;
    private static final int PREV_SLOT = 45;
    private static final int INFO_SLOT = 49;
    private static final int NEXT_SLOT = 53;

    private int page;

    public CodeListGUI(GiftcodeX plugin, Player viewer) {
        this(plugin, viewer, 0);
    }

    public CodeListGUI(GiftcodeX plugin, Player viewer, int page) {
        super(plugin, viewer);
        this.page = page;
    }

    public void open(int page) {
        this.page = page;
        open();
    }

    @Override
    protected Inventory buildInventory() {
        List<String> codeKeys = plugin.getCodeManager().listCodes();
        int totalPages = Math.max(1, (int) Math.ceil((double) codeKeys.size() / PAGE_SIZE));
        page = Math.max(0, Math.min(page, totalPages - 1));

        Inventory inv = Bukkit.createInventory(null, 54, title("gift codes"));

        int start = page * PAGE_SIZE;
        int end   = Math.min(start + PAGE_SIZE, codeKeys.size());
        for (int i = start; i < end; i++) {
            String key = codeKeys.get(i);
            plugin.getCodeManager().find(key).ifPresent(gc -> inv.addItem(buildCodeItem(key, gc)));
        }

        for (int i = 45; i < 54; i++) inv.setItem(i, panel());
        inv.setItem(INFO_SLOT, action(Material.BOOK,
                "page " + (page + 1) + " / " + totalPages,
                "total codes • " + codeKeys.size()));
        if (page > 0)              inv.setItem(PREV_SLOT, action(Material.ARROW, "previous page", "click to navigate"));
        if (page < totalPages - 1) inv.setItem(NEXT_SLOT, action(Material.ARROW, "next page",     "click to navigate"));

        return inv;
    }


    private ItemStack buildCodeItem(String key, Giftcode gc) {
        String inf = plugin.getMessageConfig().getInfinitySymbol();
        int used = plugin.getCodeManager().globalUseCount(key);

        boolean expired  = gc.isExpired();
        boolean enabled  = gc.isEnabled();

        Material mat = expired  ? Material.PAPER :
                enabled  ? Material.NAME_TAG :
                Material.BARRIER;

        String statusColor = expired ? C_BAD : enabled ? C_GOOD : C_WARN;
        String statusText  = expired ? "expired" : enabled ? "enabled" : "disabled";

        int total = used + gc.getMaxUses();
        String usesColor = usageColor(used, total);

        List<String> lore = new ArrayList<>();

        lore.add(loreLine("status", statusText, statusColor));
        lore.add(loreSep());

        lore.add(loreLine("uses", used + " / " + total, usesColor));
        lore.add(loreLine("player limit",
                gc.isUnlimitedPlayerUses() ? inf : String.valueOf(gc.getPlayerMaxUses()),
                C_SPECIAL));
        lore.add(loreLine("ip limit",
                gc.hasIpRestriction() ? String.valueOf(gc.getMaxUsesPerIp()) : "disabled",
                gc.hasIpRestriction() ? C_WARN : C_HINT));

        lore.add(loreLine("expiry",
                gc.getExpiry().isBlank() ? inf : gc.getExpiry(),
                expired ? C_BAD : gc.getExpiry().isBlank() ? C_GOOD : C_WARN));

        lore.add(loreLine("permission",
                gc.hasPermissionRestriction() ? gc.getPermission() : "none",
                gc.hasPermissionRestriction() ? C_SPECIAL : C_HINT));
        lore.add(loreLine("playtime",
                gc.hasPlaytimeRequirement() ? gc.getRequiredPlaytimeMinutes() + " min" : "none",
                gc.hasPlaytimeRequirement() ? C_WARN : C_HINT));

        lore.add(loreLine("commands", String.valueOf(gc.getCommands().size()), C_VALUE));
        lore.add(loreLine("items",    String.valueOf(gc.getItemRewards().size()), C_VALUE));

        lore.add("");
        lore.add(loreAction("left click • edit settings"));
        lore.add(loreAction("right click • edit items"));
        lore.add(loreAction("shift click • " + C_BAD + sc("delete")));

        return new ItemBuilder(mat)
                .name("&f" + key)
                .lore(lore)
                .glow(enabled && !expired)
                .build();
    }

    private String usageColor(int used, int max) {
        if (max <= 0) return C_BAD;
        double ratio = (double) used / max;
        if (ratio >= 1.0) return C_BAD;
        if (ratio >= 0.75) return C_WARN;
        return C_GOOD;
    }


    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(inventory)) return;

        int slot = event.getSlot();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (slot == PREV_SLOT && clicked.getType() == Material.ARROW) { navigate(page - 1); return; }
        if (slot == NEXT_SLOT && clicked.getType() == Material.ARROW) { navigate(page + 1); return; }
        if (slot >= 45) return;

        String codeName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        plugin.getCodeManager().find(codeName).ifPresent(gc -> {
            ClickType click = event.getClick();
            if (click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT) {
                plugin.getCodeManager().delete(codeName);
                viewer.playSound(viewer.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                viewer.sendMessage(plugin.getMessageConfig().get("code-deleted", Map.of("code", codeName)));
                plugin.getGuiListener().deregister(viewer.getUniqueId());
                new CodeListGUI(plugin, viewer, page).open();
            } else if (click.isRightClick()) {
                viewer.playSound(viewer.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
                plugin.getGuiListener().deregister(viewer.getUniqueId());
                new ItemEditorGUI(plugin, viewer, gc).open();
            } else {
                viewer.playSound(viewer.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                plugin.getGuiListener().deregister(viewer.getUniqueId());
                new CodeEditorGUI(plugin, viewer, gc).open();
            }
        });
    }

    private void navigate(int newPage) {
        viewer.playSound(viewer.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
        plugin.getGuiListener().deregister(viewer.getUniqueId());
        new CodeListGUI(plugin, viewer, newPage).open();
    }
}