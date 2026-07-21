package dev.wulnry.mossanticactusflower;

import dev.wulnry.mossanticactusflower.command.MacfCommand;
import dev.wulnry.mossanticactusflower.listener.CactusFlowerListener;
import dev.wulnry.mossanticactusflower.util.ColorUtil;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Main entry-point for the <b>MossAntiCactusFlower</b> plugin.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Load and save default configuration</li>
 *   <li>Register event listeners</li>
 *   <li>Register plugin commands</li>
 *   <li>Provide the global config-message accessor</li>
 * </ul>
 *
 * @author wulnrydev
 */
public final class MossAntiCactusFlower extends JavaPlugin {

    /** Singleton reference for cross-class access. */
    private static MossAntiCactusFlower instance;

    /** Active listener — kept to allow cache refreshes on reload. */
    private CactusFlowerListener listener;

    /**
     * Pre-colorized message prefix, cached at enable/reload time.
     * Avoids re-reading config and re-running Regex on every {@link #getMessage} call.
     */
    private String cachedPrefix;

    // ---------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------

    @Override
    public void onEnable() {
        instance = this;

        // Save default config.yml if not already present.
        saveDefaultConfig();

        // Populate in-memory caches before first use.
        refreshCache();

        // Register event listener.
        listener = new CactusFlowerListener(this);
        getServer().getPluginManager().registerEvents(listener, this);

        // Register /macf command.
        final PluginCommand command = Objects.requireNonNull(
                getCommand("mossanticactusflower"),
                "Command 'mossanticactusflower' is missing from plugin.yml"
        );
        final MacfCommand executor = new MacfCommand(this);
        command.setExecutor(executor);
        command.setTabCompleter(executor);

        // Startup message.
        log(getMessage("messages.plugin-enabled"));
    }

    @Override
    public void onDisable() {
        log(getMessage("messages.plugin-disabled"));
        instance = null;
    }

    // ---------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------

    /**
     * Returns the singleton plugin instance.
     *
     * @return the active {@link MossAntiCactusFlower} instance
     */
    public static MossAntiCactusFlower getInstance() {
        return instance;
    }

    /**
     * Refreshes all in-memory caches derived from {@code config.yml}.
     *
     * <p>Must be called after every {@link #reloadConfig()} to keep cached
     * values (prefix, enabled-flag, debug-flag) consistent with the file on disk.
     * This method is idempotent and safe to call multiple times.
     */
    public void refreshCache() {
        final String rawPrefix = getConfig().getString(
                "general.prefix", "&7[&#00D420MossACF&7] ");
        cachedPrefix = ColorUtil.colorize(rawPrefix);

        // Propagate to the listener so its boolean caches are also refreshed.
        if (listener != null) {
            listener.refreshCache();
        }
    }

    /**
     * Retrieves a message from {@code config.yml}, prepends the configured
     * prefix, and colorizes the full string.
     *
     * @param path   the config path to the message (e.g. {@code "messages.reload-success"})
     * @param tokens optional placeholder pairs: {@code key, value, key, value, ...}
     * @return a fully colorized, prefix-aware message string
     */
    public String getMessage(final String path, final String... tokens) {
        String raw = getConfig().getString(path, "&cMissing message: " + path);

        // Apply placeholder replacements.
        if (tokens.length % 2 == 0) {
            for (int i = 0; i < tokens.length; i += 2) {
                raw = raw.replace(tokens[i], tokens[i + 1]);
            }
        }

        // Messages that already contain a prefix-like structure (startup/shutdown)
        // are sent as-is; regular messages get the cached, pre-colorized prefix.
        final boolean hasPrefix = path.startsWith("messages.plugin-");
        final String full = hasPrefix ? raw : cachedPrefix + " " + raw;

        return ColorUtil.colorize(full);
    }

    /**
     * Logs a colorized (strip) message to the plugin logger at INFO level.
     *
     * @param message the colorized message string
     */
    public void log(final String message) {
        final Logger logger = getLogger();
        logger.info(ColorUtil.strip(message));
    }
}
