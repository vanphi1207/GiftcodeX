package me.ihqqq.giftcodeX.gui;

import me.ihqqq.giftcodeX.GiftcodeX;
import me.ihqqq.giftcodeX.gui.base.GiftGUI;
import me.ihqqq.giftcodeX.model.Giftcode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class ItemEditorGUI extends GiftGUI {
    private static final int ITEM_SLOTS = 45;
    private static final int SLOT_BACK  = 45;
    private static final int SLOT_INFO  = 49;
    private static final int SLOT_SAVE  = 53;

    private final Giftcode gc;

    public ItemEditorGUI(GiftcodeX plugin, Player viewer, Giftcode gc) {
        super(plugin, viewer);
        this.gc = gc;
    }

    @Override
    protected Inventory buildInventory() {
        Inventory inv = Bukkit.createInventory(null, 54, title("item rewards • " + gc.getCode()));

        List<ItemStack> rewards = gc.getItemRewards();
        for (int i = 0; i < Math.min(rewards.size(), ITEM_SLOTS); i++) {
            ItemStack item = rewards.get(i);
            if (item != null && item.getType() != Material.AIR) {
                inv.setItem(i, item.clone());
            }
        }

        for (int i = 45; i < 54; i++) inv.setItem(i, panel());

        inv.setItem(SLOT_BACK, action(Material.ARROW, "back to settings",
                "return to code editor"));

        inv.setItem(SLOT_INFO, action(Material.CHEST, "item rewards editor",
                "place items in the grid above",
                "items are given on redeem",
                "",
                "code • " + gc.getCode(),
                "current items • " + gc.getItemRewards().size()));

        inv.setItem(SLOT_SAVE, actionGlow(Material.EMERALD, "save items",
                "save item rewards"));

        return inv;
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        if (slot < 0) return;

        if (event.getSlot() >= ITEM_SLOTS && event.getClickedInventory() != null
                && event.getClickedInventory().equals(inventory)) {
            event.setCancelled(true);

            if (event.getSlot() == SLOT_BACK) {
                saveAndReturn();
            } else if (event.getSlot() == SLOT_SAVE) {
                saveItems();
                viewer.playSound(viewer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
                viewer.sendMessage(colorize("&a✔ Saved &e" + collectItems().size() + " &aitem(s) for code &e" + gc.getCode() + "&a."));
                close();
            }
        }
    }

    @Override
    public void handleDrag(InventoryDragEvent event) {
        boolean touchesNavRow = event.getRawSlots().stream().anyMatch(s -> s >= ITEM_SLOTS && s < 54);
        if (touchesNavRow) {
            event.setCancelled(true);
        }
    }

    @Override
    public void handleClose(InventoryCloseEvent event) {
        // Auto-save on close
        saveItems();
        viewer.sendMessage(colorize("&7Item rewards auto-saved for code &e" + gc.getCode() + "&7."));
    }


    private void saveItems() {
        List<ItemStack> items = collectItems();
        Giftcode updated = gc.withItemRewards(items);
        plugin.getCodeManager().update(updated);
        viewer.sendMessage(plugin.getMessageConfig().get("items-saved",
                java.util.Map.of("count", String.valueOf(items.size()), "code", gc.getCode())));
    }

    private void saveAndReturn() {
        List<ItemStack> items = collectItems();
        Giftcode updated = gc.withItemRewards(items);
        plugin.getCodeManager().update(updated);
        plugin.getGuiListener().deregister(viewer.getUniqueId());
        new CodeEditorGUI(plugin, viewer, updated).open();
    }

    private List<ItemStack> collectItems() {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < ITEM_SLOTS; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                items.add(item.clone());
            }
        }
        return items;
    }

    private String colorize(String s) { return s.replace("&", "§"); }
}
