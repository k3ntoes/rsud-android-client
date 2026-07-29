package my.id.kentoes.rsudajibarangapp.core.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ApiErrorUtilTest {

    private val request = Request.Builder().url("https://test.api/").build()

    private fun mockResponse(code: Int, body: String): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("")
            .body(body.toResponseBody("application/json".toMediaType()))
            .build()
    }

    @Test
    fun `extractErrorCode returns code from JSON body`() {
        val response = mockResponse(400, """{"detail":"Invalid token","code":"TOKEN_INVALID"}""")
        assertEquals("TOKEN_INVALID", ApiErrorUtil.extractErrorCode(response))
    }

    @Test
    fun `extractErrorCode returns null when no code field`() {
        val response = mockResponse(400, """{"detail":"Bad request"}""")
        assertNull(ApiErrorUtil.extractErrorCode(response))
    }

    @Test
    fun `extractErrorCode returns null when body is empty`() {
        val response = mockResponse(400, "")
        assertNull(ApiErrorUtil.extractErrorCode(response))
    }

    @Test
    fun `extractErrorCode returns null when body is not JSON`() {
        val response = mockResponse(500, "Internal Server Error")
        assertNull(ApiErrorUtil.extractErrorCode(response))
    }

    @Test
    fun `extractErrorDetail returns detail from JSON body`() {
        val response = mockResponse(401, """{"detail":"Access token expired","code":"TOKEN_EXPIRED"}""")
        assertEquals("Access token expired", ApiErrorUtil.extractErrorDetail(response))
    }

    @Test
    fun `extractErrorDetail returns null when no detail field`() {
        val response = mockResponse(403, """{"code":"FORBIDDEN"}""")
        assertNull(ApiErrorUtil.extractErrorDetail(response))
    }

    @Test
    fun `extractErrorDetail returns null when body is malformed`() {
        val response = mockResponse(400, "not json")
        assertNull(ApiErrorUtil.extractErrorDetail(response))
    }

    @Test
    fun `extractErrorDto returns full DTO`() {
        val response = mockResponse(409, """{"detail":"Duplicate inspection","code":"DUPLICATE_INSPECTION"}""")
        val dto = ApiErrorUtil.extractErrorDto(response)
        assertNotNull(dto)
        assertEquals("Duplicate inspection", dto!!.detail)
        assertEquals("DUPLICATE_INSPECTION", dto.code)
    }

    @Test
    fun `extractErrorDto returns null when JSON is invalid`() {
        val response = mockResponse(400, "{{{broken}}")
        assertNull(ApiErrorUtil.extractErrorDto(response))
    }

    @Test
    fun `extractErrorDto handles extra fields gracefully`() {
        val response = mockResponse(422, """{"detail":"Validation error","code":"VALIDATION_ERROR","field":"name","timestamp":"2026-01-01"}""")
        val dto = ApiErrorUtil.extractErrorDto(response)
        assertNotNull(dto)
        assertEquals("Validation error", dto!!.detail)
        assertEquals("VALIDATION_ERROR", dto.code)
    }

    @Test
    fun `extractErrorCode handles 500 with JSON body`() {
        val response = mockResponse(500, """{"detail":"Internal error","code":"SERVER_ERROR"}""")
        assertEquals("SERVER_ERROR", ApiErrorUtil.extractErrorCode(response))
    }

    @Test
    fun `extractErrorDetail handles 403 with code only`() {
        val response = mockResponse(403, """{"code":"FORBIDDEN"}""")
        assertNull(ApiErrorUtil.extractErrorDetail(response))
    }
}
