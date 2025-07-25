package dev.brahmkshatriya.echo.extension

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import dev.brahmkshatriya.echo.common.clients.ExtensionClient
import dev.brahmkshatriya.echo.common.clients.SearchFeedClient
import dev.brahmkshatriya.echo.common.clients.TrackClient
import dev.brahmkshatriya.echo.common.helpers.PagedData
import dev.brahmkshatriya.echo.common.models.Feed
import dev.brahmkshatriya.echo.common.models.QuickSearchItem
import dev.brahmkshatriya.echo.common.models.Shelf
import dev.brahmkshatriya.echo.common.models.Tab
import dev.brahmkshatriya.echo.common.models.Track
import dev.brahmkshatriya.echo.common.models.EchoMediaItem.Companion.toMediaItem
import dev.brahmkshatriya.echo.common.models.EchoMediaItem
import dev.brahmkshatriya.echo.common.models.Streamable
import dev.brahmkshatriya.echo.common.models.Streamable.Media.Companion.toServerMedia
import dev.brahmkshatriya.echo.common.settings.Setting
import dev.brahmkshatriya.echo.common.settings.Settings
import dev.brahmkshatriya.echo.extension.network.ApiService
import okhttp3.OkHttpClient

class DabYeetExtension : ExtensionClient, SearchFeedClient, TrackClient {

    private val client by lazy { OkHttpClient.Builder().build() }

    private val api = ApiService(client)

    // ===== Settings ===== //

    override suspend fun onExtensionSelected() {}

    override val settingItems: List<Setting> = emptyList()

    private lateinit var setting: Settings
    override fun setSettings(settings: Settings) {
        setting = settings
    }

    //==== SearchFeedClient ====//

    override suspend fun quickSearch(query: String): List<QuickSearchItem> = listOf()

    override suspend fun deleteQuickSearch(item: QuickSearchItem) = Unit

    override suspend fun searchTabs(query: String): List<Tab> = listOf()

    override fun searchFeed(query: String, tab: Tab?): Feed {
        if (query.isBlank()) {
            return Feed(pagedData = PagedData.empty<Shelf>() )
        }

        val paged = PagedData.Single<Shelf> {
            
            val (albums, tracks) = defaultSearch(query)

            val albumShelf = Shelf.Lists.Items(
                title = "Albums found ${albums.size}",
                list = albums,
                type = Shelf.Lists.Type.Linear
            )

            val trackShelf = Shelf.Lists.Tracks(
                title = "Songs for \"$query\"",
                subtitle = "Top Results",
                list = tracks,
                isNumbered = true,
                type = Shelf.Lists.Type.Grid
            )

            listOf(
                albumShelf,
                trackShelf
            )
        }

        return Feed(pagedData = paged)
    }

    private suspend fun defaultSearch(query: String, limit: Pair<Int, Int> = 0 to 0): Pair<List<EchoMediaItem>, List<Track>> = coroutineScope {
        val (trackLimit, albumLimit) = limit

        val albumsDeferred = async { api.search(query, albumLimit, MediaType.Album.type) }
        val tracksDeferred = async { api.search(query, trackLimit, MediaType.Track.type) }

        val albumResponse = albumsDeferred.await()
        val trackResponse = tracksDeferred.await()

        val albums = albumResponse.albums?.map { it.toAlbum().toMediaItem() } ?: emptyList()
        val tracks = trackResponse.tracks?.map { it.toTrack() } ?: emptyList()

        albums to tracks
    }

    enum class MediaType(val type: String) {
        Track("track"),
        Album("album"),
        Artist("artist"),
    }

    // ====== TrackClient ======= //

    override suspend fun loadTrack(track: Track): Track = track

    override suspend fun loadStreamableMedia(streamable: Streamable, isDownload: Boolean): Streamable.Media {
        val url = api.getStream(streamable.id)
        return url.toServerMedia()
    }

    override fun getShelves(track: Track): PagedData<Shelf> = PagedData.empty<Shelf>()
}