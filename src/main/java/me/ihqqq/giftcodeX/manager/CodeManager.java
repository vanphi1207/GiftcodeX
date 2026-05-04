package me.ihqqq.giftcodeX.manager;

import me.ihqqq.giftcodeX.GiftcodeX;
import me.ihqqq.giftcodeX.model.Giftcode;
import me.ihqqq.giftcodeX.model.PlayerData;
import me.ihqqq.giftcodeX.model.PlaytimeDuration;
import me.ihqqq.giftcodeX.model.RedeemResult;
import me.ihqqq.giftcodeX.storage.CodeRepository;
import me.ihqqq.giftcodeX.util.ColorUtil;
import me.ihqqq.giftcodeX.util.FoliaUtils;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.security.SecureRandom;
import java.util.*;

public final class CodeManager {
    private static final String RANDOM_CHARS      = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789";
    private static final int    RANDOM_SUFFIX_LEN = 8;

    public static final int UNLIMITED_USES = Integer.MAX_VALUE;

    private final GiftcodeX plugin;
    private final CodeRepository repository;
    private final PlayerDataManager playerDataManager;

    private final Map<String, Giftcode> codes;
    private final SecureRandom rng = new SecureRandom();

    public CodeManager(GiftcodeX plugin, CodeRepository repository, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.repository = repository;
        this.playerDataManager = playerDataManager;
        this.codes = Collections.synchronizedMap(new LinkedHashMap<>(repository.loadAll()));
    }

    public Optional<Giftcode> find(String code) {
        return Optional.ofNullable(codes.get(code));
    }

    public boolean exists(String code) {
        return codes.containsKey(code);
    }

    public Giftcode create(Giftcode giftCode) {
        codes.put(giftCode.getCode(), giftCode);
        saveAll();
        return giftCode;
    }

    public void update(Giftcode giftCode) {
        codes.put(giftCode.getCode(), giftCode);
        saveAll();
    }

    public boolean delete(String code) {
        if (!codes.containsKey(code)) return false;
        codes.remove(code);
        saveAll();
        playerDataManager.purgeCode(code);
        return true;
    }

    public void saveAll() {
        repository.saveAll(new LinkedHashMap<>(codes));
    }

    public void reloadFromDisk() {
        repository.reload();
        codes.clear();
        codes.putAll(repository.loadAll());
    }

    public List<String> listCodes() {
        return new ArrayList<>(codes.keySet());
    }

    public Map<String, Giftcode> getAll() {
        return Collections.unmodifiableMap(codes);
    }

    public int globalUseCount(String code) {
        return playerDataManager.globalUseCount(code);
    }

    public int generateRandom(String prefix, int amount) {
        return generateRandomFromTemplate(prefix, amount, null);
    }

    public int generateRandomFromTemplate(String prefix, int amount, String templateCode) {
        Giftcode template = templateCode != null ? codes.get(templateCode) : null;
        if (templateCode != null && template == null) return -1;

        int created = 0;
        for (int i = 0; i < amount && created < amount; i++) {
            String code = generateUniqueCode(prefix);
            if (code == null) continue;

            Giftcode.Builder builder = template != null
                    ? template.toBuilder()
                    : defaultBuilder();

            codes.put(code, builder.build());
            created++;
        }
        saveAll();
        return created;
    }

    private String generateUniqueCode(String prefix) {
        for (int attempt = 0; attempt < 100; attempt++) {
            String candidate = prefix + randomSuffix();
            if (!codes.containsKey(candidate)) return candidate;
        }
        return null;
    }

    private String randomSuffix() {
        StringBuilder sb = new StringBuilder(RANDOM_SUFFIX_LEN);
        for (int i = 0; i < RANDOM_SUFFIX_LEN; i++) {
            sb.append(RANDOM_CHARS.charAt(rng.nextInt(RANDOM_CHARS.length())));
        }
        return sb.toString();
    }

