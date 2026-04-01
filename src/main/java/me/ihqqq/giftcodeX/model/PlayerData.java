package me.ihqqq.giftcodeX.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class PlayerData {
    private final UUID uuid;
    private String lastKnownIp;
    private final List<String> usedCodes;
    private final List<String> assignedCodes;

    public PlayerData(UUID uuid, String lastKnownIp, List<String> usedCodes, List<String> assignedCodes) {
        this.uuid = uuid;
        this.lastKnownIp = lastKnownIp == null ? "" : lastKnownIp;
        this.usedCodes = new ArrayList<>(usedCodes != null ? usedCodes : List.of());
        this.assignedCodes = new ArrayList<>(assignedCodes != null ? assignedCodes : List.of());
    }

    public static PlayerData empty(UUID uuid) {
        return new PlayerData(uuid, "", List.of(), List.of());
    }


    public void recordUsedCode(String code) {
        usedCodes.add(code);
    }

    public void recordAssignedCode(String code) {
        if (!assignedCodes.contains(code)) {
            assignedCodes.add(code);
        }
    }

    public void updateIp(String ip) {
        this.lastKnownIp = ip == null ? "" : ip;
    }

    public void removeCode(String code) {
        usedCodes.removeIf(c -> c.equals(code));
        assignedCodes.removeIf(c -> c.equals(code));
    }

    public int useCount(String code) {
        return Collections.frequency(usedCodes, code);
    }

    public UUID getUuid() { return uuid; }
    public String getLastKnownIp() { return lastKnownIp; }
    public List<String> getUsedCodes() { return Collections.unmodifiableList(usedCodes); }
    public List<String> getAssignedCodes() { return Collections.unmodifiableList(assignedCodes); }
}
