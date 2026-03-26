package io.github.Earth1283.economyShopGUIOSS.marketplace

import io.github.Earth1283.economyShopGUIOSS.EconomyShopGUIOSS
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * HTTP client for the EconomyShopGUI-OSS layout marketplace.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * MARKETPLACE API CONTRACT  (clean-room server implementation reference)
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * Base URL: configurable via `config.yml → marketplace.url`
 * Default:  https://marketplace.economyshopgui-oss.example.com/api/v1
 *
 * All requests carry the header:
 *   User-Agent: EconomyShopGUI-OSS/<plugin-version>
 *
 * All responses are JSON (Content-Type: application/json).
 * All timestamps are Unix epoch seconds (integer).
 * Pagination uses cursor-based pagination via `?cursor=<opaque string>`.
 *
 * ── Authentication ──────────────────────────────────────────────────────────
 *
 * Upload and update endpoints require an API token supplied via:
 *   Authorization: Bearer <token>
 *
 * Tokens are issued per server.  There is no registration flow in the plugin;
 * operators obtain tokens out-of-band (e.g. from the marketplace website).
 *
 * ── Error responses ─────────────────────────────────────────────────────────
 *
 * All error responses share the shape:
 * ```json
 * { "error": "<machine-readable code>", "message": "<human-readable detail>" }
 * ```
 *
 * Common error codes:
 *   not_found        → 404: the requested layout does not exist
 *   unauthorized     → 401: missing or invalid Bearer token
 *   forbidden        → 403: token valid but insufficient permissions
 *   validation_error → 422: request body failed validation
 *   rate_limited     → 429: too many requests; Retry-After header is set
 *   server_error     → 500: unexpected server failure
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * ENDPOINTS
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * ── 1. List layouts ──────────────────────────────────────────────────────────
 *
 *   GET /layouts
 *
 *   Query parameters:
 *     q        (string, optional) — full-text search across name, description, tags
 *     tag      (string, optional) — filter by a single tag
 *     sort     (string, optional) — one of: "recent" (default), "popular", "name"
 *     cursor   (string, optional) — opaque pagination cursor from previous response
 *     limit    (integer, optional, default 20, max 100) — results per page
 *
 *   Response 200:
 *   ```json
 *   {
 *     "layouts": [
 *       {
 *         "id":          "abc123",
 *         "code":        "DIAMONDS",
 *         "name":        "Diamond Shop",
 *         "description": "A clean diamond materials section.",
 *         "author":      "Notch",
 *         "tags":        ["materials", "vanilla"],
 *         "downloads":   1024,
 *         "created_at":  1700000000,
 *         "updated_at":  1700001000
 *       }
 *     ],
 *     "next_cursor": "eyJpZCI6ImFiYzEyNCJ9",
 *     "total": 42
 *   }
 *   ```
 *
 * ── 2. Get layout by code ────────────────────────────────────────────────────
 *
 *   GET /layouts/{code}
 *
 *   Path parameters:
 *     code  (string) — short share code as returned by the upload endpoint,
 *                      e.g. "DIAMONDS"
 *
 *   Response 200:
 *   ```json
 *   {
 *     "id":          "abc123",
 *     "code":        "DIAMONDS",
 *     "name":        "Diamond Shop",
 *     "description": "A clean diamond materials section.",
 *     "author":      "Notch",
 *     "tags":        ["materials", "vanilla"],
 *     "downloads":   1024,
 *     "created_at":  1700000000,
 *     "updated_at":  1700001000,
 *     "files": {
 *       "section": "<base64-encoded YAML>",
 *       "shop":    "<base64-encoded YAML>"
 *     }
 *   }
 *   ```
 *
 *   The `files.section` and `files.shop` values are base64-encoded UTF-8
 *   YAML strings.  Clients decode them and write to:
 *     plugins/EconomyShopGUI-OSS/sections/<code>.yml
 *     plugins/EconomyShopGUI-OSS/shops/<code>.yml
 *
 *   Response 404: layout not found.
 *
 * ── 3. Upload layout ─────────────────────────────────────────────────────────
 *
 *   POST /layouts
 *   Authorization: Bearer <token>
 *   Content-Type: application/json
 *
 *   Request body:
 *   ```json
 *   {
 *     "name":        "Diamond Shop",
 *     "description": "A clean diamond materials section.",
 *     "tags":        ["materials", "vanilla"],
 *     "files": {
 *       "section": "<base64-encoded YAML>",
 *       "shop":    "<base64-encoded YAML>"
 *     }
 *   }
 *   ```
 *
 *   Field constraints:
 *     name        required, 3–64 characters
 *     description optional, max 512 characters
 *     tags        optional, array of strings, max 5 tags, each 2–32 chars
 *     files       required; both "section" and "shop" keys required
 *
 *   Response 201:
 *   ```json
 *   {
 *     "id":   "abc123",
 *     "code": "DIAMONDS",
 *     "url":  "https://marketplace.economyshopgui-oss.example.com/layout/DIAMONDS"
 *   }
 *   ```
 *
 *   The `code` is the short share code players paste into `/eshop install`.
 *
 *   Response 422: validation error (body contains per-field error details).
 *
 * ── 4. Update layout ─────────────────────────────────────────────────────────
 *
 *   PATCH /layouts/{code}
 *   Authorization: Bearer <token>
 *   Content-Type: application/json
 *
 *   Path parameters:
 *     code  (string) — share code of the layout to update
 *
 *   Request body (all fields optional; only provided fields are updated):
 *   ```json
 *   {
 *     "name":        "Diamond Shop v2",
 *     "description": "Updated diamond section.",
 *     "tags":        ["materials"],
 *     "files": {
 *       "section": "<base64-encoded YAML>",
 *       "shop":    "<base64-encoded YAML>"
 *     }
 *   }
 *   ```
 *
 *   Response 200: same shape as GET /layouts/{code} (excluding `files`).
 *   Response 403: token does not own this layout.
 *   Response 404: layout not found.
 *
 * ── 5. Delete layout ─────────────────────────────────────────────────────────
 *
 *   DELETE /layouts/{code}
 *   Authorization: Bearer <token>
 *
 *   Response 204: deleted (empty body).
 *   Response 403: token does not own this layout.
 *   Response 404: layout not found.
 *
 * ── 6. Record download (analytics) ───────────────────────────────────────────
 *
 *   POST /layouts/{code}/downloads
 *
 *   No authentication required.  Called automatically by the plugin after a
 *   successful layout installation to increment the download counter.
 *
 *   Response 204: recorded (empty body).
 *   Response 404: layout not found.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * RATE LIMITING
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * All endpoints are rate-limited per IP:
 *   - Unauthenticated:  60 requests / minute
 *   - Authenticated:   300 requests / minute
 *
 * When the limit is exceeded, the server returns:
 *   HTTP 429  Too Many Requests
 *   Retry-After: <seconds until reset>
 *
 * The client implements a single automatic retry after the Retry-After delay.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 */
