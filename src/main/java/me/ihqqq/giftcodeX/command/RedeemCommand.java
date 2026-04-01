package me.ihqqq.giftcodeX.command;

import me.ihqqq.giftcodeX.GiftcodeX;
import me.ihqqq.giftcodeX.model.RedeemResult;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public final class RedeemCommand implements CommandExecutor {

    private final GiftcodeX plugin;

    public RedeemCommand(GiftcodeX plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can redeem gift codes.");
            return true;
        }
        if (!player.hasPermission("giftcodex.player")) {
            player.sendMessage(plugin.getMessageConfig().get("no-permission-admin"));
            return true;
        }
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /redeem <code>");
            return true;
        }

        String code   = args[0];
        RedeemResult result = plugin.getCodeManager().redeem(player, code);

        if (result == RedeemResult.NOT_ENOUGH_PLAYTIME) {
            plugin.getCodeManager().find(code).ifPresent(gc -> {
                int current = player.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE) / (20 * 60);
                player.sendMessage(plugin.getMessageConfig().get(result.getMessageKey(), Map.of(
                        "required", String.valueOf(gc.getRequiredPlaytimeMinutes()),
                        "current",  String.valueOf(current)
                )));
            });
        } else {
            player.sendMessage(plugin.getMessageConfig().getForResult(result));
        }
        return true;
    }
}