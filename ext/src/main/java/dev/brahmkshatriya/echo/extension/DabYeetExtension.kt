package dev.brahmkshatriya.echo.extension


import dev.brahmkshatriya.echo.common.clients.ExtensionClient
import dev.brahmkshatriya.echo.common.clients.SearchFeedClient
import dev.brahmkshatriya.echo.common.helpers.PagedData
import dev.brahmkshatriya.echo.common.models.Feed
import dev.brahmkshatriya.echo.common.models.QuickSearchItem
import dev.brahmkshatriya.echo.common.models.Shelf
import dev.brahmkshatriya.echo.common.models.Tab
import dev.brahmkshatriya.echo.common.models.EchoMediaItem.Companion.toMediaItem
import dev.brahmkshatriya.echo.common.models.EchoMediaItem
import dev.brahmkshatriya.echo.common.settings.Setting
import dev.brahmkshatriya.echo.common.settings.Settings
import dev.brahmkshatriya.echo.extension.network.RetrofitClient

class DabYeetExtension : ExtensionClient, SearchFeedClient {

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

        val dummyItemsShelf = Shelf.Lists.Items(
            title = "Albums for \"$query\"",
            list = getAlbums(),
            type = Shelf.Lists.Type.Grid
        )

        val dummyTracksShelf = Shelf.Lists.Tracks(
            title = "Songs for \"$query\"",
            subtitle = "Top Results",
            list = listOf(),
            isNumbered = true,
            type = Shelf.Lists.Type.Linear
        )

        val allShelves = listOf(
            dummyItemsShelf,
            dummyTracksShelf
        )

        val paged = PagedData.Single<Shelf> { allShelves }
        return Feed(pagedData = paged)
    }

    // ====== API functions ======= //

    private val api = RetrofitClient.api

    private fun getAlbums(query: String, limit: Pair<Int, Int> = 0 to 0): List<EchoMediaItem.Lists.Album> {
        val (trackLimit, albumLimit) = limit

        val albumResponse =  api.search(query, albumLimit, MediaType.Album.type)

        return albumResponse.albums.map { it.toAlbum().toMediaItem() }
    }


    enum class MediaType(val type: String) {
        Track("track"),
        Album("album"),
        Artist("artist");
    }

}
