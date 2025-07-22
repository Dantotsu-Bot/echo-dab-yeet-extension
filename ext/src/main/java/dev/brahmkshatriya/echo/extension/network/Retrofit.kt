package dev.brahmkshatriya.echo.extension.network

import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitClient {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    private val contentType = "application/json; charset=UTF-8".toMediaType()

    private val client = OkHttpClient.Builder().build()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://dab.yeet.su/api/")
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }
}