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

    override val settingItems: List<Setting> = listOf()

    private lateinit var setting: Settings
    override fun setSettings(settings: Settings) {
        setting = settings
    }
    
    //==== SearchFeedClient ====//

    override suspend fun quickSearch(query: String): List<QuickSearchItem> = listOf()

    override suspend fun deleteQuickSearch(item: QuickSearchItem) = Unit

    override suspend fun searchTabs(query: String): List<Tab> = listOf()

    override fun searchFeed(query: String, tab: Tab?): Feed {
        val dummyShelf = Shelf.Lists.Items(
            title = "Results for \"$query\"",
            list = listOf()
        )

        val paged = PagedData.Single<Shelf> { listOf(dummyShelf) }
        return Feed(pagedData = paged)
    }

}