package io.github.Earth1283.economyShopGUIOSS.util

import org.bukkit.plugin.java.JavaPlugin

/**
 * Unified scheduling facade for both Bukkit and Folia.
 *
 * Folia uses per-region schedulers; Bukkit uses a single global one.
 * All callers in this plugin should go through this object rather than
 * touching [org.bukkit.scheduler.BukkitScheduler] or Folia APIs directly,
 * so that the codebase stays compatible with both server flavours.
 *
 * **Important:** The Minecraft world state must only ever be mutated from a
 * sync (region) context. Use [runAsync] exclusively for I/O (database writes,
 * HTTP calls, file operations) — never for Bukkit API calls.
 */
object SchedulerUtils {

    private val isFolia: Boolean by lazy { VersionUtils.isFolia }

    // ── Fire-and-forget ───────────────────────────────────────────────────────

    /** Run [block] asynchronously (off the main thread / any region thread). */
    fun runAsync(plugin: JavaPlugin, block: () -> Unit) {
        if (isFolia) {
            plugin.server.asyncScheduler.runNow(plugin) { block() }
        } else {
            plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable(block))
        }
    }

    /** Run [block] on the main thread / global region on the next tick. */
    fun runSync(plugin: JavaPlugin, block: () -> Unit) {
        if (isFolia) {
            plugin.server.globalRegionScheduler.run(plugin) { block() }
        } else {
            plugin.server.scheduler.runTask(plugin, Runnable(block))
        }
    }

    // ── Delayed ───────────────────────────────────────────────────────────────

    /** Run [block] asynchronously after [delayTicks] server ticks. */
    fun runAsyncLater(plugin: JavaPlugin, delayTicks: Long, block: () -> Unit) {
        if (isFolia) {
            plugin.server.asyncScheduler.runDelayed(
                plugin,
                { block() },
                delayTicks * 50,
                java.util.concurrent.TimeUnit.MILLISECONDS
            )
        } else {
            plugin.server.scheduler.runTaskLaterAsynchronously(plugin, Runnable(block), delayTicks)
        }
    }

    /** Run [block] synchronously after [delayTicks] server ticks. */
    fun runLater(plugin: JavaPlugin, delayTicks: Long, block: () -> Unit) {
        if (isFolia) {
            plugin.server.globalRegionScheduler.runDelayed(plugin, { block() }, delayTicks)
        } else {
            plugin.server.scheduler.runTaskLater(plugin, Runnable(block), delayTicks)
        }
    }

    // ── Repeating ─────────────────────────────────────────────────────────────

    /** Run [block] asynchronously every [periodTicks] ticks after [delayTicks]. */
    fun runAsyncTimer(plugin: JavaPlugin, delayTicks: Long, periodTicks: Long, block: () -> Unit) {
        if (isFolia) {
            plugin.server.asyncScheduler.runAtFixedRate(
                plugin,
                { block() },
                delayTicks * 50,
                periodTicks * 50,
                java.util.concurrent.TimeUnit.MILLISECONDS
            )
        } else {
            plugin.server.scheduler.runTaskTimerAsynchronously(
                plugin, Runnable(block), delayTicks, periodTicks
            )
        }
    }

    /** Run [block] synchronously every [periodTicks] ticks after [delayTicks]. */
    fun runTimer(plugin: JavaPlugin, delayTicks: Long, periodTicks: Long, block: () -> Unit) {
        if (isFolia) {
            plugin.server.globalRegionScheduler.runAtFixedRate(
                plugin, { block() }, delayTicks, periodTicks
            )
        } else {
            plugin.server.scheduler.runTaskTimer(
                plugin, Runnable(block), delayTicks, periodTicks
            )
        }
    }
}
