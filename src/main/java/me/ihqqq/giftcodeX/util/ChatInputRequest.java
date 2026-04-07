package me.ihqqq.giftcodeX.util;

import me.ihqqq.giftcodeX.util.FoliaUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class ChatInputRequest implements Listener {

    private static final Set<UUID> PENDING =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final JavaPlugin plugin;
    private final Player player;
    private final String prompt;
    private final Consumer<String> callback;

    private ChatInputRequest(JavaPlugin plugin, Player player,
                             String prompt, Consumer<String> callback) {
        this.plugin   = plugin;
        this.player   = player;
        this.prompt   = prompt;
        this.callback = callback;
    }

    public static void prompt(JavaPlugin plugin, Player player,
                              String prompt, Consumer<String> callback) {
        if (PENDING.contains(player.getUniqueId())) return; // already waiting
        new ChatInputRequest(plugin, player, prompt, callback).start();
    }

    public static boolean isPending(UUID uuid) {
        return PENDING.contains(uuid);
    }



    private void start() {
        PENDING.add(player.getUniqueId());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        player.sendMessage(colorize("&8[&bGiftcodeX&8] " + prompt));
        player.sendMessage(colorize("&8  Type &ccancel &8to abort."));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().getUniqueId().equals(player.getUniqueId())) return;

        event.setCancelled(true);
        HandlerList.unregisterAll(this);
        PENDING.remove(player.getUniqueId());

        String input = event.getMessage().trim();
        boolean cancelled = input.equalsIgnoreCase("cancel");

        FoliaUtils.runForPlayer(plugin, player,
                () -> callback.accept(cancelled ? null : input));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        if (!event.getPlayer().getUniqueId().equals(player.getUniqueId())) return;
        HandlerList.unregisterAll(this);
        PENDING.remove(player.getUniqueId());
    }

    private static String colorize(String s) { return s.replace("&", "§"); }
}