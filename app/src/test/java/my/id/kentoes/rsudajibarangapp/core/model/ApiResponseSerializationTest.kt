package my.id.kentoes.rsudajibarangapp.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiResponseSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    data class TestItem(val id: Long, val name: String)

    // ── SyncResponse ──

    @Test
    fun `SyncResponse deserializes data and synced_at`() {
        val raw = """{"data":[{"id":1,"name":"Meja"},{"id":2,"name":"Kursi"}],"synced_at":"2026-01-01T00:00:00Z"}"""
        val response = json.decodeFromString<SyncResponse<TestItem>>(raw)
        assertEquals(2, response.data.size)
        assertEquals("Meja", response.data[0].name)
        assertEquals("Kursi", response.data[1].name)
        assertEquals("2026-01-01T00:00:00Z", response.syncedAt)
    }

    @Test
    fun `SyncResponse handles empty data`() {
        val raw = """{"data":[],"synced_at":"2026-01-01T00:00:00Z"}"""
        val response = json.decodeFromString<SyncResponse<TestItem>>(raw)
        assertTrue(response.data.isEmpty())
        assertNotNull(response.syncedAt)
    }

    @Test
    fun `SyncResponse handles null synced_at`() {
        val raw = """{"data":[{"id":1,"name":"Meja"}]}"""
        val response = json.decodeFromString<SyncResponse<TestItem>>(raw)
        assertEquals(1, response.data.size)
        assertNull(response.syncedAt)
    }

    @Test
    fun `SyncResponse handles missing synced_at field gracefully`() {
        val raw = """{"data":[{"id":1,"name":"Meja"}]}"""
        val response = json.decodeFromString<SyncResponse<TestItem>>(raw)
        assertEquals(1, response.data.size)
        assertNull(response.syncedAt)
    }

    @Test
    fun `SyncResponse serializes back correctly`() {
        val response = SyncResponse(
            data = listOf(TestItem(1, "Meja")),
            syncedAt = "2026-07-01T00:00:00Z"
        )
        val raw = json.encodeToString(SyncResponse.serializer(TestItem.serializer()), response)
        assertTrue(raw.contains("\"data\""))
        assertTrue(raw.contains("\"synced_at\""))
        assertTrue(raw.contains("Meja"))
    }

    // ── PaginatedResponse ──

    @Test
    fun `PaginatedResponse deserializes all fields with snake_case`() {
        val raw = """{"items":[{"id":1,"name":"Item A"}],"total":50,"page":2,"per_page":20,"total_pages":3}"""
        val response = json.decodeFromString<PaginatedResponse<TestItem>>(raw)
        assertEquals(1, response.items.size)
        assertEquals("Item A", response.items[0].name)
        assertEquals(50, response.total)
        assertEquals(2, response.page)
        assertEquals(20, response.perPage)
        assertEquals(3, response.totalPages)
    }

    @Test
    fun `PaginatedResponse uses defaults for missing fields`() {
        val raw = """{"items":[{"id":1,"name":"Test"}]}"""
        val response = json.decodeFromString<PaginatedResponse<TestItem>>(raw)
        assertEquals(1, response.items.size)
        assertEquals(0, response.total)
        assertEquals(1, response.page)
        assertEquals(20, response.perPage)
        assertEquals(1, response.totalPages)
    }

    @Test
    fun `PaginatedResponse handles empty items`() {
        val raw = """{"items":[],"total":0,"page":1,"per_page":20,"total_pages":0}"""
        val response = json.decodeFromString<PaginatedResponse<TestItem>>(raw)
        assertTrue(response.items.isEmpty())
        assertEquals(0, response.total)
        assertEquals(1, response.page)
        assertEquals(0, response.totalPages)
    }

    @Test
    fun `PaginatedResponse ignores extra fields`() {
        val raw = """{"items":[{"id":1,"name":"X"}],"total":10,"page":1,"per_page":20,"total_pages":1,"extra_field":"ignored"}"""
        val response = json.decodeFromString<PaginatedResponse<TestItem>>(raw)
        assertEquals(1, response.items.size)
        assertEquals(10, response.total)
    }

    @Test
    fun `PaginatedResponse serializes back correctly`() {
        val response = PaginatedResponse(
            items = listOf(TestItem(1, "Test")),
            total = 100,
            page = 1,
            perPage = 20,
            totalPages = 5
        )
        val raw = json.encodeToString(PaginatedResponse.serializer(TestItem.serializer()), response)
        // Round-trip: serialize then deserialize, verify values preserved
        val decoded = json.decodeFromString(PaginatedResponse.serializer(TestItem.serializer()), raw)
        assertEquals(1, decoded.items.size)
        assertEquals("Test", decoded.items[0].name)
        assertEquals(100, decoded.total)
        assertEquals(1, decoded.page)
        assertEquals(20, decoded.perPage)
        assertEquals(5, decoded.totalPages)
    }
}