    private Giftcode.Builder defaultBuilder() {
        return new Giftcode.Builder("__tmp__")
                .commands(List.of("give %player% diamond 1"))
                .messages(List.of("&aYou received &e1 Diamond&a!"))
                .maxUses(99)
                .expiry("2099-12-31T23:59:59")
                .enabled(true)
                .playerMaxUses(1)
                .maxUsesPerIp(1)
                .requiredPlaytime(PlaytimeDuration.zero())
                .cooldownSeconds(plugin.getConfigManager().getDefaultCooldownSeconds());
    }

    public RedeemResult redeem(Player player, String rawCode) {
        Giftcode gc = codes.get(rawCode);
        if (gc == null)         return RedeemResult.INVALID_CODE;
        if (!gc.isEnabled())    return RedeemResult.CODE_DISABLED;
        if (gc.isExpired())     return RedeemResult.CODE_EXPIRED;

        boolean unlimitedGlobal = gc.getMaxUses() >= UNLIMITED_USES;
        if (!unlimitedGlobal && gc.getMaxUses() <= 0) return RedeemResult.MAX_USES_REACHED;

        if (gc.hasPermissionRestriction() && !player.hasPermission(gc.getPermission())) {
            return RedeemResult.NO_PERMISSION;
        }

        if (gc.hasPlaytimeRequirement()) {
            long ticksPlayed = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
            long msPlayed    = (ticksPlayed * 1000L) / 20L;
            if (msPlayed < gc.getRequiredPlaytime().toMilliseconds()) {
                return RedeemResult.NOT_ENOUGH_PLAYTIME;
            }
        }

        PlayerData pd = playerDataManager.getOrCreate(player.getUniqueId());
        if (!gc.isUnlimitedPlayerUses() && pd.useCount(rawCode) >= gc.getPlayerMaxUses()) {
            return RedeemResult.ALREADY_REDEEMED;
        }

        if (gc.hasCooldown()) {
            long lastTime = playerDataManager.getLastRedeemTime(player.getUniqueId(), rawCode);
            if (lastTime > 0) {
                long elapsed   = System.currentTimeMillis() - lastTime;
                long cooldownMs = gc.getCooldownSeconds() * 1000L;
                if (elapsed < cooldownMs) {
                    return RedeemResult.ON_COOLDOWN;
                }
            }
        }

        if (gc.hasIpRestriction()) {
            String ip = resolveIp(player);
            if (playerDataManager.ipUseCount(ip, rawCode) >= gc.getMaxUsesPerIp()) {
                return RedeemResult.IP_LIMIT_REACHED;
            }
        }

        executeRewards(player, gc);

        if (!unlimitedGlobal) {
            codes.put(rawCode, gc.withMaxUses(gc.getMaxUses() - 1));
        }
        playerDataManager.recordRedemption(player, rawCode);
        saveAll();

        return RedeemResult.SUCCESS;
    }

    public void assign(Player player, String code) {
        Giftcode gc = codes.get(code);
        if (gc == null) return;
        executeRewards(player, gc);
        playerDataManager.recordAssignment(player.getUniqueId(), code);
    }

    private void executeRewards(Player player, Giftcode gc) {
        if (!gc.getCommands().isEmpty()) {
            List<String> cmds = gc.getCommands().stream()
                    .map(c -> c.replace("%player%", player.getName()))
                    .toList();

            FoliaUtils.runGlobal(plugin, () -> {
                for (String cmd : cmds) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            });
        }

        if (!gc.getItemRewards().isEmpty() || !gc.getMessages().isEmpty()) {
            List<ItemStack> items = gc.getItemRewards();
            List<String>    msgs  = gc.getMessages();
            String color          = plugin.getConfigManager().getRewardColor();

            FoliaUtils.runForPlayer(plugin, player, () -> {
                for (ItemStack item : items) {
                    if (item == null) continue;
                    Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item.clone());
                    leftovers.values().forEach(lf ->
                            player.getWorld().dropItemNaturally(player.getLocation(), lf));
                }
                for (String msg : msgs) {
                    player.sendMessage(ColorUtil.colorize(color + msg));
                }
            });
        }
    }


    private String resolveIp(Player player) {
        try {
            if (player.getAddress() != null && player.getAddress().getAddress() != null) {
                return player.getAddress().getAddress().getHostAddress();
            }
        } catch (Exception ignored) {}
        return "";
    }
}