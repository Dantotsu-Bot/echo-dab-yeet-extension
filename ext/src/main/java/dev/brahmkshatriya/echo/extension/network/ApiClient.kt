package dev.brahmkshatriya.echo.extension.network

import dev.brahmkshatriya.echo.extension.model.Album
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("albums/{id}")
    suspend fun getAlbum(@Path("id") id: String): Album

    @GET("artists/{id}")
    suspend fun getArtist(@Path("id") id: String): Artist
}