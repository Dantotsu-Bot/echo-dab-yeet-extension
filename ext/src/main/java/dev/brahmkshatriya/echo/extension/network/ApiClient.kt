package dev.brahmkshatriya.echo.extension.network

import dev.brahmkshatriya.echo.extension.models.AlbumResponse
import dev.brahmkshatriya.echo.extension.models.ArtistResponse
import dev.brahmkshatriya.echo.extension.models.Stream
import dev.brahmkshatriya.echo.extension.models.SearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("album")
    suspend fun getAlbum(
        @Query("albumId") id: String
    ): AlbumResponse

    @GET("discography")
    suspend fun getArtist(
        @Query("artistId") id: String
    ): ArtistResponse

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("offset") offset: Int = 0,
        @Query("type") type: String,
    ): SearchResponse

    @GET("stream")
    suspend fun getStream(
        @Query("trackId") trackId: String,
    ): Stream
}