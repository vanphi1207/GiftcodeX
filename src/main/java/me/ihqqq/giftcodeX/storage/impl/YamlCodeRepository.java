package me.ihqqq.giftcodeX.storage.impl;

import me.ihqqq.giftcodeX.model.Giftcode;
import me.ihqqq.giftcodeX.model.PlaytimeDuration;
import me.ihqqq.giftcodeX.storage.CodeRepository;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class YamlCodeRepository implements CodeRepository {

    private static final String FILE_NAME = "codes.yml";

    private final JavaPlugin plugin;
    private final File file;
    private FileConfiguration config;

    public YamlCodeRepository(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(FILE_NAME, false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public Map<String, Giftcode> loadAll() {
        Map<String, Giftcode> map = new LinkedHashMap<>();
        for (String key : config.getKeys(false)) {
            try {
                Giftcode code = deserialize(key);
                if (code != null) map.put(key, code);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load gift code: " + key, e);
            }
        }
        return map;
    }

    @Override
    public void saveAll(Map<String, Giftcode> codes) {
        for (String key : new ArrayList<>(config.getKeys(false))) {
            config.set(key, null);
        }
        codes.forEach(this::serialize);
        flush();
    }

    @Override
    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }


    private Giftcode deserialize(String key) {
        String base = key + ".";

        List<String> commands = config.getStringList(base + "commands");
        List<String> messages = toStringList(config.get(base + "messages"));

        List<ItemStack> items = new ArrayList<>();
        List<?> rawItems = config.getList(base + "items");
        if (rawItems != null) {
            for (Object o : rawItems) {
                if (o instanceof ItemStack is) items.add(is);
            }
        }

        PlaytimeDuration playtime = deserializePlaytime(base);

        return new Giftcode.Builder(key)
                .commands(commands)
                .messages(messages)
                .maxUses(config.getInt(base + "max-uses", 100))
                .expiry(config.getString(base + "expiry", ""))
                .enabled(config.getBoolean(base + "enabled", true))
                .playerMaxUses(config.getInt(base + "player-max-uses", 1))
                .maxUsesPerIp(config.getInt(base + "max-uses-per-ip", 1))
                .requiredPlaytime(playtime)
                .permission(config.getString(base + "permission", ""))
                .itemRewards(items)
                .cooldownSeconds(config.getLong(base + "cooldown-seconds", 0))
                .build();
    }

    private PlaytimeDuration deserializePlaytime(String base) {
        String sectionKey = base + "required-playtime";
        Object raw = config.get(sectionKey);

        if (raw instanceof Number number) {
            return PlaytimeDuration.ofMinutes(number.intValue());
        }

        if (config.isConfigurationSection(sectionKey)) {
            return new PlaytimeDuration.Builder()
                    .years(config.getInt(sectionKey + ".years", 0))
                    .months(config.getInt(sectionKey + ".months", 0))
                    .weeks(config.getInt(sectionKey + ".weeks", 0))
                    .days(config.getInt(sectionKey + ".days", 0))
                    .hours(config.getInt(sectionKey + ".hours", 0))
                    .minutes(config.getInt(sectionKey + ".minutes", 0))
                    .seconds(config.getInt(sectionKey + ".seconds", 0))
                    .milliseconds(config.getInt(sectionKey + ".milliseconds", 0))
                    .build();
        }

        return PlaytimeDuration.zero();
    }

    private void serialize(String key, Giftcode gc) {
        String base = key + ".";
        config.set(base + "commands",          gc.getCommands());
        config.set(base + "messages",          gc.getMessages());
        config.set(base + "max-uses",          gc.getMaxUses());
        config.set(base + "expiry",            gc.getExpiry());
        config.set(base + "enabled",           gc.isEnabled());
        config.set(base + "player-max-uses",   gc.getPlayerMaxUses());
        config.set(base + "max-uses-per-ip",   gc.getMaxUsesPerIp());
        config.set(base + "cooldown-seconds",  gc.getCooldownSeconds());
        config.set(base + "permission",        gc.getPermission());
        config.set(base + "items",             gc.getItemRewards().isEmpty() ? null : gc.getItemRewards());
        serializePlaytime(base, gc.getRequiredPlaytime());
    }

    private void serializePlaytime(String base, PlaytimeDuration d) {
        String section = base + "required-playtime";
        config.set(section + ".years",        d.getYears());
        config.set(section + ".months",       d.getMonths());
        config.set(section + ".weeks",        d.getWeeks());
        config.set(section + ".days",         d.getDays());
        config.set(section + ".hours",        d.getHours());
        config.set(section + ".minutes",      d.getMinutes());
        config.set(section + ".seconds",      d.getSeconds());
        config.set(section + ".milliseconds", d.getMilliseconds());
    }

    private void flush() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save " + FILE_NAME + "!", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> toStringList(Object raw) {
        if (raw instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            for (Object o : list) out.add(String.valueOf(o));
            return out;
        }
        if (raw instanceof String s) return List.of(s);
        return new ArrayList<>();
    }
}