package me.ihqqq.giftcodeX.config;

import me.ihqqq.giftcodeX.GiftcodeX;
import me.ihqqq.giftcodeX.model.RedeemResult;
import me.ihqqq.giftcodeX.util.ColorUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;

public final class MessageConfig {

    /** Supported language codes → their resource file names */
    private static final Map<String, String> LANGUAGE_FILES = Map.of(
            "en", "messages_en.yml",
            "vi", "messages_vi.yml"
    );

    /** Default language when an unknown code is configured */
    private static final String DEFAULT_LANGUAGE = "en";

    /**
     * Hard-coded English fallbacks used when a key is missing in the
     * active language file (prevents NPEs / blank messages).
     */
    private static final Map<String, String> FALLBACKS = Map.ofEntries(
            Map.entry("prefix",              "&8[&bGiftCodeX&8] "),
            Map.entry("infinity-symbol",     "∞"),
            Map.entry("redeemed",            "&aCode redeemed successfully!"),
            Map.entry("invalid-code",        "&cThis code does not exist."),
            Map.entry("code-disabled",       "&cThis code is currently disabled."),
            Map.entry("code-expired",        "&cThis code has expired."),
            Map.entry("max-uses-reached",    "&cThis code has reached its global limit."),
            Map.entry("already-redeemed",    "&cYou have already used this code the maximum number of times."),
            Map.entry("ip-limit-reached",    "&cThis code has been used too many times from your IP address."),
            Map.entry("not-enough-playtime", "&cYou need &e{required}&c of playtime. &7(Current: &e{current}&7)"),
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

    /** Currently active language file configuration */
    private FileConfiguration cfg;

    /** Currently loaded language code */
    private String currentLanguage;

    public MessageConfig(GiftcodeX plugin) {
        this.plugin = plugin;
        ensureLanguageFilesExist();
        reload();
    }


    public void reload() {
        currentLanguage = resolveLanguage(plugin.getConfigManager().getLanguage());
        String fileName = LANGUAGE_FILES.get(currentLanguage);

        ensureLanguageFilesExist();

        File file = new File(plugin.getDataFolder(), fileName);
        cfg = YamlConfiguration.loadConfiguration(file);

        InputStream resource = plugin.getResource(fileName);
        if (resource != null) {
            FileConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(resource, StandardCharsets.UTF_8));
            cfg.setDefaults(defaults);
        }

        plugin.getLogger().info("Language loaded: " + currentLanguage + " (" + fileName + ")");
    }

    public String getLanguage() {
        return currentLanguage;
    }

    public String getInfinitySymbol() {
        return cfg.getString("infinity-symbol", FALLBACKS.get("infinity-symbol"));
    }

    public String get(String key) {
        return get(key, Map.of());
    }

    public String get(String key, Map<String, String> placeholders) {
        String prefix = cfg.getString("prefix", FALLBACKS.get("prefix"));
        String raw    = cfg.getString(key, FALLBACKS.getOrDefault(key, "&c[Missing: " + key + "]"));

        String result = prefix + raw;
        for (Map.Entry<String, String> e : placeholders.entrySet()) {
            result = result.replace("{" + e.getKey() + "}", e.getValue());
        }
        return ColorUtil.colorize(result);
    }

    public String getForResult(RedeemResult result) {
        return get(result.getMessageKey());
    }

    private void ensureLanguageFilesExist() {
        for (String fileName : LANGUAGE_FILES.values()) {
            File target = new File(plugin.getDataFolder(), fileName);
            if (!target.exists()) {
                try {
                    plugin.saveResource(fileName, false);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().log(Level.WARNING,
                            "Could not save bundled resource: " + fileName, e);
                }
            }
        }
    }

    private String resolveLanguage(String configured) {
        if (LANGUAGE_FILES.containsKey(configured)) return configured;
        plugin.getLogger().warning(
                "Unknown language '" + configured + "' in config.yml. "
                        + "Falling back to '" + DEFAULT_LANGUAGE + "'. "
                        + "Available languages: " + String.join(", ", LANGUAGE_FILES.keySet()));
        return DEFAULT_LANGUAGE;
    }
}