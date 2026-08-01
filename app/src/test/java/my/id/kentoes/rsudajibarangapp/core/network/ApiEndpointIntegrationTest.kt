package my.id.kentoes.rsudajibarangapp.core.network

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import my.id.kentoes.rsudajibarangapp.auth.api.AuthApi
import my.id.kentoes.rsudajibarangapp.auth.api.ChangePasswordRequest
import my.id.kentoes.rsudajibarangapp.auth.api.LoginRequest
import my.id.kentoes.rsudajibarangapp.auth.api.LogoutRequest
import my.id.kentoes.rsudajibarangapp.auth.api.RefreshRequest
import my.id.kentoes.rsudajibarangapp.master.api.ItemOut
import my.id.kentoes.rsudajibarangapp.master.api.MasterDataApi
import my.id.kentoes.rsudajibarangapp.master.api.RoomOut
import my.id.kentoes.rsudajibarangapp.sync.api.DetailSubmit
import my.id.kentoes.rsudajibarangapp.sync.api.InspectionSubmit
import my.id.kentoes.rsudajibarangapp.sync.api.PhotoSubmit
import my.id.kentoes.rsudajibarangapp.sync.api.SyncApi
import my.id.kentoes.rsudajibarangapp.sync.api.UploadPhotoResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Integration test menggunakan MockWebServer untuk memverifikasi bahwa
 * setiap endpoint Retrofit dipanggil dengan HTTP method, path, dan
 * format body yang benar sesuai kontrak API.
 */
class ApiEndpointIntegrationTest {

    private lateinit var mockServer: MockWebServer
    private lateinit var json: Json

    private lateinit var authApi: AuthApi
    private lateinit var masterDataApi: MasterDataApi
    private lateinit var syncApi: SyncApi

