package me.ihqqq.giftcodeX.model;

public enum RedeemResult {

    SUCCESS("redeemed"),
    INVALID_CODE("invalid-code"),
    CODE_DISABLED("code-disabled"),
    CODE_EXPIRED("code-expired"),
    MAX_USES_REACHED("max-uses-reached"),
    ALREADY_REDEEMED("already-redeemed"),
    IP_LIMIT_REACHED("ip-limit-reached"),
    NOT_ENOUGH_PLAYTIME("not-enough-playtime"),
    NO_PERMISSION("no-permission"),
    ON_COOLDOWN("on-cooldown"),
    ASSIGNED("assigned");

    private final String messageKey;

    RedeemResult(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }

}
