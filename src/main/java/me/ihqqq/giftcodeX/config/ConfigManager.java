package me.ihqqq.giftcodeX.config;

import me.ihqqq.giftcodeX.GiftcodeX;
import org.bukkit.configuration.file.FileConfiguration;

public final class ConfigManager {

    private final GiftcodeX plugin;

    public ConfigManager(GiftcodeX plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    private FileConfiguration cfg() { return plugin.getConfig(); }

    public String getLanguage()               { return cfg().getString("language", "en").toLowerCase().trim(); }

    public String getDatabaseType()           { return cfg().getString("database.type", "H2"); }
    public boolean isUpdateCheckEnabled()     { return cfg().getBoolean("check-update", true); }
    public String getRewardColor()            { return cfg().getString("gui.reward-color", "&a"); }
    public String getDefaultExpiry()          { return cfg().getString("defaults.expiry", "2099-12-31T23:59:59"); }
    public int getDefaultMaxUses()            { return cfg().getInt("defaults.max-uses", 100); }
    public int getDefaultPlayerMaxUses()      { return cfg().getInt("defaults.player-max-uses", 1); }
    public int getDefaultMaxUsesPerIp()       { return cfg().getInt("defaults.max-uses-per-ip", 1); }
    public int getDefaultRequiredPlaytime()   { return cfg().getInt("defaults.required-playtime", 0); }

    public void reload() { plugin.reloadConfig(); }
}