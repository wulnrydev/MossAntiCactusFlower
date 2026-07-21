package dev.wulnry.mossanticactusflower.util;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for translating legacy and hex color codes into
 * Bukkit/BungeeCord chat color sequences.
 *
 * <p>Supports:
 * <ul>
 *   <li>Legacy color codes: {@code &a}, {@code &l}, etc.</li>
 *   <li>Hex RGB colors: {@code &#RRGGBB}</li>
 * </ul>
 *
 * @author wulnrydev
 */
public final class ColorUtil {

    /** Pattern that matches {@code &#RRGGBB} hex color codes. */
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private ColorUtil() {
        // Utility class — no instantiation.
    }

    /**
     * Translates a raw string containing {@code &} color codes and
     * {@code &#RRGGBB} hex color codes into a fully colorized string
     * ready to be sent to Minecraft clients.
     *
     * @param raw the raw input string
     * @return a colorized string compatible with Bukkit's chat system
     */
    public static String colorize(final String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }

        // First, replace &#RRGGBB with BungeeCord hex color sequences.
        final Matcher matcher = HEX_PATTERN.matcher(raw);
        final StringBuilder hexResolved = new StringBuilder();

        while (matcher.find()) {
            final String hexCode = matcher.group(1);
            matcher.appendReplacement(hexResolved, ChatColor.of("#" + hexCode).toString());
        }
        matcher.appendTail(hexResolved);

        // Then handle remaining legacy & codes.
        return ChatColor.translateAlternateColorCodes('&', hexResolved.toString());
    }

    /**
     * Strips all Bukkit/BungeeCord color codes from an already-colorized string,
     * returning plain text. Useful for console logging where color escapes are
     * not rendered.
     *
     * <p><b>Note:</b> This method expects a string that has already been processed
     * by {@link #colorize(String)} (i.e. containing {@code §} sequences, not raw
     * {@code &} codes). Passing a raw string will still work for ASCII text but
     * un-translated {@code &} codes will be left intact.
     *
     * @param colored a colorized string (output of {@link #colorize(String)})
     * @return the plain text without any color codes
     */
    public static String strip(final String colored) {
        return ChatColor.stripColor(colored);
    }
}
