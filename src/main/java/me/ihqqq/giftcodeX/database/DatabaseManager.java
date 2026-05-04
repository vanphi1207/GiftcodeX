package me.ihqqq.giftcodeX.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.ihqqq.giftcodeX.GiftcodeX;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public final class DatabaseManager {

    public enum Type { H2, MYSQL }

    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS player_data (
                uuid           VARCHAR(36)  NOT NULL PRIMARY KEY,
                last_ip        VARCHAR(45)  NOT NULL DEFAULT '',
                used_codes     MEDIUMTEXT   NOT NULL DEFAULT '',
                assigned_codes MEDIUMTEXT   NOT NULL DEFAULT ''
            )
            """;

    private static final String CREATE_COOLDOWN_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS cooldown_timestamps (
                uuid        VARCHAR(36) NOT NULL,
                code        VARCHAR(64) NOT NULL,
                last_redeem BIGINT      NOT NULL DEFAULT 0,
                PRIMARY KEY (uuid, code)
            )
            """;

    private final GiftcodeX plugin;
    private HikariDataSource dataSource;
    private Type activeType;

    public DatabaseManager(GiftcodeX plugin) { this.plugin = plugin; }

    public boolean initialize() {
        String typeStr = plugin.getConfigManager().getDatabaseType().toUpperCase();
        try {
            activeType = Type.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Unknown database type '" + typeStr + "', falling back to H2.");
            activeType = Type.H2;
        }

        if (tryConnect(buildConfig(activeType))) return true;

        // MySQL failed — try H2 in-memory fallback
        if (activeType == Type.MYSQL) {
            plugin.getLogger().warning("MySQL connection failed. Attempting H2 fallback...");
            activeType = Type.H2;
            if (tryConnect(buildH2Config(true))) {
                plugin.getLogger().info("Using H2 in-memory fallback.");
                return true;
            }
        }
        return false;
    }

    private boolean tryConnect(HikariConfig config) {
        try {
            dataSource = new HikariDataSource(config);
            createTables();
            plugin.getLogger().info("Connected to " + activeType + " database.");
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to database!", e);
            return false;
        }
    }

    private void createTables() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt  = conn.createStatement()) {
            stmt.execute(CREATE_TABLE_SQL);
            stmt.execute(CREATE_COOLDOWN_TABLE_SQL);
        }
    }


    private HikariConfig buildConfig(Type type) {
        return type == Type.MYSQL ? buildMySQLConfig() : buildH2Config(false);
    }

    private HikariConfig buildH2Config(boolean inMemory) {
        HikariConfig cfg = new HikariConfig();
        if (inMemory) {
            cfg.setJdbcUrl("jdbc:h2:mem:giftcodex;DB_CLOSE_DELAY=-1;MODE=MySQL");
        } else {
            File dbFile = new File(plugin.getDataFolder(), "playerdata");
            cfg.setJdbcUrl("jdbc:h2:file:" + dbFile.getAbsolutePath() + ";MODE=MySQL;AUTO_SERVER=TRUE");
        }
        cfg.setDriverClassName("org.h2.Driver");
        cfg.setUsername("sa");
        cfg.setPassword("");
        cfg.setMaximumPoolSize(5);
        cfg.setMinimumIdle(1);
        cfg.setConnectionTimeout(10_000);
        cfg.setPoolName("GiftCodeX-H2");
        return cfg;
    }

    private HikariConfig buildMySQLConfig() {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("database.mysql");
        String host = sec.getString("host", "localhost");
        int    port = sec.getInt("port", 3306);
        String db   = sec.getString("database", "giftcodex");
        String user = sec.getString("username", "root");
        String pass = sec.getString("password", "");

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + db
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=utf8");
        cfg.setDriverClassName("com.mysql.cj.jdbc.Driver");
        cfg.setUsername(user);
        cfg.setPassword(pass);
        cfg.setMaximumPoolSize(15);
        cfg.setMinimumIdle(3);
        cfg.setConnectionTimeout(30_000);
        cfg.setIdleTimeout(300_000);
        cfg.setMaxLifetime(600_000);
        cfg.addDataSourceProperty("cachePrepStmts",          "true");
        cfg.addDataSourceProperty("prepStmtCacheSize",       "250");
        cfg.addDataSourceProperty("prepStmtCacheSqlLimit",   "2048");
        cfg.addDataSourceProperty("useServerPrepStmts",      "true");
        cfg.addDataSourceProperty("rewriteBatchedStatements","true");
        cfg.setPoolName("GiftCodeX-MySQL");
        return cfg;
    }


    public Connection getConnection() throws SQLException {
        if (dataSource == null) throw new SQLException("DataSource is not initialised.");
        return dataSource.getConnection();
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Database connection pool closed.");
        }
    }

    public Type getActiveType() { return activeType; }
}