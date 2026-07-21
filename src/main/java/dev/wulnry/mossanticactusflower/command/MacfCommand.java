package dev.wulnry.mossanticactusflower.command;

import dev.wulnry.mossanticactusflower.MossAntiCactusFlower;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Handles the {@code /mossanticactusflower} (alias: {@code /macf}) command.
 *
 * <p>Supported sub-commands:
 * <ul>
 *   <li>{@code /macf reload} — Reloads the plugin configuration from disk.</li>
 * </ul>
 *
 * @author wulnrydev
 */
public final class MacfCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION_ADMIN = "mossanticactusflower.admin";
    private static final String SUB_RELOAD       = "reload";

    private final MossAntiCactusFlower plugin;

    /**
     * Constructs the command handler bound to the owning plugin.
     *
     * @param plugin the active {@link MossAntiCactusFlower} instance
     */
    public MacfCommand(final MossAntiCactusFlower plugin) {
        this.plugin = plugin;
    }

    // ---------------------------------------------------------------
    // CommandExecutor
    // ---------------------------------------------------------------

    @Override
    public boolean onCommand(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String label,
            @NotNull final String[] args
    ) {
        // Permission guard.
        if (!sender.hasPermission(PERMISSION_ADMIN)) {
            sender.sendMessage(plugin.getMessage("messages.no-permission"));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase(SUB_RELOAD)) {
            handleReload(sender);
            return true;
        }

        sender.sendMessage(plugin.getMessage("messages.unknown-command"));
        return true;
    }

    // ---------------------------------------------------------------
    // TabCompleter
    // ---------------------------------------------------------------

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String alias,
            @NotNull final String[] args
    ) {
        if (!sender.hasPermission(PERMISSION_ADMIN)) {
            return List.of();
        }

        if (args.length == 1) {
            final String partial = args[0].toLowerCase();
            return SUB_RELOAD.startsWith(partial) ? List.of(SUB_RELOAD) : List.of();
        }

        return List.of();
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    /**
     * Reloads the plugin configuration and reports the duration to the sender.
     *
     * @param sender the command sender to notify
     */
    private void handleReload(final CommandSender sender) {
        final long start = System.currentTimeMillis();

        plugin.reloadConfig();
        // Refresh in-memory caches (prefix, enabled, debug) from the new file.
        plugin.refreshCache();

        final long elapsed = System.currentTimeMillis() - start;
        sender.sendMessage(plugin.getMessage(
                "messages.reload-success",
                "%time%", String.valueOf(elapsed)
        ));
    }
}
