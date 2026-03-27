Marketplace API
===============

The EconomyShopGUI-OSS plugin can connect to a layout marketplace where
server administrators upload and download pre-built shop sections (called
**layouts**).

This document describes the full HTTP API contract so that anyone can
implement a compatible marketplace server.

.. contents::
   :local:
   :depth: 2

----

Overview
--------

Base URL
~~~~~~~~

Configured in ``config.yml``:

.. code-block:: yaml

   marketplace:
     url: https://api.gpplugins.com:2096/val
     token: ""   # bearer token for upload/update — obtain from the marketplace website

All paths in this document are relative to the base URL.

.. note::
   The default URL above points to the gpplugins reference marketplace used by the
   original EconomyShopGUI plugin.  Its wire protocol differs from the REST API
   described here: it uses ``/createLayout``, ``/getLayout``, and ``/createUpdate``
   endpoints with ZIP file uploads and a session-UUID token flow rather than
   JSON + base64 payloads and Bearer tokens.  If you are implementing a clean-room
   compatible marketplace server, follow the REST contract in this document and
   configure a custom ``marketplace.url`` in ``config.yml``.

Transport
~~~~~~~~~

- HTTPS only (HTTP connections should be rejected by the server)
- All request and response bodies are ``application/json`` (UTF-8)
- All timestamps are **Unix epoch seconds** (64-bit integers)

Common headers (sent on every request)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. code-block:: text

   User-Agent:    EconomyShopGUI-OSS/<plugin-version>
   Accept:        application/json

----

Authentication
--------------

Endpoints that modify data (upload, update, delete) require a Bearer token:

.. code-block:: text

   Authorization: Bearer <token>

Tokens are issued per server through the marketplace website.
There is no in-plugin registration flow.

Unauthenticated requests to protected endpoints return **HTTP 401**.
Valid tokens without ownership of the target resource return **HTTP 403**.

----

Error responses
---------------

All errors share this shape:

.. code-block:: json

   { "error": "<machine-readable code>", "message": "<human-readable detail>" }

.. list-table::
   :header-rows: 1
   :widths: 20 15 65

   * - Code
     - HTTP status
     - Meaning
   * - ``not_found``
     - 404
     - The requested layout code does not exist.
   * - ``unauthorized``
     - 401
     - Missing or invalid Bearer token.
   * - ``forbidden``
     - 403
     - Token valid but the caller does not own the resource.
   * - ``validation_error``
     - 422
     - Request body failed validation.  The ``message`` field contains
       per-field details.
   * - ``rate_limited``
     - 429
     - Too many requests.  The ``Retry-After`` header contains the number
       of seconds to wait.
   * - ``server_error``
     - 500
     - Unexpected server-side failure.

----

Rate limiting
-------------

.. list-table::
   :header-rows: 1
   :widths: 35 65

   * - Client type
     - Limit
   * - Unauthenticated
     - 60 requests / minute per IP
   * - Authenticated (Bearer token present)
     - 300 requests / minute per token

When the limit is exceeded:

.. code-block:: text

   HTTP/1.1 429 Too Many Requests
   Retry-After: 12
   Content-Type: application/json

   { "error": "rate_limited", "message": "Try again in 12 seconds." }

The plugin automatically retries once after the ``Retry-After`` delay.

----

Pagination
----------

List endpoints use **cursor-based pagination**.

Request the next page by passing the ``next_cursor`` value from the
previous response as the ``cursor`` query parameter.  If ``next_cursor``
is absent from a response, there are no more results.

.. code-block:: text

   GET /layouts?limit=20&cursor=eyJpZCI6ImFiYzEyNCJ9

Cursors are opaque strings — do not parse or construct them.

----

Endpoints
---------

1. List layouts
~~~~~~~~~~~~~~~

.. code-block:: text

   GET /layouts

Query parameters:

.. list-table::
   :header-rows: 1
   :widths: 20 15 65

   * - Parameter
     - Type
     - Description
   * - ``q``
     - string
     - Full-text search across name, description, and tags.
   * - ``tag``
     - string
     - Filter by a single tag (exact match, case-insensitive).
   * - ``sort``
     - string
     - One of: ``recent`` (default), ``popular``, ``name``.
   * - ``cursor``
     - string
     - Opaque pagination cursor from a previous response.
   * - ``limit``
     - integer
     - Results per page.  Default: 20.  Maximum: 100.

Response ``200 OK``:

.. code-block:: json

   {
     "layouts": [
       {
         "id":          "abc123",
         "code":        "DIAMONDS",
         "name":        "Diamond Shop",
         "description": "A clean diamond materials section.",
         "author":      "Notch",
         "tags":        ["materials", "vanilla"],
         "downloads":   1024,
         "created_at":  1700000000,
         "updated_at":  1700001000
       }
     ],
     "next_cursor": "eyJpZCI6ImFiYzEyNCJ9",
     "total": 42
   }

``next_cursor`` is absent when there are no further results.

----

2. Get layout by code
~~~~~~~~~~~~~~~~~~~~~

.. code-block:: text

   GET /layouts/{code}

Path parameter ``code`` — the short share code (case-insensitive, e.g.
``DIAMONDS``).

Response ``200 OK``:

.. code-block:: json

   {
     "id":          "abc123",
     "code":        "DIAMONDS",
     "name":        "Diamond Shop",
     "description": "A clean diamond materials section.",
     "author":      "Notch",
     "tags":        ["materials", "vanilla"],
     "downloads":   1024,
     "created_at":  1700000000,
     "updated_at":  1700001000,
     "files": {
       "section": "<base64-encoded UTF-8 YAML>",
       "shop":    "<base64-encoded UTF-8 YAML>"
     }
   }

