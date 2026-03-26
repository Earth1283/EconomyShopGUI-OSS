package io.github.Earth1283.economyShopGUIOSS.marketplace

/**
 * Typed result of a marketplace HTTP operation.
 *
 * Pattern-match on this rather than catching exceptions:
 * ```kotlin
 * when (val result = client.download("DIAMONDS")) {
 *     is MarketplaceResult.Success    -> installer.install(result.value)
 *     is MarketplaceResult.Failure.NotFound -> sender.sendMessage("Layout not found.")
 *     is MarketplaceResult.Failure.RateLimited -> sender.sendMessage("Try again in ${result.retryAfterSeconds}s.")
 *     else -> sender.sendMessage("Error: ${result}")
 * }
 * ```
 */
sealed class MarketplaceResult<out T> {

    data class Success<T>(val value: T) : MarketplaceResult<T>()

    sealed class Failure : MarketplaceResult<Nothing>() {
        /** The requested layout code was not found on the marketplace. */
        data class NotFound(val code: String) : Failure()
        /** The Bearer token is missing or invalid. */
        data object Unauthorized : Failure()
        /** The request body failed server-side validation. */
        data class ValidationError(val body: String) : Failure()
        /** The server returned an unexpected status. */
        data class ServerError(val status: Int, val body: String) : Failure()
        /** The response body could not be parsed. */
        data class ParseError(val reason: String) : Failure()
        /** Rate limit exceeded; retry after [retryAfterSeconds] seconds. */
        data class RateLimited(val retryAfterSeconds: Int) : Failure()
        /** Network or I/O failure. */
        data class NetworkError(val cause: Throwable) : Failure()
    }

    val isSuccess: Boolean get() = this is Success
}

// ── Result data types ─────────────────────────────────────────────────────────

/** Metadata about a layout (no YAML content). */
data class LayoutMeta(
    val id: String,
    val code: String,
    val name: String,
    val description: String?,
    val author: String,
    val downloads: Int,
    val updatedAt: Long,
)

/** Full layout download including decoded YAML content. */
data class LayoutDownload(
    val meta: LayoutMeta,
    /** Decoded UTF-8 content for `sections/<code>.yml`. */
    val sectionYaml: String,
    /** Decoded UTF-8 content for `shops/<code>.yml`. */
    val shopYaml: String,
)

/** Result of uploading a new layout. */
data class LayoutUploadResult(
    val id: String,
    /** Short share code, e.g. `"DIAMONDS"`. */
    val code: String,
    /** Public URL for sharing the layout. */
    val url: String,
)
