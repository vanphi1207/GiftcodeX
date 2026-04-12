package me.ihqqq.giftcodeX;

import me.ihqqq.giftcodeX.command.GiftcodeCommand;
import me.ihqqq.giftcodeX.command.RedeemCommand;
import me.ihqqq.giftcodeX.config.ConfigManager;
import me.ihqqq.giftcodeX.config.MessageConfig;
import me.ihqqq.giftcodeX.database.DatabaseManager;
import me.ihqqq.giftcodeX.expansion.GiftcodeExpansion;
import me.ihqqq.giftcodeX.expansion.UpdateChecker;
import me.ihqqq.giftcodeX.listener.GUIListener;
import me.ihqqq.giftcodeX.listener.PlayerListener;
import me.ihqqq.giftcodeX.manager.CodeManager;
import me.ihqqq.giftcodeX.manager.PlayerDataManager;
import me.ihqqq.giftcodeX.storage.impl.YamlCodeRepository;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class GiftcodeX extends JavaPlugin {

    private static GiftcodeX instance;

    private ConfigManager     configManager;
    private MessageConfig     messageConfig;
    private DatabaseManager   databaseManager;
    private PlayerDataManager playerDataManager;
    private CodeManager       codeManager;
    private GUIListener       guiListener;

    @Override
    public void onEnable() {
        instance = this;

        if (!isSupportedVersion()) {
            getLogger().severe("========================================");
            getLogger().severe(" GiftcodeX requires server version 1.21.+");
            getLogger().severe("========================================");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        printBanner();

        configManager  = new ConfigManager(this);
        messageConfig  = new MessageConfig(this);
        guiListener    = new GUIListener(this);

        databaseManager = new DatabaseManager(this);
        if (!databaseManager.initialize()) {
            getLogger().severe("Failed to initialize database! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        playerDataManager = new PlayerDataManager(this, databaseManager);
        codeManager       = new CodeManager(this, new YamlCodeRepository(this), playerDataManager);

        registerCommands();
        registerListeners();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new GiftcodeExpansion(this, codeManager, playerDataManager).register();
            getLogger().info("PlaceholderAPI expansion registered.");
        }

        new Metrics(this, 30497);
        new UpdateChecker(this).start();

        getLogger().info("GiftCodeX v" + getDescription().getVersion() + " enabled successfully!");
        if (me.ihqqq.giftcodeX.util.FoliaUtils.isFolia()) {
            getLogger().info("Running on Folia.");
        }
    }

    @Override
    public void onDisable() {
        if (codeManager != null)    codeManager.saveAll();
        if (databaseManager != null) databaseManager.shutdown();
        getLogger().info("GiftCodeX disabled.");
    }


    private void registerCommands() {
        GiftcodeCommand adminCmd = new GiftcodeCommand(this);
        getCommand("giftcodex").setExecutor(adminCmd);
        getCommand("giftcodex").setTabCompleter(adminCmd);

        RedeemCommand redeemCmd = new RedeemCommand(this);
        getCommand("redeem").setExecutor(redeemCmd);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(guiListener,            this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    private boolean isSupportedVersion() {
        String[] v = getServer().getBukkitVersion().split("-")[0].split("\\.");
        int[] current  = { Integer.parseInt(v[0]), Integer.parseInt(v[1]), v.length > 2 ? Integer.parseInt(v[2]) : 0 };
        int[] required = { 1, 21, 0 };
        for (int i = 0; i < required.length; i++) {
            if (current[i] > required[i]) return true;
            if (current[i] < required[i]) return false;
        }
        return true;
    }

    private void printBanner() {
        Logger log = getLogger();
        log.info("");
        log.info("  ██████╗ ██╗███████╗████████╗ ██████╗ ██████╗ ██████╗ ███████╗██╗  ██╗");
        log.info(" ██╔════╝ ██║██╔════╝╚══██╔══╝██╔════╝██╔═══██╗██╔══██╗██╔════╝╚██╗██╔╝");
        log.info(" ██║  ███╗██║█████╗     ██║   ██║     ██║   ██║██║  ██║█████╗   ╚███╔╝ ");
        log.info(" ██║   ██║██║██╔══╝     ██║   ██║     ██║   ██║██║  ██║██╔══╝   ██╔██╗ ");
        log.info(" ╚██████╔╝██║██║        ██║   ╚██████╗╚██████╔╝██████╔╝███████╗██╔╝ ██╗");
        log.info("  ╚═════╝ ╚═╝╚═╝        ╚═╝    ╚═════╝ ╚═════╝ ╚═════╝ ╚══════╝╚═╝  ╚═╝");
        log.info("");
        log.info("  Version: " + getDescription().getVersion());
        log.info("  Author:  " + String.join(", ", getDescription().getAuthors()));
        log.info("");
    }


    public static GiftcodeX getInstance() { return instance; }


    public ConfigManager     getConfigManager()     { return configManager; }
    public MessageConfig     getMessageConfig()      { return messageConfig; }
    public PlayerDataManager getPlayerDataManager()  { return playerDataManager; }
    public CodeManager       getCodeManager()        { return codeManager; }
    public GUIListener       getGuiListener()        { return guiListener; }
}