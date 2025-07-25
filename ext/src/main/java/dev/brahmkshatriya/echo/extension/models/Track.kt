package dev.brahmkshatriya.echo.extension.models

import dev.brahmkshatriya.echo.common.models.Album
import dev.brahmkshatriya.echo.common.models.Artist
import dev.brahmkshatriya.echo.common.models.Date
import dev.brahmkshatriya.echo.common.models.ImageHolder
import dev.brahmkshatriya.echo.common.models.ImageHolder.Companion.toImageHolder
import dev.brahmkshatriya.echo.common.models.Streamable
import dev.brahmkshatriya.echo.common.models.Streamable.Companion.server
import dev.brahmkshatriya.echo.common.models.Track as EchoTrack
import dev.brahmkshatriya.echo.extension.utils.IntToString
import dev.brahmkshatriya.echo.extension.utils.parseDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Track(
    @Serializable(with = IntToString::class)
    val id: String,
    val title: String,
    val artist: String,
    @Serializable(with = IntToString::class)
    val artistId: String,
    val albumTitle: String,
    val albumCover: String,
    @Serializable(with = IntToString::class)
    val albumId: String,
    val releaseDate: String,
    val genre: String,
    val duration: Int,
    val audioQuality: AudioQuality,
    val version: String? = null,
    val label: String? = null,
    @SerialName("parental_warning")
    val parentalWarning: Boolean = false,
    val isrc: String? = null,
    val images: Images? = null
) {
    fun toTrack(): EchoTrack {
        return EchoTrack(
            id = id,
            title = title,
            artists = listOf(Artist(id = artistId, name = artist)),
            album = Album(id = albumId, title = albumTitle),
            cover = images?.high?.toImageHolder() ?: albumCover.toImageHolder(),
            duration = duration.toLong(),
            releaseDate = parseDate(releaseDate),
            isExplicit = parentalWarning,
            irsc = isrc,
            genres = genre.split(" ").filter { it.isNotBlank() },
            streamables = listOf(
                Streamable.server(
                    id = id,
                    quality = 0,
                    title = if (audioQuality.isHiRes) "Lossless" else "Lossy",
                )
            ),
        )
    }
}