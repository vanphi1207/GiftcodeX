package me.ihqqq.giftcodeX.expansion;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.ihqqq.giftcodeX.GiftcodeX;
import me.ihqqq.giftcodeX.util.FoliaUtils;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public final class UpdateChecker implements Listener {

    private static final String API_URL = "https://api.github.com/repos/vanphi1207/GiftCodeX/releases/latest";
    private static final long   CHECK_INTERVAL = 10 * 60 * 1_000L; // 10 minutes in ms

    private final GiftcodeX plugin;
    private volatile String latestVersion;
    private String etag;
    private long lastCheckTime;

    public UpdateChecker(GiftcodeX plugin) { this.plugin = plugin; }

    public void start() {
        if (!plugin.getConfigManager().isUpdateCheckEnabled()) return;

        FoliaUtils.runAsync(plugin, this::checkAsync);

        if (FoliaUtils.isFolia()) {
            plugin.getServer().getAsyncScheduler().runAtFixedRate(
                    plugin, task -> checkAsync(), 10, 600, TimeUnit.SECONDS);
        } else {
            plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                    plugin, this::checkAsync, 20L * 600, 20L * 600);
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void checkAsync() {
        long now = System.currentTimeMillis();
        if (now - lastCheckTime < CHECK_INTERVAL) return;
        lastCheckTime = now;

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "GiftcodeX");
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            if (etag != null) conn.setRequestProperty("If-None-Match", etag);
            conn.setConnectTimeout(5_000);
            conn.setReadTimeout(5_000);

            int responseCode = conn.getResponseCode();
            if (responseCode == 304) return; // not modified
            if (responseCode != 200) {
                plugin.getLogger().warning("Update check failed: HTTP " + responseCode);
                return;
            }

            etag = conn.getHeaderField("ETag");

            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                JsonObject json = JsonParser.parseReader(br).getAsJsonObject();
                String newVersion = json.get("tag_name").getAsString().replaceFirst("^v", "");
                String current    = plugin.getDescription().getVersion();

                if (isNewerVersion(newVersion, current) && !newVersion.equals(latestVersion)) {
                    plugin.getLogger().info("Update available: v" + newVersion + " (current: v" + current + ")");
                    latestVersion = newVersion;
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Update check error: " + e.getMessage());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (latestVersion == null) return;
        if (!isNewerVersion(latestVersion, plugin.getDescription().getVersion())) return;
        if (!event.getPlayer().hasPermission("giftcodex.admin")) return;

        var player  = event.getPlayer();
        String msg  = ChatColor.YELLOW + "[GiftcodeX] Update available: "
                + ChatColor.GOLD + "v" + latestVersion
                + ChatColor.YELLOW + " (current: v" + plugin.getDescription().getVersion() + ")";

        if (FoliaUtils.isFolia()) {
            plugin.getServer().getGlobalRegionScheduler()
                    .runDelayed(plugin, task -> player.sendMessage(msg), 40L);
        } else {
            plugin.getServer().getScheduler()
                    .runTaskLater(plugin, () -> player.sendMessage(msg), 40L);
        }
    }

    private static boolean isNewerVersion(String latest, String current) {
        String[] l = latest.split("\\.");
        String[] c = current.split("\\.");
        int len = Math.max(l.length, c.length);
        for (int i = 0; i < len; i++) {
            int lv = i < l.length  ? Integer.parseInt(l[i]) : 0;
            int cv = i < c.length  ? Integer.parseInt(c[i]) : 0;
            if (lv > cv) return true;
            if (lv < cv) return false;
        }
        return false;
    }
}