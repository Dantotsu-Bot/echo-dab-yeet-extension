package dev.brahmkshatriya.echo.extension.network

import dev.brahmkshatriya.echo.extension.models.AlbumResponse
import dev.brahmkshatriya.echo.extension.models.ArtistResponse
import dev.brahmkshatriya.echo.extension.models.Stream
import dev.brahmkshatriya.echo.extension.models.SearchResponse

class ApiService(private val httpClient: HttpClient) {

    suspend fun getAlbum(id: String): AlbumResponse {
        return httpClient.get(
            endpoint = "album",
            queryParams = mapOf("albumId" to id)
        )
    }

    suspend fun getArtist(id: String): ArtistResponse {
        return httpClient.get(
            endpoint = "discography",
            queryParams = mapOf("artistId" to id)
        )
    }

    suspend fun search(
        query: String,
        offset: Int = 0,
        type: String
    ): SearchResponse {
        return httpClient.get(
            endpoint = "search",
            queryParams = mapOf(
                "q" to query,
                "offset" to offset.toString(),
                "type" to type
            )
        )
    }

    suspend fun getStream(trackId: String): Stream {
        return httpClient.get(
            endpoint = "stream",
            queryParams = mapOf("trackId" to trackId)
        )
    }
}