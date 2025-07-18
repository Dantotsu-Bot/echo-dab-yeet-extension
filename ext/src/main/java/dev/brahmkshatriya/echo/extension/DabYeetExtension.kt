package dev.brahmkshatriya.echo.extension

import dev.brahmkshatriya.echo.common.clients.ExtensionClient
import dev.brahmkshatriya.echo.common.clients.SearchFeedClient
import dev.brahmkshatriya.echo.common.settings.Setting
import dev.brahmkshatriya.echo.common.settings.Settings
import dev.brahmkshatriya.echo.common.helpers.PagedData
import dev.brahmkshatriya.echo.common.models.Feed
import dev.brahmkshatriya.echo.common.models.QuickSearchItem
import dev.brahmkshatriya.echo.common.models.Shelf
import dev.brahmkshatriya.echo.common.models.Tab

class DabYeetExtension : ExtensionClient, SearchFeedClient {

    private val baseUrl = "https://dab.yeet.su/api/"

    override suspend fun onExtensionSelected() {}

    override val settingItems: List<Setting> = emptyList()

    private lateinit var setting: Settings
    override fun setSettings(settings: Settings) {
        setting = settings
    }
    
    //==== SearchFeedClient ====//

    override suspend fun quickSearch(query: String): List<QuickSearchItem> {
        TODO("Implement")
    }

    override suspend fun deleteQuickSearch(item: QuickSearchItem) {
        TODO("Implement")
    }

    override suspend fun searchTabs(query: String): List<Tab> {
        TODO("Implement")
    }

    override fun searchFeed(query: String, tab: Tab?): Feed {
        TODO("Implement")
    }
}