package dev.brahmkshatriya.echo.extension.network

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.serializer
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

abstract class BaseHttpClient(private val client: OkHttpClient) {

    protected abstract val baseUrl: String

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val jsonMediaType = "application/json".toMediaType()

    /**
     * Performs a GET request.
     * @param endpoint The API endpoint to append to the baseUrl.
     * @param params A map of query parameters to include in the URL.
     * @return The deserialized response body of type T.
     */
    suspend inline fun <reified T> get(endpoint: String, params: Map<String, Any> = emptyMap()): T {
        val urlBuilder = baseUrl.toHttpUrl().newBuilder().addPathSegments(endpoint)
        params.forEach { (key, value) -> urlBuilder.addQueryParameter(key, value.toString()) }
        val request = Request.Builder().url(urlBuilder.build()).get().build()
        return execute(request)
    }

    /**
     * Performs a POST request with a JSON body.
     * @param endpoint The API endpoint.
     * @param body The object to serialize into the JSON request body.
     * @return The deserialized response body of type T.
     */
    suspend inline fun <reified T> post(endpoint: String, body: Any): T {
        val requestBody = json.encodeToString(body).toRequestBody(jsonMediaType)
        val request = Request.Builder().url(baseUrl.toHttpUrl().newBuilder().addPathSegments(endpoint).build()).post(requestBody).build()
        return execute(request)
    }

    /**
     * Performs a PATCH request with a JSON body.
     * @param endpoint The API endpoint.
     * @param body The object to serialize into the JSON request body.
     * @return The deserialized response body of type T.
     */
    suspend inline fun <reified T> patch(endpoint: String, body: Any): T {
        val requestBody = json.encodeToString(body).toRequestBody(jsonMediaType)
        val request = Request.Builder().url(baseUrl.toHttpUrl().newBuilder().addPathSegments(endpoint).build()).patch(requestBody).build()
        return execute(request)
    }

    /**
     * Performs a DELETE request.
     * @param endpoint The API endpoint.
     * @return The deserialized response body of type T.
     */
    suspend inline fun <reified T> delete(endpoint: String): T {
        val request = Request.Builder().url(baseUrl.toHttpUrl().newBuilder().addPathSegments(endpoint).build()).delete().build()
        return execute(request)
    }

    /**
     * Executes the request and parses the JSON response.
     */
    suspend inline fun <reified T> execute(request: Request): T {
        val response = client.newCall(request).await()
        if (!response.isSuccessful) throw IOException("Unexpected code $response")
        val responseBody = response.body?.string() ?: throw IOException("Empty response body")
        return json.decodeFromString(responseBody)
    }

    /**
     * Awaits the response of a call in a suspending manner.
     */
    private suspend fun Call.await(): Response {
        return suspendCancellableCoroutine { continuation ->
            enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    continuation.resume(response)
                }
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isCancelled) return
                    continuation.resumeWithException(e)
                }
            })
            continuation.onCancellation {
                cancel()
            }
        }
    }
}
