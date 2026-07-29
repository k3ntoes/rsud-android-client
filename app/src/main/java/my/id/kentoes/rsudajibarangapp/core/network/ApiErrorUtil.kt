package my.id.kentoes.rsudajibarangapp.core.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Response
import my.id.kentoes.rsudajibarangapp.sync.api.ApiErrorDto

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
        return try {
            json.decodeFromString<ApiErrorDto>(body)
        } catch (_: Exception) {
            null
        }
    }
}
