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
        if (query.isBlank()) {
            return emptyList<Shelf>().toFeed()
        }

        val albumMore = makePagedShelf(
            title = "Albums",
            shelfType = Shelf.Lists.Type.Linear,
            search = { offset -> api.search(query, offset, MediaType.Album.type) },
            extractItems = { response -> response.albums?.map { it.toAlbum() } ?: emptyList() },
            extractPagination = { response -> response.pagination }
        )

        val albumShelf = Shelf.Lists.Items(
            id = "0",
            title = "Albums",
            list = listOf(),
            type = Shelf.Lists.Type.Linear,
            more = albumMore.toFeed()
        )

        val trackMore = makePagedShelf(
            title = "Tracks",
            shelfType = Shelf.Lists.Type.Grid,
            search = { offset -> api.search(query, offset, MediaType.Track.type) },
            extractItems = { response -> response.tracks?.map { it.toTrack() } ?: emptyList() },
            extractPagination = { response -> response.pagination }
        )

        val trackShelf = Shelf.Lists.Items(
            id = "1",
            title = "Tracks",
            list = listOf(),
            type = Shelf.Lists.Type.Grid,
            more = trackMore.toFeed()
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

    override suspend fun loadFeed(artist: Artist) : Feed<Shelf> = listOf<Shelf>().toFeed()

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

    private fun <R> makePagedShelf(
        title: String,
        shelfType: Shelf.Lists.Type,
        search: suspend (offset: Int) -> R,
        extractItems: (R) -> List<EchoMediaItem>,
        extractPagination: (R) -> Pagination
    ): PagedData.Continuous<Shelf> {
        return PagedData.Continuous { pagination ->
            val offset = if (pagination == null) {
                0
            } else {
                val current = json.decodeFromString<Pagination>(pagination)
                current.offset + current.limit
            }

            val response = search(offset)
            val items = extractItems(response)

            val shelf = Shelf.Lists.Items(
                id = "search-$title",
                title = title,
                list = items,
                type = shelfType
            )

            val pag = extractPagination(response)
            val next = if (pag.hasMore == true) json.encodeToString(pag) else null

            Page(listOf(shelf), next)
        }
    }

    enum class MediaType(val type: String) {
        Track("track"),
        Album("album"),
        Artist("artist"),
    }

}