    @Before
    fun setup() {
        mockServer = MockWebServer()
        mockServer.start()

        json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = true
            coerceInputValues = true
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockServer.url("/"))  // base path, endpoint paths akan ditambahkan
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        authApi = retrofit.create(AuthApi::class.java)
        masterDataApi = retrofit.create(MasterDataApi::class.java)
        syncApi = retrofit.create(SyncApi::class.java)
    }

    @After
    fun teardown() {
        mockServer.shutdown()
    }

    // ── Helper ──

    private fun enqueueJson(responseBody: String, statusCode: Int = 200) {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(statusCode)
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody)
        )
    }

    private fun assertRequest(req: RecordedRequest, method: String, path: String) {
        assertEquals("HTTP method mismatch", method, req.method)
        // req.path includes query params — use encodedPath for path-only comparison
        val actualPath = req.requestUrl?.encodedPath
        assertEquals("URL path mismatch", path, actualPath)
    }

    private fun assertBodyContains(req: RecordedRequest, vararg expectedFields: String) {
        val body = req.body.readUtf8()
        for (field in expectedFields) {
            assertTrue("Request body should contain '$field' but was: $body", body.contains(field))
        }
    }

    // ═══════════════════════════════════════════════
    // 🔐 AUTH ENDPOINTS
    // ═══════════════════════════════════════════════

    @Test
    fun `POST auth login - correct path and body fields`() = runTest {
        enqueueJson("""{"access_token":"at","refresh_token":"rt","user":{"id":1,"username":"u","role":"inspector","is_active":true}}""")

        authApi.login(LoginRequest(username = "petugas01", password = "rahasia"))

        val req = mockServer.takeRequest()
        assertRequest(req, "POST", "/auth/login")
        assertBodyContains(req, "\"username\"", "\"password\"")
    }

    @Test
    fun `POST auth refresh - correct path and body fields`() = runTest {
        enqueueJson("""{"access_token":"at2","refresh_token":"rt2","user":{"id":1,"username":"u","role":"inspector","is_active":true}}""")

        authApi.refresh(RefreshRequest(refreshToken = "old-rt"))

        val req = mockServer.takeRequest()
        assertRequest(req, "POST", "/auth/refresh")
        assertBodyContains(req, "\"refresh_token\"")
    }

    @Test
    fun `POST auth logout - correct path and body fields with both tokens`() = runTest {
        enqueueJson("{}")

        authApi.logout(LogoutRequest(refreshToken = "rt", accessToken = "at"))

        val req = mockServer.takeRequest()
        assertRequest(req, "POST", "/auth/logout")
        assertBodyContains(req, "\"refresh_token\"", "\"access_token\"")
    }

    @Test
    fun `POST auth change-password - correct path and body fields`() = runTest {
        enqueueJson("{}")

        authApi.changePassword(ChangePasswordRequest(oldPassword = "old", newPassword = "new"))

        val req = mockServer.takeRequest()
        assertRequest(req, "POST", "/auth/change-password")
        assertBodyContains(req, "\"old_password\"", "\"new_password\"")
    }

    @Test
    fun `GET auth me - correct path`() = runTest {
        enqueueJson("""{"id":1,"username":"u","role":"inspector","is_active":true}""")

        authApi.me()

        val req = mockServer.takeRequest()
        assertRequest(req, "GET", "/auth/me")
    }

    // ═══════════════════════════════════════════════
    // 🏗️ MASTER DATA ENDPOINTS
    // ═══════════════════════════════════════════════

    @Test
    fun `GET rooms - correct path, returns SyncResponse`() = runTest {
        enqueueJson("""{"data":[{"id":1,"name":"Ruang A","is_active":true}],"synced_at":"2026-01-01T00:00:00Z"}""")

        val response = masterDataApi.getRooms()

        val req = mockServer.takeRequest()
        assertRequest(req, "GET", "/rooms")
        assertEquals(1, response.data.size)
        assertEquals("Ruang A", response.data[0].name)
    }

    @Test
    fun `GET inspection-items - correct path, returns SyncResponse`() = runTest {
        enqueueJson("""{"data":[{"id":1,"name":"Meja","is_active":true}],"synced_at":"2026-01-01T00:00:00Z"}""")

        val response = masterDataApi.getItems()

        val req = mockServer.takeRequest()
        assertRequest(req, "GET", "/inspection-items")
        assertEquals(1, response.data.size)
        assertEquals("Meja", response.data[0].name)
    }

    // ═══════════════════════════════════════════════
    // 📤 SYNC ENDPOINTS
    // ═══════════════════════════════════════════════

    @Test
    fun `POST sync upload - correct path and multipart field name`() = runTest {
        // Enqueue response with correct field names per contract
        enqueueJson("""{"photo_file_name":"server.jpg","thumbnail_file_name":null}""")

        // Create a temporary file to upload
        val tempFile = File.createTempFile("test_photo_", ".jpg")
        try {
            tempFile.writeBytes(ByteArray(100))
            val requestBody = tempFile.asRequestBody("image/jpeg".toMediaType())
            val multipart = MultipartBody.Part.createFormData("file", tempFile.name, requestBody)

            syncApi.uploadPhoto(multipart)

            val req = mockServer.takeRequest()
            assertRequest(req, "POST", "/upload")
            // Verify Content-Disposition header contains name="file"
            val body = req.body.readUtf8()
            assertTrue("Multipart body should contain name=\"file\"", body.contains("name=\"file\""))
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun `POST sync inspection - correct path and body field names, returns InspectionOut`() = runTest {
        enqueueJson("""{"id":1,"room_id":1,"inspector_id":5,"status":"PENDING"}""")

        val submit = InspectionSubmit(
            roomId = 1,
            localTimestamp = "2026-07-28T10:00:00Z",
            businessDate = "2026-07-28",
            details = listOf(
                DetailSubmit(
                    itemId = 1,
                    score = 2,
                    catatan = "Catatan inspektur",
                    photos = listOf(
                        PhotoSubmit(fileName = "server_a.jpg", sortOrder = 0)
                    )
                )
            )
        )

        val response = syncApi.submitInspection(submit)

        val req = mockServer.takeRequest()
        assertRequest(req, "POST", "/inspections")
        assertBodyContains(req,
            "\"room_id\"", "\"local_timestamp\"", "\"business_date\"",
            "\"item_id\"", "\"score\"", "\"catatan\"", "\"file_name\"", "\"sort_order\""
        )
        assertEquals(1L, response.id)
        assertEquals("PENDING", response.status)
    }

    // ═══════════════════════════════════════════════
    // 🧪 RESPONSE DESERIALIZATION TESTS
    // ═══════════════════════════════════════════════

    @Test
    fun `auth login response deserializes snake_case correctly`() = runTest {
        enqueueJson("""{"access_token":"at","refresh_token":"rt","user":{"id":1,"username":"u","role":"inspector","is_active":true}}""")

        val response = authApi.login(LoginRequest("u", "p"))

        assertEquals("at", response.accessToken)
        assertEquals("rt", response.refreshToken)
        assertEquals(1, response.user.id)
        assertEquals(true, response.user.isActive)
    }

    @Test
    fun `master data response deserializes snake_case correctly`() = runTest {
        enqueueJson("""{"data":[{"id":1,"name":"Meja","is_active":true,"updated_at":"2026-01-01T00:00:00Z"}],"synced_at":"2026-01-01T00:00:00Z"}""")

        val response = masterDataApi.getItems()

        assertEquals(1, response.data.size)
        assertEquals(1L, response.data[0].id)
        assertEquals("Meja", response.data[0].name)
        assertEquals(true, response.data[0].isActive)
        assertNotNull(response.data[0].updatedAt)
    }

    @Test
    fun `sync upload response deserializes photo_file_name correctly`() = runTest {
        enqueueJson("""{"photo_file_name":"uuid-photo.jpg","thumbnail_file_name":"thumb-uuid.jpg"}""")

        val tempFile = File.createTempFile("test_", ".jpg")
        try {
            tempFile.writeBytes(ByteArray(100))
            val multipart = MultipartBody.Part.createFormData(
                "file", tempFile.name,
                tempFile.asRequestBody("image/jpeg".toMediaType())
            )

            val response: UploadPhotoResponse = syncApi.uploadPhoto(multipart)

            assertEquals("uuid-photo.jpg", response.fileName)
            assertEquals("thumb-uuid.jpg", response.thumbnailName)
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun `sync upload response with null thumbnail`() = runTest {
        enqueueJson("""{"photo_file_name":"uuid-photo.jpg","thumbnail_file_name":null}""")

        val tempFile = File.createTempFile("test_", ".jpg")
        try {
            tempFile.writeBytes(ByteArray(100))
            val multipart = MultipartBody.Part.createFormData(
                "file", tempFile.name,
                tempFile.asRequestBody("image/jpeg".toMediaType())
            )

            val response: UploadPhotoResponse = syncApi.uploadPhoto(multipart)

            assertEquals("uuid-photo.jpg", response.fileName)
            assertEquals(null, response.thumbnailName)
        } finally {
            tempFile.delete()
        }
    }
}
