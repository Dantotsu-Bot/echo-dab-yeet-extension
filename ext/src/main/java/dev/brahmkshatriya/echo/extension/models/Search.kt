package dev.brahmkshatriya.echo.extension.models

import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    val tracks: List<Track>? = null,
    val albums: List<Album>? = null,
    val pagination: Pagination? = null
)

@Serializable
data class Pagination(
    val offset: Int,
    val limit: Int? = null,
    val total: Int,
    val hasMore: Boolean,
    val returned: Int? = null
)