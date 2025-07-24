package dev.brahmkshatriya.echo.extension.models

import dev.brahmkshatriya.echo.common.models.Album as EchoAlbum
import dev.brahmkshatriya.echo.common.models.Artist
import dev.brahmkshatriya.echo.common.models.ImageHolder.Companion.toImageHolder
import dev.brahmkshatriya.echo.common.models.ImageHolder
import dev.brahmkshatriya.echo.common.models.Date
import dev.brahmkshatriya.echo.extension.utils.IntToString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Album(
    @Serializable(with = IntToString::class)
    val id: String,
    val title: String,
    val artist: String,
    @Serializable(with = IntToString::class)
    val artistId: String? = null,
    val releaseDate: String,
    val genre: String,
    val cover: String,
    val images: Images? = null,
    val trackCount: Int,
    val duration: Int? = null,
    val audioQuality: AudioQuality,
    val label: Label? = null,
    val tracks: List<Song>? = null,
    @SerialName("parental_warning")
    val parentalWarning: Boolean = false
) {
    fun toAlbum(): EchoAlbum {
        return EchoAlbum(
            id = id,
            title = title,
            cover = images?.high?.toImageHolder() ?: cover.toImageHolder(),
            artists = listOf(Artist(id = artistId ?: artist, name = artist)),
            tracks = trackCount,
            duration = duration?.toLong(),
            releaseDate = parseDate(releaseDate),
            label = label?.name,
            isExplicit = parentalWarning
        )
    }

    private fun parseDate(input: String): Date? {
        return try {
            val parts = input.split("-")
            Date(
                year = parts[0].toInt(),
                month = parts.getOrNull(1)?.toIntOrNull() ?: 1,
                day = parts.getOrNull(2)?.toIntOrNull() ?: 1
            )
        } catch (e: Exception) {
            null
        }
    }
}


@Serializable
data class AlbumResponse(
    val album: Album
)

@Serializable
data class Label(
    val name: String,
    val id: Long
)