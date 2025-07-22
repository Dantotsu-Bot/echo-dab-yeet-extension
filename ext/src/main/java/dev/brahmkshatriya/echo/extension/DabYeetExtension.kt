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

        // 1. A shelf for tracks, displayed in a numbered list.
        val dummyTracksShelf = Shelf.Lists.Tracks(
            title = "Songs for \"$query\"",
            subtitle = "Top Results",
            list = listOf(), // Empty because we can't create dummy `Track` objects here.
            isNumbered = true
        )

        // 2. A shelf for items like albums or playlists, displayed in a grid.
        val dummyItemsShelf = Shelf.Lists.Items(
            title = "Albums for \"$query\"",
            list = listOf(), // Empty because we can't create dummy `EchoMediaItem` objects.
            type = Shelf.Lists.Type.Grid
        )
        
        // 3. A shelf for categories (e.g., genres).
        // First, create the individual category items.
        val category1 = Shelf.Category(title = "Rock", items = null, subtitle = "Genre")
        val category2 = Shelf.Category(title = "Pop", items = null, subtitle = "Genre")
        
        // Then, create the shelf that holds these categories.
        val dummyCategoriesShelf = Shelf.Lists.Categories(
            title = "Genres",
            list = listOf(category1, category2), // We can populate this list.
            type = Shelf.Lists.Type.Linear
        )

        // 4. A single header-style category shelf.
        val headerShelf = Shelf.Category(
            title = "More results",
            items = null // `items = null` makes it act as a header.
        )
        
        // Combine all the created shelves into a list.
        val allShelves = listOf(
            dummyTracksShelf,
            dummyItemsShelf,
            dummyCategoriesShelf,
            headerShelf
        )

        // Wrap the list of shelves in PagedData to create the Feed.
        val paged = PagedData.Single<Shelf> { allShelves }
        return Feed(pagedData = paged)
    }

}