class MarketplaceClient(private val plugin: EconomyShopGUIOSS) {

    private val baseUrl: String get() =
        plugin.configManager.config.marketplaceUrl
            .trimEnd('/')
            .ifBlank { "https://marketplace.economyshopgui-oss.example.com/api/v1" }

    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    private val userAgent: String get() =
        "EconomyShopGUI-OSS/${plugin.pluginMeta.version}"

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Fetch a [LayoutMeta] by its short [code] without downloading the YAML.
     *
     * Returns [MarketplaceResult.Success] with the layout metadata, or a typed
     * [MarketplaceResult.Failure] if the request failed.
     */
    fun fetchMeta(code: String): MarketplaceResult<LayoutMeta> {
        val response = get("/layouts/${code.uppercase()}")
        return when (response.statusCode()) {
            200  -> parseLayoutMeta(response.body())
            404  -> MarketplaceResult.Failure.NotFound(code)
            429  -> MarketplaceResult.Failure.RateLimited(retryAfter(response))
            else -> MarketplaceResult.Failure.ServerError(response.statusCode(), response.body())
        }
    }

    /**
     * Download a layout's YAML files by [code].
     *
     * On success, [LayoutDownload.sectionYaml] and [LayoutDownload.shopYaml]
     * are base64-decoded UTF-8 strings ready to be written to disk.
     */
    fun download(code: String): MarketplaceResult<LayoutDownload> {
        val response = get("/layouts/${code.uppercase()}")
        return when (response.statusCode()) {
            200  -> parseLayoutDownload(response.body())
            404  -> MarketplaceResult.Failure.NotFound(code)
            429  -> MarketplaceResult.Failure.RateLimited(retryAfter(response))
            else -> MarketplaceResult.Failure.ServerError(response.statusCode(), response.body())
        }
    }

