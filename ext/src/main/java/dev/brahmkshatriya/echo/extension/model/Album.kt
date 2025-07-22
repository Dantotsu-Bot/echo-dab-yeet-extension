package dev.brahmkshatriya.echo.extension.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val artistId: Long? = null,
    val releaseDate: String,
    val genre: String,
    val genreId: Int? = null,
    val genreColor: String? = null,
    val cover: String,
    val images: Images? = null,
    val trackCount: Int,
    val duration: Int? = null,
    val audioQuality: AudioQuality,
    val label: Label? = null,
    val tracks: List<Track>? = null,
    val url: String? = null,
    val mediaCount: Int? = null,
    @SerialName("parental_warning")
    val parentalWarning: Boolean? = null
)

@Serializable
data class Label(
    val name: String,
    val id: Long,
    @SerialName("albums_count")
    val albumsCount: Int,
    @SerialName("supplier_id")
    val supplierId: Int,
    val slug: String
)