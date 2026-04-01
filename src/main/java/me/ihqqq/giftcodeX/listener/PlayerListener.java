package me.ihqqq.giftcodeX.listener;

import me.ihqqq.giftcodeX.GiftcodeX;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerListener implements Listener {

    private final GiftcodeX plugin;

    public PlayerListener(GiftcodeX plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        plugin.getPlayerDataManager().updateIp(event.getPlayer());
    }
}