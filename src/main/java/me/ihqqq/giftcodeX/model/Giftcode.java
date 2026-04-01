package me.ihqqq.giftcodeX.model;

import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Giftcode {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final String code;
    private final List<String> commands;
    private final List<String> messages;
    private final List<ItemStack> itemRewards;
    private final String permission;
    private final String expiry;
    private final int maxUses;
    private final int playerMaxUses;   // -1 = unlimited
    private final int maxUsesPerIp;    // 0  = disabled
    private final int requiredPlaytimeMinutes;
    private final boolean enabled;

    private Giftcode(Builder b) {
        this.code = Objects.requireNonNull(b.code, "code");
        this.commands = Collections.unmodifiableList(new ArrayList<>(b.commands));
        this.messages = Collections.unmodifiableList(new ArrayList<>(b.messages));
        this.itemRewards = Collections.unmodifiableList(new ArrayList<>(b.itemRewards));
        this.permission = b.permission == null ? "" : b.permission;
        this.expiry = b.expiry == null ? "" : b.expiry.trim();
        this.maxUses = b.maxUses;
        this.playerMaxUses = b.playerMaxUses;
        this.maxUsesPerIp = b.maxUsesPerIp;
        this.requiredPlaytimeMinutes = b.requiredPlaytimeMinutes;
        this.enabled = b.enabled;
    }


    public Giftcode withMaxUses(int maxUses)               { return toBuilder().maxUses(maxUses).build(); }
    public Giftcode withEnabled(boolean enabled)           { return toBuilder().enabled(enabled).build(); }
    public Giftcode withPermission(String permission)      { return toBuilder().permission(permission).build(); }
    public Giftcode withItemRewards(List<ItemStack> items) { return toBuilder().itemRewards(items).build(); }
    public Giftcode withCommands(List<String> commands)    { return toBuilder().commands(commands).build(); }
    public Giftcode withMessages(List<String> messages)    { return toBuilder().messages(messages).build(); }


    public boolean isExpired() {
        if (expiry == null || expiry.isBlank()) return false;
        String normalised = expiry
                .replace("Z", "")
                .replaceAll("[+-]\\d{2}:\\d{2}$", "")
                .trim();
        try {
            return LocalDateTime.now(ZoneId.systemDefault())
                    .isAfter(LocalDateTime.parse(normalised, FORMATTER));
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public boolean hasPermissionRestriction()  { return permission != null && !permission.isBlank(); }
    public boolean hasIpRestriction()          { return maxUsesPerIp > 0; }
    public boolean hasPlaytimeRequirement()    { return requiredPlaytimeMinutes > 0; }
    public boolean isUnlimitedPlayerUses()     { return playerMaxUses < 0; }


    public Builder toBuilder() {
        return new Builder(code)
                .commands(new ArrayList<>(commands))
                .messages(new ArrayList<>(messages))
                .itemRewards(new ArrayList<>(itemRewards))
                .permission(permission)
                .expiry(expiry)
                .maxUses(maxUses)
                .playerMaxUses(playerMaxUses)
                .maxUsesPerIp(maxUsesPerIp)
                .requiredPlaytimeMinutes(requiredPlaytimeMinutes)
                .enabled(enabled);
    }


    public String getCode()                    { return code; }
    public List<String> getCommands()          { return commands; }
    public List<String> getMessages()          { return messages; }
    public List<ItemStack> getItemRewards()    { return itemRewards; }
    public String getPermission()              { return permission; }
    public String getExpiry()                  { return expiry; }
    public int getMaxUses()                    { return maxUses; }
    public int getPlayerMaxUses()              { return playerMaxUses; }
    public int getMaxUsesPerIp()               { return maxUsesPerIp; }
    public int getRequiredPlaytimeMinutes()    { return requiredPlaytimeMinutes; }
    public boolean isEnabled()                 { return enabled; }


    public static final class Builder {
        private final String code;
        private List<String> commands = new ArrayList<>();
        private List<String> messages = new ArrayList<>();
        private List<ItemStack> itemRewards = new ArrayList<>();
        private String permission = "";
        private String expiry = "";
        private int maxUses = 100;
        private int playerMaxUses = 1;
        private int maxUsesPerIp = 1;
        private int requiredPlaytimeMinutes = 0;
        private boolean enabled = true;

        public Builder(String code) {
            this.code = Objects.requireNonNull(code, "code cannot be null");
        }

        public Builder commands(List<String> v)          { this.commands = new ArrayList<>(v); return this; }
        public Builder messages(List<String> v)          { this.messages = new ArrayList<>(v); return this; }
        public Builder itemRewards(List<ItemStack> v)    { this.itemRewards = new ArrayList<>(v); return this; }
        public Builder permission(String v)              { this.permission = v; return this; }
        public Builder expiry(String v)                  { this.expiry = v; return this; }
        public Builder maxUses(int v)                    { this.maxUses = v; return this; }
        public Builder playerMaxUses(int v)              { this.playerMaxUses = v; return this; }
        public Builder maxUsesPerIp(int v)               { this.maxUsesPerIp = v; return this; }
        public Builder requiredPlaytimeMinutes(int v)    { this.requiredPlaytimeMinutes = v; return this; }
        public Builder enabled(boolean v)                { this.enabled = v; return this; }

        public Giftcode build() { return new Giftcode(this); }
    }
}