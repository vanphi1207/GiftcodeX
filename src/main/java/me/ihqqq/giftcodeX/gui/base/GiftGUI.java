package me.ihqqq.giftcodeX.gui.base;

import me.ihqqq.giftcodeX.GiftcodeX;
import me.ihqqq.giftcodeX.util.FoliaUtils;
import me.ihqqq.giftcodeX.util.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class GiftGUI {

    private static final String SMALL_CAP_FROM = "abcdefghijklmnopqrstuvwxyz";
    private static final String[] SMALL_CAP_TO = {
            "ᴀ", "ʙ", "ᴄ", "ᴅ", "ᴇ", "ꜰ", "ɢ", "ʜ", "ɪ", "ᴊ", "ᴋ", "ʟ", "ᴍ",
            "ɴ", "ᴏ", "ᴘ", "ǫ", "ʀ", "ꜱ", "ᴛ", "ᴜ", "ᴠ", "ᴡ", "x", "ʏ", "ᴢ"
    };


    protected static final String C_LABEL   = "§7";
    protected static final String C_VALUE   = "§f";
    protected static final String C_GOOD    = "§a";
    protected static final String C_BAD     = "§c";
    protected static final String C_WARN    = "§e";
    protected static final String C_SPECIAL = "§b";
    protected static final String C_HINT    = "§8";
    protected static final String C_SEP     = "§8";

    protected final GiftcodeX plugin;
    protected final Player viewer;
    protected Inventory inventory;

    protected GiftGUI(GiftcodeX plugin, Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
    }



    public final void open() {
        FoliaUtils.runForPlayer(plugin, viewer, () -> {
            this.inventory = buildInventory();
            viewer.openInventory(inventory);
            plugin.getGuiListener().register(viewer.getUniqueId(), this);
        });
    }

    protected abstract Inventory buildInventory();

    public abstract void handleClick(InventoryClickEvent event);

    public void handleDrag(InventoryDragEvent event)   { event.setCancelled(true); }
    public void handleClose(InventoryCloseEvent event) { }

    protected void close() {
        plugin.getGuiListener().deregister(viewer.getUniqueId());
        FoliaUtils.runForPlayer(plugin, viewer, viewer::closeInventory);
    }


    protected ItemStack panel() {
        return new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("&0").build();
    }

    protected ItemStack softPanel() {
        return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&0").build();
    }

    protected ItemStack action(Material material, String name, String... lore) {
        return new ItemBuilder(material)
                .name("&f" + sc(name))
                .lore(hintLore(lore))
                .build();
    }

    protected ItemStack actionGlow(Material material, String name, String... lore) {
        return new ItemBuilder(material)
                .name("&f" + sc(name))
                .lore(hintLore(lore))
                .glow()
                .build();
    }

    protected List<String> styleLore(String... lines) {
        List<String> lore = new ArrayList<>();
        for (String line : lines) {
            lore.add(line == null || line.isEmpty() ? "" : C_HINT + sc(line));
        }
        return lore;
    }

    protected List<String> hintLore(String... lines) {
        List<String> lore = new ArrayList<>();
        for (String line : lines) {
            lore.add(line == null || line.isEmpty() ? "" : C_HINT + sc(line));
        }
        return lore;
    }

    protected String loreLine(String label, String value, String color) {
        return C_LABEL + sc(label + " • ") + color + sc(value);
    }


    protected String loreSep() {
        return C_SEP + "  ─────────────────";
    }


    protected String loreAction(String text) {
        return C_HINT + sc("⬡ " + text);
    }


    protected String title(String text) {
        return ChatColor.WHITE + sc(text);
    }

    protected String sc(String text) {
        if (text == null || text.isEmpty()) return "";
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ((c == '§' || c == '&') && i + 1 < text.length()) {
                out.append(c).append(text.charAt(++i));
                continue;
            }
            int idx = SMALL_CAP_FROM.indexOf(Character.toLowerCase(c));
            out.append(idx >= 0 ? SMALL_CAP_TO[idx] : c);
        }
        return out.toString();
    }
}