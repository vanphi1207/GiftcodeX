package me.ihqqq.giftcodeX.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {

    private static final Pattern HEX_PATTERN =
            Pattern.compile("&#([0-9A-Fa-f]{6})");

    private ColorUtil() {}

    public static String colorize(String text) {
        if (text == null || text.isEmpty()) return text == null ? "" : text;
        text = translateHex(text);
        return text.replace("&", "§");
    }
    public static String translateHex(String text) {
        if (text == null || !text.contains("&#")) return text;
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder(text.length() + 32);
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(c);
            }
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static Component toComponent(String text) {
        return LegacyComponentSerializer.legacySection().deserialize(colorize(text));
    }

    public static String strip(String text) {
        if (text == null) return "";
        text = text.replaceAll("§x(§[0-9A-Fa-f]){6}", "");
        text = text.replaceAll("§[0-9A-Fa-fK-ORk-or]", "");
        return text;
    }
}