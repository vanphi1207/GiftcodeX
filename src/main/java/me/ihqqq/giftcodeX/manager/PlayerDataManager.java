package me.ihqqq.giftcodeX.manager;

import me.ihqqq.giftcodeX.GiftcodeX;
import me.ihqqq.giftcodeX.database.DatabaseManager;
import me.ihqqq.giftcodeX.model.PlayerData;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class PlayerDataManager {

    private final GiftcodeX plugin;
    private final DatabaseManager db;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    private final Map<UUID, Map<String, Long>> cooldownTimestamps = new ConcurrentHashMap<>();

    public PlayerDataManager(GiftcodeX plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
        loadAll();
        loadCooldowns();
    }


    private void loadAll() {
        String sql = "SELECT uuid, last_ip, used_codes, assigned_codes FROM player_data";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                UUID uuid    = UUID.fromString(rs.getString("uuid"));
                String ip    = rs.getString("last_ip");
                List<String> used     = splitCodes(rs.getString("used_codes"));
                List<String> assigned = splitCodes(rs.getString("assigned_codes"));
                cache.put(uuid, new PlayerData(uuid, ip, used, assigned));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load player data!", e);
        }
    }

    private void loadCooldowns() {
        String sql = "SELECT uuid, code, last_redeem FROM cooldown_timestamps";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String code = rs.getString("code");
                long lastRedeem = rs.getLong("last_redeem");
                cooldownTimestamps
                        .computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                        .put(code, lastRedeem);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load cooldown data!", e);
        }
    }


    public PlayerData getOrCreate(UUID uuid) {
        return cache.computeIfAbsent(uuid, PlayerData::empty);
    }

    public void updateIp(Player player) {
        PlayerData data = getOrCreate(player.getUniqueId());
        data.updateIp(resolveIp(player));
        persistAsync(data);
    }

    public void recordRedemption(Player player, String code) {
        PlayerData data = getOrCreate(player.getUniqueId());
        data.recordUsedCode(code);
        data.updateIp(resolveIp(player));
        long now = System.currentTimeMillis();
        cooldownTimestamps
                .computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                .put(code, now);
        persistAsync(data);
        persistCooldownAsync(player.getUniqueId(), code, now);
    }

    public long getLastRedeemTime(UUID uuid, String code) {
        Map<String, Long> map = cooldownTimestamps.get(uuid);
        if (map == null) return 0L;
        return map.getOrDefault(code, 0L);
    }

    public void recordAssignment(UUID uuid, String code) {
        PlayerData data = getOrCreate(uuid);
        data.recordAssignedCode(code);
        persistAsync(data);
    }

    public void purgeCode(String code) {
        cache.values().forEach(pd -> pd.removeCode(code));
        cooldownTimestamps.values().forEach(map -> map.remove(code));
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            cache.values().forEach(this::persist);
            purgeCooldownsForCode(code);
            plugin.getLogger().info("Purged code '" + code + "' from all player records.");
        });
    }


    public int globalUseCount(String code) {
        return cache.values().stream().mapToInt(pd -> pd.useCount(code)).sum();
    }

    public int ipUseCount(String ip, String code) {
        if (ip == null || ip.isBlank()) return 0;
        return cache.values().stream()
                .filter(pd -> ip.equals(pd.getLastKnownIp()))
                .mapToInt(pd -> pd.useCount(code))
                .sum();
    }


    private void persistAsync(PlayerData data) {
        PlayerData snapshot = new PlayerData(
                data.getUuid(),
                data.getLastKnownIp(),
                new ArrayList<>(data.getUsedCodes()),
                new ArrayList<>(data.getAssignedCodes())
        );
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> persist(snapshot));
    }

    private void persistCooldownAsync(UUID uuid, String code, long lastRedeemMs) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            String sql = db.getActiveType() == DatabaseManager.Type.MYSQL
                    ? """
                      INSERT INTO cooldown_timestamps (uuid, code, last_redeem) VALUES (?, ?, ?)
                      ON DUPLICATE KEY UPDATE last_redeem=VALUES(last_redeem)
                      """
                    : """
                      MERGE INTO cooldown_timestamps (uuid, code, last_redeem) KEY(uuid, code) VALUES (?, ?, ?)
                      """;
            try (Connection conn = db.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, code);
                ps.setLong(3, lastRedeemMs);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to persist cooldown for " + uuid, e);
            }
        });
    }

    private void purgeCooldownsForCode(String code) {
        String sql = "DELETE FROM cooldown_timestamps WHERE code = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to purge cooldowns for code '" + code + "'", e);
        }
    }

    private void persist(PlayerData data) {
        String sql = db.getActiveType() == DatabaseManager.Type.MYSQL
                ? """
                  INSERT INTO player_data (uuid, last_ip, used_codes, assigned_codes) VALUES (?, ?, ?, ?)
                  ON DUPLICATE KEY UPDATE last_ip=VALUES(last_ip), used_codes=VALUES(used_codes), assigned_codes=VALUES(assigned_codes)
                  """
                : """
                  MERGE INTO player_data (uuid, last_ip, used_codes, assigned_codes) KEY(uuid) VALUES (?, ?, ?, ?)
                  """;

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, data.getUuid().toString());
            ps.setString(2, data.getLastKnownIp());
            ps.setString(3, String.join(",", data.getUsedCodes()));
            ps.setString(4, String.join(",", data.getAssignedCodes()));
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to persist player data for " + data.getUuid(), e);
        }
    }


    private static String resolveIp(Player player) {
        try {
            if (player.getAddress() != null && player.getAddress().getAddress() != null) {
                return player.getAddress().getAddress().getHostAddress();
            }
        } catch (Exception ignored) {}
        return "";
    }

    private static List<String> splitCodes(String raw) {
        if (raw == null || raw.isBlank()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(raw.split(",")));
    }
}