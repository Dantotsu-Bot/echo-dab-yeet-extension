package dev.brahmkshatriya.echo.extension

import dev.brahmkshatriya.echo.common.clients.AlbumClient
import dev.brahmkshatriya.echo.common.clients.ArtistClient
import dev.brahmkshatriya.echo.common.clients.ExtensionClient
import dev.brahmkshatriya.echo.common.clients.SearchFeedClient
import dev.brahmkshatriya.echo.common.clients.ShareClient
import dev.brahmkshatriya.echo.common.clients.TrackClient
import dev.brahmkshatriya.echo.common.helpers.ClientException
import dev.brahmkshatriya.echo.common.helpers.Page
import dev.brahmkshatriya.echo.common.helpers.PagedData
import dev.brahmkshatriya.echo.common.models.Album
import dev.brahmkshatriya.echo.common.models.Artist
import dev.brahmkshatriya.echo.common.models.Feed
import dev.brahmkshatriya.echo.common.models.Feed.Companion.toFeed
import dev.brahmkshatriya.echo.common.models.Playlist
import dev.brahmkshatriya.echo.common.models.Radio
import dev.brahmkshatriya.echo.common.models.QuickSearchItem
import dev.brahmkshatriya.echo.common.models.Shelf
import dev.brahmkshatriya.echo.common.models.Tab
import dev.brahmkshatriya.echo.common.models.Track
import dev.brahmkshatriya.echo.common.models.EchoMediaItem
import dev.brahmkshatriya.echo.common.models.Streamable
import dev.brahmkshatriya.echo.common.models.Streamable.Media.Companion.toServerMedia
import dev.brahmkshatriya.echo.common.settings.Setting
import dev.brahmkshatriya.echo.common.settings.Settings
import dev.brahmkshatriya.echo.extension.models.Pagination
import dev.brahmkshatriya.echo.extension.network.ApiService
import okhttp3.OkHttpClient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class DabYeetExtension : ExtensionClient, SearchFeedClient, TrackClient, AlbumClient, ArtistClient, ShareClient {

    private val client by lazy { OkHttpClient.Builder().build() }

    private val api = ApiService(client)

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // ===== Settings ===== //

    override suspend fun onExtensionSelected() {}

    override suspend fun onInitialize() {}
    
    override suspend fun getSettingItems() : List<Setting> = listOf<Setting>()
    
    override fun setSettings(settings: Settings) {}

    //==== SearchFeedClient ====//

    override suspend fun loadSearchFeed(query: String): Feed<Shelf> {
        if (query.isBlank()) return emptyList<Shelf>().toFeed()

        val albumShelf = buildPagedShelf(
            id = "0",
            title = "Albums",
            type = Shelf.Lists.Type.Linear,
            search = { offset -> api.search(query, offset, MediaType.Album.type) },
            extractItems = { it.albums?.map { a -> a.toAlbum() } ?: emptyList() },
            extractPagination = { it.pagination }
        )

        val trackShelf = buildPagedShelf(
            id = "1",
            title = "Tracks",
            type = Shelf.Lists.Type.Grid,
            search = { offset -> api.search(query, offset, MediaType.Track.type) },
            extractItems = { it.tracks?.map { t -> t.toTrack() } ?: emptyList() },
            extractPagination = { it.pagination }
        )

        return listOf(albumShelf, trackShelf).toFeed()
    }

    // ====== TrackClient ======= //

    override suspend fun loadTrack(track: Track, isDownload: Boolean): Track = track

    override suspend fun loadStreamableMedia(streamable: Streamable, isDownload: Boolean): Streamable.Media {
        val stream = api.getStream(streamable.id)
        return stream.url.toServerMedia()
    }

    override suspend fun loadFeed(track: Track): Feed<Shelf>? = null
    
    // ====== AlbumClient ====== //

    override suspend fun loadAlbum(album: Album): Album {
        if (album.isLoaded()) {
            return album
        } else {
            return api.getAlbum(album.id).album.toAlbum()
        }
    }

    override suspend fun loadTracks(album: Album): Feed<Track>? {
        val albumList = api.getAlbum(album.id).album.tracks?.map { it.toTrack() }.orEmpty()
        if (albumList.isNotEmpty()) {
            return albumList.toFeed()
        }
        return null
    }

    override suspend fun loadFeed(album: Album): Feed<Shelf>? = null

    // ====== ArtistClient ===== //
    
    override suspend fun loadArtist(artist: Artist): Artist {
        if (artist.isLoaded()) {
            return artist
        } else {
            return api.getArtist(artist.id).toArtist()
        }
    }

    override suspend fun loadFeed(artist: Artist) : Feed<Shelf> {
        val data = if (artist.isLoaded()) {
            artist
        } else {
            api.getArtist(artist.id).toArtist()
        }
        val albumList = json.decodeFromString<Album>(data.extras["albumList"]!!)
        val similarArtistId = json.decodeFromString(data.extras["similarArtistIds"])
        
        val albums = Shelf.Lists.Items(
            id = "0",
            title = "More from ${artist.name}",
            list = albumList,
            type = Shelf.Lists.Type.Linear
        )
        return albums.toFeed()
    }

    // ====== ShareClient ===== //

    override suspend fun onShare(item: EchoMediaItem): String {
        return when(item) {
            is Track -> "https://www.qobuz.com/us-en/album/${item.extras["albumId"]}"
            is Album -> "https://www.qobuz.com/us-en/album/${item.id}"
            is Artist -> {
                val id = item.id
                val slug = item.extras["slug"]
                "https://www.qobuz.com/us-en/interpreter/$slug/$id"
            }
            is Playlist -> throw ClientException.NotSupported("TODO: Playlist sharing")
            is Radio -> throw ClientException.NotSupported("Will not be implemented")
        }
    }

    // ===== Utils ===== //

    private fun Any.isLoaded(): Boolean {
        return when (this) {
            is Track -> extras["isLoaded"] == "true"
            is Album -> extras["isLoaded"] == "true"
            is Artist -> extras["isLoaded"] == "true"
            else -> throw TypeCastException("Type mismatch: expected Echo Model but found ${this::class.simpleName}")
        }
    }

    private suspend fun <R> buildPagedShelf(
        id: String,
        title: String,
        type: Shelf.Lists.Type,
        search: suspend (offset: Int) -> R,
        extractItems: (R) -> List<EchoMediaItem>,
        extractPagination: (R) -> Pagination
    ): Shelf.Lists.Items {

        val firstResponse = search(0)
        val firstItems = extractItems(firstResponse)

        val paged = PagedData.Continuous<Shelf> { paginationString ->
            val offset = if (paginationString == null) {
                0
            } else {
                val current = json.decodeFromString<Pagination>(paginationString)
                current.offset + current.limit
            }

            val response = if (offset == 0) firstResponse else search(offset)
            val items = extractItems(response).map { it.toShelf() }
            val pagination = extractPagination(response)
            val next = if (pagination.hasMore) json.encodeToString(pagination) else null

            Page(items, next)
        }

        return Shelf.Lists.Items(
            id = id,
            title = title,
            list = firstItems,
            type = type,
            more = paged.toFeed()
        )
    }

    enum class MediaType(val type: String) {
        Track("track"),
        Album("album"),
        Artist("artist"),
    }

}