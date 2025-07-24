package dev.brahmkshatriya.echo.extension.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.IOException

class HttpClient(
    private val client: OkHttpClient,
    private val baseUrl: String = "https://dab.yeet.su/api/"
) {
    
    internal val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }
    
    internal val jsonMediaType = "application/json; charset=UTF-8".toMediaType()
    
    /**
     * Performs a GET request and returns the decoded response
     */
    suspend inline fun <reified T> get(
        endpoint: String,
        queryParams: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): T = withContext(Dispatchers.IO) {
        val urlBuilder = (baseUrl + endpoint).toHttpUrl().newBuilder()
        queryParams.forEach { (key, value) ->
            urlBuilder.addQueryParameter(key, value)
        }
        
        val request = Request.Builder()
            .url(urlBuilder.build())
            .get()
            .apply {
                headers.forEach { (key, value) ->
                    addHeader(key, value)
                }
            }
            .build()
        
        executeRequest<T>(request)
    }
    
    /**
     * Performs a POST request with JSON body and returns the decoded response
     */
    suspend inline fun <reified T> post(
        endpoint: String,
        body: Any? = null,
        queryParams: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): T = withContext(Dispatchers.IO) {
        val urlBuilder = (baseUrl + endpoint).toHttpUrl().newBuilder()
        queryParams.forEach { (key, value) ->
            urlBuilder.addQueryParameter(key, value)
        }
        
        val requestBody = when (body) {
            null -> "".toRequestBody(jsonMediaType)
            is String -> body.toRequestBody(jsonMediaType)
            else -> json.encodeToString(body).toRequestBody(jsonMediaType)
        }
        
        val request = Request.Builder()
            .url(urlBuilder.build())
            .post(requestBody)
            .apply {
                headers.forEach { (key, value) ->
                    addHeader(key, value)
                }
            }
            .build()
        
        executeRequest<T>(request)
    }
    
    /**
     * Performs a PATCH request with JSON body and returns the decoded response
     */
    suspend inline fun <reified T> patch(
        endpoint: String,
        body: Any? = null,
        queryParams: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): T = withContext(Dispatchers.IO) {
        val urlBuilder = (baseUrl + endpoint).toHttpUrl().newBuilder()
        queryParams.forEach { (key, value) ->
            urlBuilder.addQueryParameter(key, value)
        }
        
        val requestBody = when (body) {
            null -> "".toRequestBody(jsonMediaType)
            is String -> body.toRequestBody(jsonMediaType)
            else -> json.encodeToString(body).toRequestBody(jsonMediaType)
        }
        
        val request = Request.Builder()
            .url(urlBuilder.build())
            .patch(requestBody)
            .apply {
                headers.forEach { (key, value) ->
                    addHeader(key, value)
                }
            }
            .build()
        
        executeRequest<T>(request)
    }
    
    /**
     * Performs a DELETE request and returns the decoded response
     */
    suspend inline fun <reified T> delete(
        endpoint: String,
        queryParams: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): T = withContext(Dispatchers.IO) {
        val urlBuilder = (baseUrl + endpoint).toHttpUrl().newBuilder()
        queryParams.forEach { (key, value) ->
            urlBuilder.addQueryParameter(key, value)
        }
        
        val request = Request.Builder()
            .url(urlBuilder.build())
            .delete()
            .apply {
                headers.forEach { (key, value) ->
                    addHeader(key, value)
                }
            }
            .build()
        
        executeRequest<T>(request)
    }
    
    /**
     * Executes the request and handles the response
     */
    internal suspend inline fun <reified T> executeRequest(request: Request): T {
        val response = client.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw IOException("HTTP ${response.code}: ${response.message}")
        }
        
        val responseBody = response.body?.string() 
            ?: throw IOException("Empty response body")
        
        return json.decodeFromString<T>(responseBody)
    }
    
    /**
     * Performs a raw request and returns the string response
     */
    suspend fun raw(
        method: String,
        endpoint: String,
        body: Any? = null,
        queryParams: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): String = withContext(Dispatchers.IO) {
        val urlBuilder = (baseUrl + endpoint).toHttpUrl().newBuilder()
        queryParams.forEach { (key, value) ->
            urlBuilder.addQueryParameter(key, value)
        }
        
        val requestBody = when {
            body == null -> null
            body is String -> body.toRequestBody(jsonMediaType)
            else -> json.encodeToString(body).toRequestBody(jsonMediaType)
        }
        
        val request = Request.Builder()
            .url(urlBuilder.build())
            .method(method.uppercase(), requestBody)
            .apply {
                headers.forEach { (key, value) ->
                    addHeader(key, value)
                }
            }
            .build()
        
        val response = client.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw IOException("HTTP ${response.code}: ${response.message}")
        }
        
        response.body?.string() ?: throw IOException("Empty response body")
    }
}