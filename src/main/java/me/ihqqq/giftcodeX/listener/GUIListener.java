package me.ihqqq.giftcodeX.listener;

import me.ihqqq.giftcodeX.GiftcodeX;
import me.ihqqq.giftcodeX.gui.base.GiftGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GUIListener implements Listener {

    private final Map<UUID, GiftGUI> openGUIs = new HashMap<>();

    public GUIListener(GiftcodeX plugin) {  }

    public void register(UUID uuid, GiftGUI gui)    { openGUIs.put(uuid, gui); }
    public void deregister(UUID uuid)               { openGUIs.remove(uuid); }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        GiftGUI gui = openGUIs.get(player.getUniqueId());
        if (gui != null) gui.handleClick(event);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        GiftGUI gui = openGUIs.get(player.getUniqueId());
        if (gui != null) gui.handleDrag(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        GiftGUI gui = openGUIs.remove(player.getUniqueId());
        if (gui != null) gui.handleClose(event);
    }
}