The ``files.section`` and ``files.shop`` values are standard Base64-encoded
UTF-8 strings.  The plugin decodes them and writes:

- ``plugins/EconomyShopGUI-OSS/sections/<CODE>.yml`` ← ``files.section``
- ``plugins/EconomyShopGUI-OSS/shops/<CODE>.yml`` ← ``files.shop``

Response ``404``: layout not found.

----

3. Upload layout
~~~~~~~~~~~~~~~~

.. code-block:: text

   POST /layouts
   Authorization: Bearer <token>

Request body:

.. code-block:: json

   {
     "name":        "Diamond Shop",
     "description": "A clean diamond materials section.",
     "tags":        ["materials", "vanilla"],
     "files": {
       "section": "<base64-encoded UTF-8 YAML>",
       "shop":    "<base64-encoded UTF-8 YAML>"
     }
   }

Field constraints:

.. list-table::
   :header-rows: 1
   :widths: 20 80

   * - Field
     - Rules
   * - ``name``
     - Required.  3–64 characters.
   * - ``description``
     - Optional.  Maximum 512 characters.
   * - ``tags``
     - Optional.  Array of strings.  Maximum 5 tags, each 2–32 characters.
       Only ``[a-z0-9-]`` characters allowed.
   * - ``files``
     - Required.  Both ``section`` and ``shop`` keys must be present.
       The ``shop`` value may be an empty string if the section has no items.
   * - ``files.section``
     - Required.  Base64-encoded YAML.  Maximum decoded size: 512 KB.
   * - ``files.shop``
     - Required.  Base64-encoded YAML.  Maximum decoded size: 2 MB.

Response ``201 Created``:

.. code-block:: json

   {
     "id":   "abc123",
     "code": "DIAMONDS",
     "url":  "https://api.gpplugins.com:2096/val/layout/DIAMONDS"
   }

The ``code`` is the short share code administrators paste into
``/eshop install DIAMONDS``.  Codes are assigned by the server; suggest
using the uppercase sanitised ``name`` value if it is unique, otherwise a
random alphanumeric string.

Response ``422``: validation failed.  The ``message`` field contains
per-field details.

----

4. Update layout
~~~~~~~~~~~~~~~~

.. code-block:: text

   PATCH /layouts/{code}
   Authorization: Bearer <token>

All request body fields are **optional**.  Only provided fields are updated
(PATCH semantics).

Request body:

.. code-block:: json

   {
     "name":        "Diamond Shop v2",
     "description": "Updated description.",
     "tags":        ["materials"],
     "files": {
       "section": "<base64>",
       "shop":    "<base64>"
     }
   }

Partial ``files`` objects (e.g. only ``section`` without ``shop``) update
only the provided file.

Response ``200 OK``: same shape as ``GET /layouts/{code}`` (without
``files``).

Response ``403``: the token does not own this layout.
Response ``404``: layout not found.

----

5. Delete layout
~~~~~~~~~~~~~~~~

.. code-block:: text

   DELETE /layouts/{code}
   Authorization: Bearer <token>

Response ``204 No Content``: deleted.

Response ``403``: the token does not own this layout.
Response ``404``: layout not found.

----

6. Record download (analytics)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. code-block:: text

   POST /layouts/{code}/downloads

No authentication required.  The plugin calls this endpoint automatically
after a successful layout installation to increment the public download counter.

Implementations should be idempotent and should not fail if the same server
records multiple downloads for the same code within a short window (e.g.
during reload testing).

Response ``204 No Content``: recorded.
Response ``404``: layout not found (the counter is not incremented).

----

Layout object schema
--------------------

The full layout object returned by **GET /layouts/{code}**:

.. list-table::
   :header-rows: 1
   :widths: 25 15 60

   * - Field
     - Type
     - Description
   * - ``id``
     - string
     - Internal unique identifier (UUID or database PK).
   * - ``code``
     - string
     - Short, uppercase share code (e.g. ``DIAMONDS``).
   * - ``name``
     - string
     - Human-readable listing name.
   * - ``description``
     - string or null
     - Optional description.
   * - ``author``
     - string
     - Username of the uploader.
   * - ``tags``
     - array of strings
     - Searchable tags.
   * - ``downloads``
     - integer
     - Total download count (cumulative across all versions).
   * - ``created_at``
     - integer
     - Unix epoch seconds when the layout was first uploaded.
   * - ``updated_at``
     - integer
     - Unix epoch seconds of the last update.
   * - ``files``
     - object
     - Only present on the single-layout endpoint.  Contains ``section``
       and ``shop`` as base64-encoded YAML strings.

----

Implementing a compatible server
---------------------------------

The plugin communicates with the marketplace entirely over HTTPS with
standard JSON bodies and no proprietary binary formats.  A minimal compliant
server needs:

1. A database with a ``layouts`` table (columns: id, code, name, description,
   author_token, tags, downloads, created_at, updated_at, section_yaml,
   shop_yaml).
2. Endpoints 1–6 as described above.
3. Rate limiting per IP (unauthenticated) and per token (authenticated).
4. Standard Base64 encoding/decoding for YAML file content.

No special integration with the Minecraft server software is required — the
marketplace is a standalone HTTP service.

Reference implementations and OpenAPI specs can be contributed to the
`EconomyShopGUI-OSS GitHub repository`_.

.. _EconomyShopGUI-OSS GitHub repository: https://github.com/Earth1283/EconomyShopGUI-OSS
