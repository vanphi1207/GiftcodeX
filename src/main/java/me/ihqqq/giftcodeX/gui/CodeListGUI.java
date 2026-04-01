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
        int end = Math.min(start + PAGE_SIZE, codeKeys.size());
        for (int i = start; i < end; i++) {
            String key = codeKeys.get(i);
            plugin.getCodeManager().find(key).ifPresent(gc -> inv.addItem(buildCodeItem(key, gc)));
        }

        for (int i = 45; i < 54; i++) inv.setItem(i, panel());
        inv.setItem(INFO_SLOT, action(Material.BOOK,
                "page " + (page + 1) + " / " + totalPages, "total codes • " + codeKeys.size()));
        if (page > 0)             inv.setItem(PREV_SLOT, action(Material.ARROW, "previous page", "click to navigate"));
        if (page < totalPages - 1) inv.setItem(NEXT_SLOT, action(Material.ARROW, "next page",     "click to navigate"));

        return inv;
    }

    private ItemStack buildCodeItem(String key, Giftcode gc) {
        int used   = plugin.getCodeManager().globalUseCount(key);
        String status  = gc.isEnabled() ? (gc.isExpired() ? "expired" : "enabled") : "disabled";
        Material mat   = gc.isExpired() ? Material.PAPER : gc.isEnabled() ? Material.NAME_TAG : Material.BARRIER;

        return new ItemBuilder(mat)
                .name("&f" + key)
                .lore(styleLore(
                        "status • " + status,
                        "uses • " + used + " / " + gc.getMaxUses(),
                        "player limit • " + (gc.isUnlimitedPlayerUses() ? "unlimited" : gc.getPlayerMaxUses()),
                        "ip limit • " + (gc.hasIpRestriction() ? gc.getMaxUsesPerIp() : "disabled"),
                        "expiry • " + (gc.getExpiry().isBlank() ? "never" : gc.getExpiry()),
                        "permission • " + (gc.hasPermissionRestriction() ? gc.getPermission() : "none"),
                        "playtime • " + (gc.hasPlaytimeRequirement() ? gc.getRequiredPlaytimeMinutes() + " min" : "none"),
                        "items • " + gc.getItemRewards().size(),
                        "",
                        "left click • edit",
                        "right click • items",
                        "shift click • delete"
                ))
                .glow(gc.isEnabled() && !gc.isExpired())
                .build();
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(inventory)) return;

        int slot = event.getSlot();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (slot == PREV_SLOT && clicked.getType() == Material.ARROW) {
            navigate(page - 1);
            return;
        }
        if (slot == NEXT_SLOT && clicked.getType() == Material.ARROW) {
            navigate(page + 1);
            return;
        }
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