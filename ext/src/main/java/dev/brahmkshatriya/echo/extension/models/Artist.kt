package dev.brahmkshatriya.echo.extension.models

import kotlinx.serialization.Serializable

@Serializable
data class ArtistResponse(
    val artist: Artist,
    val albums: List<Album>
)

@Serializable
data class Artist(
    val id: String,
    val name: String,
    val albumsCount: Int,
    val albumsAsPrimaryArtistCount: Int,
    val albumsAsPrimaryComposerCount: Int,
    val slug: String,
    val image: Images,
    val biography: Biography? = null,
    val similarArtistIds: List<String>,
    val information: String? = null
)

@Serializable
data class Biography(
    val summary: String? = null,
    val content: String? = null,
    val source: String? = null,
    val language: String? = null
)