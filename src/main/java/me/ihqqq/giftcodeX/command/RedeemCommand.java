package me.ihqqq.giftcodeX.command;

import me.ihqqq.giftcodeX.GiftcodeX;
import me.ihqqq.giftcodeX.model.PlaytimeDuration;
import me.ihqqq.giftcodeX.model.RedeemResult;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
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
                long ticksPlayed   = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
                long msPlayed      = (ticksPlayed * 1000L) / 20L;
                PlaytimeDuration current  = buildCurrentDuration(msPlayed);
                PlaytimeDuration required = gc.getRequiredPlaytime();

                player.sendMessage(plugin.getMessageConfig().get(result.getMessageKey(), Map.of(
                        "required", required.toDisplayString(),
                        "current",  current.toDisplayString()
                )));
            });
        } else {
            player.sendMessage(plugin.getMessageConfig().getForResult(result));
        }
        return true;
    }

    private static PlaytimeDuration buildCurrentDuration(long ms) {
        final long MS_YEAR  = 365L * 24 * 60 * 60 * 1000;
        final long MS_MONTH =  30L * 24 * 60 * 60 * 1000;
        final long MS_WEEK  =   7L * 24 * 60 * 60 * 1000;
        final long MS_DAY   =       24L * 60 * 60 * 1000;
        final long MS_HOUR  =            60L * 60 * 1000;
        final long MS_MIN   =                 60L * 1000;
        final long MS_SEC   =                      1000L;

        long rem = ms;
        int years = (int)(rem / MS_YEAR);   rem %= MS_YEAR;
        int months= (int)(rem / MS_MONTH);  rem %= MS_MONTH;
        int weeks = (int)(rem / MS_WEEK);   rem %= MS_WEEK;
        int days  = (int)(rem / MS_DAY);    rem %= MS_DAY;
        int hours = (int)(rem / MS_HOUR);   rem %= MS_HOUR;
        int mins  = (int)(rem / MS_MIN);    rem %= MS_MIN;
        int secs  = (int)(rem / MS_SEC);    rem %= MS_SEC;
        int millis= (int) rem;

        return new PlaytimeDuration.Builder()
                .years(years).months(months).weeks(weeks).days(days)
                .hours(hours).minutes(mins).seconds(secs).milliseconds(millis)
                .build();
    }
}