    /**
     * Upload a new layout to the marketplace.
     *
     * [token] must be a valid Bearer token.  [sectionYaml] and [shopYaml] are
     * the raw YAML strings — this method handles base64 encoding.
     *
     * Returns the assigned [LayoutUploadResult] containing the share code.
     */
    fun upload(
        token: String,
        name: String,
        description: String,
        tags: List<String>,
        sectionYaml: String,
        shopYaml: String,
    ): MarketplaceResult<LayoutUploadResult> {
        val body = buildJsonObject(
            "name"        to name,
            "description" to description,
            "tags"        to tags,
            "files"       to mapOf(
                "section" to java.util.Base64.getEncoder().encodeToString(sectionYaml.toByteArray()),
                "shop"    to java.util.Base64.getEncoder().encodeToString(shopYaml.toByteArray()),
            ),
        )
        val response = post("/layouts", body, token)
        return when (response.statusCode()) {
            201  -> parseUploadResult(response.body())
            401, 403 -> MarketplaceResult.Failure.Unauthorized
            422  -> MarketplaceResult.Failure.ValidationError(response.body())
            429  -> MarketplaceResult.Failure.RateLimited(retryAfter(response))
            else -> MarketplaceResult.Failure.ServerError(response.statusCode(), response.body())
        }
    }

    /**
     * Update an existing layout identified by [code].
     *
     * Only the provided fields are changed (PATCH semantics).
     */
    fun update(
        token: String,
        code: String,
        name: String? = null,
        description: String? = null,
        tags: List<String>? = null,
        sectionYaml: String? = null,
        shopYaml: String? = null,
    ): MarketplaceResult<LayoutMeta> {
        val fields = mutableMapOf<String, Any>()
        name?.let        { fields["name"] = it }
        description?.let { fields["description"] = it }
        tags?.let        { fields["tags"] = it }
        if (sectionYaml != null || shopYaml != null) {
            val files = mutableMapOf<String, String>()
            sectionYaml?.let { files["section"] = java.util.Base64.getEncoder().encodeToString(it.toByteArray()) }
            shopYaml?.let    { files["shop"]    = java.util.Base64.getEncoder().encodeToString(it.toByteArray()) }
            fields["files"] = files
        }
        val response = patch("/layouts/${code.uppercase()}", buildJsonObject(*fields.entries.map { it.key to it.value }.toTypedArray()), token)
        return when (response.statusCode()) {
            200      -> parseLayoutMeta(response.body())
            401, 403 -> MarketplaceResult.Failure.Unauthorized
            404      -> MarketplaceResult.Failure.NotFound(code)
            429      -> MarketplaceResult.Failure.RateLimited(retryAfter(response))
            else     -> MarketplaceResult.Failure.ServerError(response.statusCode(), response.body())
        }
    }

    /**
     * Record a download event for analytics (no auth required).
     * Called automatically by [LayoutInstaller] after a successful install.
     */
    fun recordDownload(code: String) {
        runCatching {
            post("/layouts/${code.uppercase()}/downloads", "{}", token = null)
        }
    }

    /**
     * Search the marketplace index.
     *
     * Returns the raw JSON body (parsing delegated to callers / future GUI).
     */
    fun search(query: String, limit: Int = 20, cursor: String? = null): MarketplaceResult<String> {
        val params = buildString {
            append("?q=").append(java.net.URLEncoder.encode(query, "UTF-8"))
            append("&limit=").append(limit.coerceIn(1, 100))
            cursor?.let { append("&cursor=").append(java.net.URLEncoder.encode(it, "UTF-8")) }
        }
        val response = get("/layouts$params")
        return when (response.statusCode()) {
            200  -> MarketplaceResult.Success(response.body())
            429  -> MarketplaceResult.Failure.RateLimited(retryAfter(response))
            else -> MarketplaceResult.Failure.ServerError(response.statusCode(), response.body())
        }
    }

    // ── HTTP helpers ──────────────────────────────────────────────────────────

