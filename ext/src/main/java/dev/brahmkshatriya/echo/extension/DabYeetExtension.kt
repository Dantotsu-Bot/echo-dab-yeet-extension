package dev.brahmkshatriya.echo.extension

import dev.brahmkshatriya.echo.common.clients.ExtensionClient
import dev.brahmkshatriya.echo.common.clients.SearchFeedClient
import dev.brahmkshatriya.echo.common.helpers.PagedData
import dev.brahmkshatriya.echo.common.models.Feed
import dev.brahmkshatriya.echo.common.models.QuickSearchItem
import dev.brahmkshatriya.echo.common.models.Shelf
import dev.brahmkshatriya.echo.common.models.Tab
import dev.brahmkshatriya.echo.common.settings.Setting
import dev.brahmkshatriya.echo.common.settings.Settings

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
            list = listOf(),
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

}
