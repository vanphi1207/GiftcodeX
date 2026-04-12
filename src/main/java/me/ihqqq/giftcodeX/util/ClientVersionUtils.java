package me.ihqqq.giftcodeX.util;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class ClientVersionUtils {

    private static final int PROTOCOL_1_21_6 = 771;

    private static final boolean IS_DIALOG_API_AVAILABLE;
    static {
        boolean available = false;
        try {
            Class.forName("io.papermc.paper.dialog.Dialog");
            available = true;
        } catch (ClassNotFoundException ignored) {}
        IS_DIALOG_API_AVAILABLE = available;
    }

    private static volatile Object viaApi = null;
    private static volatile boolean viaChecked = false;

    private ClientVersionUtils() {}

    public static boolean isDialogApiAvailable() {
        return IS_DIALOG_API_AVAILABLE;
    }


    public static boolean supportsDialog(Player player) {
        return IS_DIALOG_API_AVAILABLE && getClientProtocol(player) >= PROTOCOL_1_21_6;
    }


    public static int getClientProtocol(Player player) {
        Object api = getViaApi(player);
        if (api instanceof com.viaversion.viaversion.api.ViaAPI<?> via) {
            try {
                @SuppressWarnings("unchecked")
                com.viaversion.viaversion.api.ViaAPI<Player> typedVia =
                        (com.viaversion.viaversion.api.ViaAPI<Player>) via;
                return typedVia.getPlayerVersion(player);
            } catch (Exception ignored) {
            }
        }

        try {
            return player.getProtocolVersion();
        } catch (NoSuchMethodError | Exception ignored) {}

        return PROTOCOL_1_21_6;
    }


    private static Object getViaApi(Player player) {
        if (!viaChecked) {
            synchronized (ClientVersionUtils.class) {
                if (!viaChecked) {
                    Plugin via = player.getServer().getPluginManager().getPlugin("ViaVersion");
                    if (via != null) {
                        try {
                            viaApi = com.viaversion.viaversion.api.Via.getAPI();
                        } catch (Exception ignored) {
                            viaApi = null;
                        }
                    }
                    viaChecked = true;
                }
            }
        }
        return viaApi;
    }
}