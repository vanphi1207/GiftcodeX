package me.ihqqq.giftcodeX.util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta  meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder name(String name) {
        meta.setDisplayName(colorize(name));
        return this;
    }

    public ItemBuilder lore(List<String> lines) {
        List<String> lore = new ArrayList<>();
        for (String line : lines) lore.add(colorize(line));
        meta.setLore(lore);
        return this;
    }

    public ItemBuilder appendLore(String... lines) {
        List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        for (String line : lines) lore.add(colorize(line));
        meta.setLore(lore);
        return this;
    }

    public ItemBuilder glow() {
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public ItemBuilder glow(boolean apply) {
        if (apply) glow();
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }

    private static String colorize(String s) {
        return s == null ? "" : s.replace("&", "§");
    }
}