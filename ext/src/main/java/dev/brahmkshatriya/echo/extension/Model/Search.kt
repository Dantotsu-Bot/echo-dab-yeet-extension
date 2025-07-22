package dev.brahmkshatriya.echo.extension.model

import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    val tracks: List<Track>? = null,
    val albums: List<AlbumSummary>? = null,
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