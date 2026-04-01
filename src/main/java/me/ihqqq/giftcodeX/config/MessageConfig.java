package me.ihqqq.giftcodeX.config;

import me.ihqqq.giftcodeX.GiftcodeX;
import me.ihqqq.giftcodeX.model.RedeemResult;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;

public final class MessageConfig {

    private static final Map<String, String> DEFAULTS = Map.ofEntries(
            Map.entry("prefix",              "&8[&bGiftCodeX&8] "),
            Map.entry("redeemed",            "&aCode redeemed successfully!"),
            Map.entry("invalid-code",        "&cThis code does not exist."),
            Map.entry("code-disabled",       "&cThis code is currently disabled."),
            Map.entry("code-expired",        "&cThis code has expired."),
            Map.entry("max-uses-reached",    "&cThis code has reached its global limit."),
            Map.entry("already-redeemed",    "&cYou have already used this code the maximum number of times."),
            Map.entry("ip-limit-reached",    "&cThis code has been used too many times from your IP address."),
            Map.entry("not-enough-playtime", "&cYou need &e{required}&c minutes of playtime. &7(Current: &e{current}&7)"),
            Map.entry("no-permission",       "&cYou do not have permission to use this code."),
            Map.entry("assigned",            "&aYou have been granted a gift code by an administrator."),
            Map.entry("code-created",        "&aGift code &e{code} &acreated successfully."),
            Map.entry("code-deleted",        "&aGift code &e{code} &adeleted."),
            Map.entry("code-enabled",        "&aGift code &e{code} &aenabled."),
            Map.entry("code-disabled-admin", "&cGift code &e{code} &cdisabled."),
            Map.entry("code-not-found",      "&cGift code &e{code} &cnot found."),
            Map.entry("code-already-exists", "&cGift code &e{code} &calready exists."),
            Map.entry("player-not-found",    "&cPlayer &e{player} &cnot found or is offline."),
            Map.entry("no-permission-admin", "&cYou do not have permission to use this command."),
            Map.entry("plugin-reloaded",     "&aPlugin reloaded successfully."),
            Map.entry("random-generated",    "&aGenerated &e{amount} &acodes with prefix &e{prefix}&a."),
            Map.entry("permission-set",      "&aPermission for &e{code} &aset to &b{permission}&a."),
            Map.entry("permission-cleared",  "&aPermission requirement for &e{code} &acleared."),
            Map.entry("items-saved",         "&aSaved &e{count} &aitem reward(s) for code &e{code}&a.")
    );

    private final GiftcodeX plugin;
    private FileConfiguration cfg;

    public MessageConfig(GiftcodeX plugin) {
        this.plugin = plugin;
        plugin.saveResource("messages.yml", false);
        reload();
    }

    public void reload() {
        cfg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages.yml"));
    }

    public String get(String key) {
        return get(key, Map.of());
    }

    public String get(String key, Map<String, String> placeholders) {
        String prefix = cfg.getString("prefix", DEFAULTS.get("prefix"));
        String raw    = cfg.getString(key, DEFAULTS.getOrDefault(key, "&c[Missing: " + key + "]"));

        String result = (prefix + raw).replace("&", "§");
        for (Map.Entry<String, String> e : placeholders.entrySet()) {
            result = result.replace("{" + e.getKey() + "}", e.getValue());
        }
        return result;
    }

    public String getForResult(RedeemResult result) {
        return get(result.getMessageKey());
    }
}