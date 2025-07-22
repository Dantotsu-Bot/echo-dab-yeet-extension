package dev.brahmkshatriya.echo.extension.model

import kotlinx.serialization.Serializable

@Serializable
data class ArtistResponse(
    val artist: Artist,
    val albums: List<Album>
)

@Serializable
data class Artist(
    val id: Long,
    val name: String,
    val albumsCount: Int,
    val albumsAsPrimaryArtistCount: Int,
    val albumsAsPrimaryComposerCount: Int,
    val slug: String,
    val image: Images,
    val biography: Biography? = null,
    val similarArtistIds: List<Long>,
    val information: String? = null
)

@Serializable
data class Biography(
    val summary: String? = null,
    val content: String? = null,
    val source: String? = null,
    val language: String? = null
)