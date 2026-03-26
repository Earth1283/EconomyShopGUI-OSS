package io.github.Earth1283.economyShopGUIOSS.util

import org.bukkit.Bukkit

/**
 * Runtime server-capability detection helpers.
 *
 * All properties use lazy initialisation so the class-loading cost is paid
 * only when first accessed, and each check runs at most once.
 */
object VersionUtils {

    /** True when the server is running under Folia (threaded-region scheduling). */
    val isFolia: Boolean by lazy {
        runCatching {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
        }.isSuccess
    }

    /** True when the server is a Paper fork (or Paper itself). */
    val isPaper: Boolean by lazy {
        runCatching { Class.forName("io.papermc.paper.configuration.Configuration") }.isSuccess ||
        runCatching { Class.forName("com.destroystokyo.paper.PaperConfig") }.isSuccess
    }

    /** The server's full version string as reported by Bukkit. */
    val serverVersion: String get() = Bukkit.getServer().version

    /** The Minecraft data version string (e.g. "1.21"). */
    val minecraftVersion: String get() = Bukkit.getServer().minecraftVersion
}
