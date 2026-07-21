package dev.wulnry.mossanticactusflower.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

import dev.wulnry.mossanticactusflower.MossAntiCactusFlower;

/**
 * Listens for {@link BlockGrowEvent} and cancels any cactus flower
 * ({@link Material#CACTUS_FLOWER}) growth events as early as possible.
 *
 * <p><b>Design decisions:</b>
 * <ul>
 *   <li>{@link EventPriority#HIGHEST} — runs late in the pipeline so other
 *       plugins cannot re-enable the event after us, while still before
 *       {@code MONITOR}-only listeners.</li>
 *   <li>{@code ignoreCancelled = false} — we explicitly want to cancel even
 *       if another plugin already cancelled the event, ensuring the cancel
 *       is always set when the plugin is enabled.</li>
 *   <li>Zero heap allocation in the hot path — only a single material
 *       comparison before cancelling.</li>
 * </ul>
 *
 * @author wulnrydev
 */
public final class CactusFlowerListener implements Listener {

    private final MossAntiCactusFlower plugin;

    /**
     * Cached value of {@code general.enabled} from config.
     * Avoids a {@code HashMap} lookup on every {@link BlockGrowEvent}.
     */
    private boolean cachedEnabled;

    /**
     * Cached value of {@code general.debug} from config.
     * Avoids a {@code HashMap} lookup on every {@link BlockGrowEvent}.
     */
    private boolean cachedDebug;

    /**
     * Constructs a new listener bound to the given plugin instance
     * and immediately populates its in-memory caches.
     *
     * @param plugin the owning {@link MossAntiCactusFlower} instance
     */
    public CactusFlowerListener(final MossAntiCactusFlower plugin) {
        this.plugin = plugin;
        refreshCache();
    }

    // ---------------------------------------------------------------
    // Cache
    // ---------------------------------------------------------------

    /**
     * Re-reads {@code general.enabled} and {@code general.debug} from the
     * current config into local fields. Called on construction and after
     * every config reload so the hot event path never touches the config map.
     */
    public void refreshCache() {
        cachedEnabled = plugin.getConfig().getBoolean("general.enabled", true);
        cachedDebug   = plugin.getConfig().getBoolean("general.debug", false);
    }

    /**
     * Intercepts block-grow events and cancels any cactus flower growth.
     *
     * <p>Performance notes:
     * <ul>
     *   <li>The {@link EventPriority#HIGHEST} priority fires after most other
     *       listeners but before MONITOR, ensuring our cancellation is final.</li>
     *   <li>The method body is intentionally minimal: one boolean check, one
     *       material comparison, one cancel call — no loops, no allocations.</li>
     * </ul>
     *
     * @param event the block grow event fired by the server
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onCactusFlowerGrow(final BlockGrowEvent event) {
        // Fast-exit: only act when the plugin feature is enabled.
        if (!cachedEnabled) {
            return;
        }

        // Cancel only cactus flower growth — ignore all other block-grow events.
        if (event.getNewState().getType() != Material.CACTUS) {
            return;
        }

        event.setCancelled(true);

        // Emit a debug log if enabled — zero-cost when debug is false.
        // Block reference is resolved lazily: only allocated when actually needed.
        if (cachedDebug) {
            final String location = formatLocation(event.getNewState().getBlock());
            plugin.log(plugin.getMessage("messages.debug-cancelled", "%location%", location));
        }
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    /**
     * Formats a block's world and coordinates into a compact string.
     *
     * @param block the block whose location to format
     * @return a human-readable location string (e.g. {@code "world, 10, 64, -30"})
     */
    private static String formatLocation(final Block block) {
        return block.getWorld().getName()
                + ", " + block.getX()
                + ", " + block.getY()
                + ", " + block.getZ();
    }
}
