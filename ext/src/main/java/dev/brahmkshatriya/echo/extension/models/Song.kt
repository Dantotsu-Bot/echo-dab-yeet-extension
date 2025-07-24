package dev.brahmkshatriya.echo.extension.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val artistId: String,
    val albumTitle: String,
    val albumCover: String,
    val albumId: String,
    val releaseDate: String,
    val genre: String,
    val duration: Int,
    val audioQuality: AudioQuality,
    val version: String? = null,
    val label: String? = null,
    @SerialName("parental_warning")
    val parentalWarning: Boolean? = null,
    val images: Images? = null
)
