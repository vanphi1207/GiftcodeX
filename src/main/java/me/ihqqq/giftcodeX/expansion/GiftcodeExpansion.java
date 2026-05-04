package me.ihqqq.giftcodeX.expansion;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.ihqqq.giftcodeX.GiftcodeX;
import me.ihqqq.giftcodeX.manager.CodeManager;
import me.ihqqq.giftcodeX.manager.PlayerDataManager;
import me.ihqqq.giftcodeX.model.Giftcode;
import me.ihqqq.giftcodeX.model.PlaytimeDuration;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Available placeholders:
 *
 *  %giftcodex_total_codes%
 *  %giftcodex_player_totalused%
 *  %giftcodex_code_exists_<code>%
 *  %giftcodex_code_enabled_<code>%
 *  %giftcodex_code_expired_<code>%
 *  %giftcodex_code_maxuses_<code>%
 *  %giftcodex_code_used_<code>%
 *  %giftcodex_code_expiry_<code>%
 *  %giftcodex_code_permission_<code>%
 *  %giftcodex_code_playtime_<code>%
 *  %giftcodex_code_playtime_minutes_<code>%
 *  %giftcodex_code_playtime_ms_<code>%
 *  %giftcodex_code_playeruses_<code>%
 *  %giftcodex_code_canuseip_<code>%
 *  %giftcodex_code_playerlimit_<code>%
 *  %giftcodex_code_iplimit_<code>%
 *  %giftcodex_code_cooldown_<code>%
 *  %giftcodex_code_cooldownleft_<code>%
 */
public final class GiftcodeExpansion extends PlaceholderExpansion {

    private final GiftcodeX plugin;
    private final CodeManager codeManager;
    private final PlayerDataManager playerDataManager;

    public GiftcodeExpansion(GiftcodeX plugin, CodeManager codeManager, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.codeManager = codeManager;
        this.playerDataManager = playerDataManager;
    }

    @Override public @NotNull String getIdentifier() { return "giftcodex"; }
    @Override public @NotNull String getAuthor()     { return String.join(", ", plugin.getDescription().getAuthors()); }
    @Override public @NotNull String getVersion()    { return plugin.getDescription().getVersion(); }
    @Override public boolean persist()               { return true; }
    @Override public boolean canRegister()           { return true; }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {

        String inf = plugin.getMessageConfig().getInfinitySymbol();

        if (params.equals("total_codes")) {
            return String.valueOf(codeManager.getAll().size());
        }

        if (params.equals("player_totalused")) {
            if (player == null) return "0";
            return String.valueOf(playerDataManager.getOrCreate(player.getUniqueId()).getUsedCodes().size());
        }

        if (!params.startsWith("code_")) return null;

        String rest = params.substring(5);

        if (rest.startsWith("playtime_minutes_")) {
            String codeName = rest.substring("playtime_minutes_".length());
            return codeManager.find(codeName)
                    .map(gc -> String.valueOf(gc.getRequiredPlaytime().toTotalMinutes()))
                    .orElse("0");
        }
        if (rest.startsWith("playtime_ms_")) {
            String codeName = rest.substring("playtime_ms_".length());
            return codeManager.find(codeName)
                    .map(gc -> String.valueOf(gc.getRequiredPlaytime().toMilliseconds()))
                    .orElse("0");
        }

        int sep = rest.indexOf('_');
        if (sep < 0) return null;

        String subaction = rest.substring(0, sep);
        String codeName  = rest.substring(sep + 1);
        Optional<Giftcode> opt = codeManager.find(codeName);

        return switch (subaction) {
            case "exists"     -> String.valueOf(opt.isPresent());
            case "enabled"    -> opt.map(gc -> String.valueOf(gc.isEnabled())).orElse("false");
            case "expired"    -> opt.map(gc -> String.valueOf(gc.isExpired())).orElse("true");
            case "maxuses"    -> opt.map(gc -> String.valueOf(gc.getMaxUses())).orElse("0");
            case "used"       -> String.valueOf(codeManager.globalUseCount(codeName));
            case "expiry"     -> opt.map(gc -> gc.getExpiry().isBlank() ? inf : gc.getExpiry()).orElse("N/A");
            case "permission" -> opt.map(gc -> gc.hasPermissionRestriction() ? gc.getPermission() : "None").orElse("N/A");

            case "playtime"   -> opt.map(gc -> {
                PlaytimeDuration pt = gc.getRequiredPlaytime();
                return pt.isZero() ? "0m" : pt.toDisplayString();
            }).orElse("0m");

            case "playeruses" -> {
                if (player == null || opt.isEmpty()) yield "0";
                yield String.valueOf(playerDataManager.getOrCreate(player.getUniqueId()).useCount(codeName));
            }

            case "canuseip" -> {
                if (player == null || opt.isEmpty()) yield "false";
                Giftcode gc = opt.get();
                if (!gc.hasIpRestriction()) yield "true";
                String ip = playerDataManager.getOrCreate(player.getUniqueId()).getLastKnownIp();
                yield String.valueOf(playerDataManager.ipUseCount(ip, codeName) < gc.getMaxUsesPerIp());
            }

            case "playerlimit" -> opt.map(gc -> gc.isUnlimitedPlayerUses() ? inf : String.valueOf(gc.getPlayerMaxUses())).orElse("0");
            case "iplimit"     -> opt.map(gc -> gc.hasIpRestriction() ? String.valueOf(gc.getMaxUsesPerIp()) : inf).orElse("0");

            case "cooldown" -> opt.map(gc -> gc.hasCooldown() ? String.valueOf(gc.getCooldownSeconds()) : "0").orElse("0");

            case "cooldownleft" -> {
                if (player == null || opt.isEmpty()) yield "0s";
                Giftcode gc = opt.get();
                if (!gc.hasCooldown()) yield "0s";
                long lastTime    = playerDataManager.getLastRedeemTime(player.getUniqueId(), codeName);
                if (lastTime == 0) yield "0s";
                long remainingMs = gc.getCooldownSeconds() * 1000L - (System.currentTimeMillis() - lastTime);
                yield remainingMs > 0 ? me.ihqqq.giftcodeX.command.RedeemCommand.formatRemaining(remainingMs) : "0s";
            }

            default -> null;
        };
    }
}