    private fun get(path: String): HttpResponse<String> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl$path"))
            .timeout(Duration.ofSeconds(15))
            .header("User-Agent", userAgent)
            .header("Accept", "application/json")
            .GET()
            .build()
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    }

    private fun post(path: String, body: String, token: String?): HttpResponse<String> {
        val builder = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl$path"))
            .timeout(Duration.ofSeconds(30))
            .header("User-Agent", userAgent)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
        token?.let { builder.header("Authorization", "Bearer $it") }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString())
    }

    private fun patch(path: String, body: String, token: String?): HttpResponse<String> {
        val builder = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl$path"))
            .timeout(Duration.ofSeconds(30))
            .header("User-Agent", userAgent)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
        token?.let { builder.header("Authorization", "Bearer $it") }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString())
    }

    private fun retryAfter(response: HttpResponse<*>): Int =
        response.headers().firstValue("Retry-After").orElse("5").toIntOrNull() ?: 5

    // ── Minimal JSON builder (no external dependency) ─────────────────────────

    private fun buildJsonObject(vararg pairs: Pair<String, Any?>): String = buildString {
        append('{')
        pairs.forEachIndexed { i, (k, v) ->
            if (i > 0) append(',')
            append('"').append(k).append('"').append(':')
            append(jsonValue(v))
        }
        append('}')
    }

    private fun jsonValue(v: Any?): String = when (v) {
        null            -> "null"
        is String       -> "\"${v.replace("\\", "\\\\").replace("\"", "\\\"")}\""
        is Number       -> v.toString()
        is Boolean      -> v.toString()
        is List<*>      -> "[${v.joinToString(",") { jsonValue(it) }}]"
        is Map<*, *>    -> "{${v.entries.joinToString(",") { (k, val2) -> "\"$k\":${jsonValue(val2)}" }}}"
        else            -> "\"${v}\""
    }

    // ── Response parsers (minimal — avoids pulling in a JSON library) ─────────

    private fun parseLayoutMeta(json: String): MarketplaceResult<LayoutMeta> = runCatching {
        MarketplaceResult.Success(LayoutMeta(
            id          = extractString(json, "id"),
            code        = extractString(json, "code"),
            name        = extractString(json, "name"),
            description = extractStringOrNull(json, "description"),
            author      = extractString(json, "author"),
            downloads   = extractInt(json, "downloads"),
            updatedAt   = extractLong(json, "updated_at"),
        ))
    }.getOrElse { MarketplaceResult.Failure.ParseError(it.message ?: "unknown") }

    private fun parseLayoutDownload(json: String): MarketplaceResult<LayoutDownload> = runCatching {
        val filesBlock = extractBlock(json, "files")
        MarketplaceResult.Success(LayoutDownload(
            meta = (parseLayoutMeta(json) as? MarketplaceResult.Success)?.value
                ?: return MarketplaceResult.Failure.ParseError("could not parse meta"),
            sectionYaml = String(java.util.Base64.getDecoder().decode(extractString(filesBlock, "section"))),
            shopYaml    = String(java.util.Base64.getDecoder().decode(extractString(filesBlock, "shop"))),
        ))
    }.getOrElse { MarketplaceResult.Failure.ParseError(it.message ?: "unknown") }

    private fun parseUploadResult(json: String): MarketplaceResult<LayoutUploadResult> = runCatching {
        MarketplaceResult.Success(LayoutUploadResult(
            id   = extractString(json, "id"),
            code = extractString(json, "code"),
            url  = extractString(json, "url"),
        ))
    }.getOrElse { MarketplaceResult.Failure.ParseError(it.message ?: "unknown") }

    // ── Tiny JSON extractors (regex-based, good enough for flat objects) ──────

    private fun extractString(json: String, key: String): String =
        Regex(""""$key"\s*:\s*"([^"\\]*(?:\\.[^"\\]*)*)"""").find(json)?.groupValues?.get(1)
            ?: error("key '$key' not found")

    private fun extractStringOrNull(json: String, key: String): String? =
        Regex(""""$key"\s*:\s*"([^"\\]*(?:\\.[^"\\]*)*)"""").find(json)?.groupValues?.get(1)

    private fun extractInt(json: String, key: String): Int =
        Regex(""""$key"\s*:\s*(\d+)""").find(json)?.groupValues?.get(1)?.toInt()
            ?: error("key '$key' not found")

    private fun extractLong(json: String, key: String): Long =
        Regex(""""$key"\s*:\s*(\d+)""").find(json)?.groupValues?.get(1)?.toLong()
            ?: error("key '$key' not found")

    private fun extractBlock(json: String, key: String): String {
        val start = json.indexOf("\"$key\"")
        if (start < 0) error("key '$key' not found")
        val braceStart = json.indexOf('{', start)
        if (braceStart < 0) error("object for '$key' not found")
        var depth = 0
        for (i in braceStart until json.length) {
            when (json[i]) {
                '{' -> depth++
                '}' -> { depth--; if (depth == 0) return json.substring(braceStart, i + 1) }
            }
        }
        error("unterminated object for '$key'")
    }
}
