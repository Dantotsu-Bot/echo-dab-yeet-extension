package dev.brahmkshatriya.echo.extension.network

import dev.brahmkshatriya.echo.extension.models.AlbumResponse
import dev.brahmkshatriya.echo.extension.models.ArtistResponse
import dev.brahmkshatriya.echo.extension.models.Stream
import dev.brahmkshatriya.echo.extension.models.SearchResponse
import okhttp3.OkHttpClient

class ApiService(client: OkHttpClient) : BaseHttpClient(client) {

    override val baseUrl: String = "https://dab.yeet.su/api/"

    suspend fun getAlbum(id: String): AlbumResponse {
        return get("album", mapOf("albumId" to id))
    }

    suspend fun getArtist(id: String): ArtistResponse {
        return get("discography", mapOf("artistId" to id))
    }

    suspend fun search(
        query: String,
        offset: Int = 0,
        type: String,
    ): SearchResponse {
        return get("search", mapOf("q" to query, "offset" to offset, "type" to type))
    }

    suspend fun getStream(trackId: String): Stream {
        return get("stream", mapOf("trackId" to trackId))
    }
}
