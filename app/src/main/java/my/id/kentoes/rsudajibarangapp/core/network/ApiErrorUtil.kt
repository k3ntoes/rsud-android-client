package my.id.kentoes.rsudajibarangapp.core.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import my.id.kentoes.rsudajibarangapp.sync.api.ApiErrorDto
import okhttp3.Response
import okhttp3.ResponseBody

/**
 * Utility for parsing standard error codes from API responses.
 */
object ApiErrorUtil {

    private val json = Json { ignoreUnknownKeys = true }

    fun extractErrorCode(response: Response): String? {
        val body = response.peekBody(2048).string()
        return try {
            val obj = json.decodeFromString<JsonObject>(body)
            obj["code"]?.jsonPrimitive?.content
        } catch (_: Exception) {
            null
        }
    }

    fun extractErrorDetail(response: Response): String? {
        val body = response.peekBody(2048).string()
        return try {
            val obj = json.decodeFromString<JsonObject>(body)
            obj["detail"]?.jsonPrimitive?.content
        } catch (_: Exception) {
            null
        }
    }

    fun extractErrorDto(response: Response): ApiErrorDto? {
        val body = response.peekBody(2048).string()
        return decodeErrorDto(body)
    }

    /**
     * Parse error DTO dari response body okhttp (mis. Retrofit `Response.errorBody()`).
     * Membaca via `source().peek()` — NON-consuming, jadi body bisa dibaca berulang
     * (mis. helper retry membacanya, lalu outer catch membacanya lagi). `raw().peekBody()`
     * TIDAK bisa dipakai untuk error body Retrofit (NoContentResponseBody —
     * IllegalStateException), sedangkan `errorBody()` aman.
     */
    fun extractErrorDto(body: ResponseBody?): ApiErrorDto? {
        val text = try {
            body?.source()?.peek()?.readUtf8()
        } catch (_: Exception) {
            null
        } ?: return null
        return decodeErrorDto(text)
    }

    private fun decodeErrorDto(body: String): ApiErrorDto? = try {
        json.decodeFromString<ApiErrorDto>(body)
    } catch (_: Exception) {
        null
    }
}
