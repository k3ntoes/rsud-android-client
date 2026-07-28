package my.id.kentoes.rsudajibarangapp.master

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.MasterDataItem
import my.id.kentoes.rsudajibarangapp.core.database.entity.RuangEntity
import my.id.kentoes.rsudajibarangapp.master.api.ItemOut
import my.id.kentoes.rsudajibarangapp.master.api.MasterDataApi
import my.id.kentoes.rsudajibarangapp.master.api.RoomOut
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MasterDataRepositoryTest {

    private lateinit var api: MasterDataApi
    private lateinit var dao: MasterDataDao
    private lateinit var repository: MasterDataRepository

    private val sampleItems = listOf(
        MasterDataItem(1, "Meja", "Furnitur", "Meja kayu"),
        MasterDataItem(2, "Kursi", "Furnitur", "Kursi plastik"),
    )
    private val sampleRooms = listOf(
        RuangEntity(1, "Ruang 1", "Lantai 1"),
        RuangEntity(2, "Ruang 2", "Lantai 2"),
    )

    @Before
    fun setup() {
        api = mockk()
        dao = mockk()
        // items dan rooms dipanggil di konstruktor repository (property initializers)
        // Setup mock SEBELUM instansiasi
        every { dao.getAllItems() } returns flowOf(sampleItems)
        every { dao.getAllRooms() } returns flowOf(sampleRooms)
        repository = MasterDataRepository(api, dao)
    }

    // ── Cache availability ────────────────────────────────

    @Test
    fun `isCacheAvailable returns true when items exist`() = runTest {
        // items di-setup di @Before dengan 2 sample items
        assertTrue(repository.isCacheAvailable())
    }

    @Test
    fun `isCacheAvailable returns false when items empty`() = runTest {
        // Override mock untuk test ini
        every { dao.getAllItems() } returns flowOf(emptyList())
        assertFalse(repository.isCacheAvailable())
    }

    // ── Sync from API — success ───────────────────────────

    @Test
    fun `syncFromApi inserts mapped items and rooms on full success`() = runTest {
        val apiItems = listOf(
            ItemOut(1, "Meja"),
            ItemOut(2, "Kursi"),
        )
        val apiRooms = listOf(
            RoomOut(1, "Ruang 1"),
        )
        coEvery { api.getItems() } returns apiItems
        coEvery { api.getRooms() } returns apiRooms
        coEvery { dao.insertItems(any()) } returns Unit
        coEvery { dao.insertRooms(any()) } returns Unit

        val result = repository.syncFromApi()

        assertEquals("Data berhasil diperbarui", result)
        coVerify(exactly = 1) { dao.insertItems(any()) }
        coVerify(exactly = 1) { dao.insertRooms(any()) }
    }

    @Test
    fun `syncFromApi maps API response fields correctly to entities`() = runTest {
        val apiItems = listOf(
            ItemOut(id = 10, name = "AC", isActive = true)
        )
        val apiRooms = listOf(
            RoomOut(id = 5, name = "IGD", isActive = true)
        )
        coEvery { api.getItems() } returns apiItems
        coEvery { api.getRooms() } returns apiRooms
        coEvery { dao.insertItems(any()) } returns Unit
        coEvery { dao.insertRooms(any()) } returns Unit

        repository.syncFromApi()

        coVerify {
            dao.insertItems(match { items ->
                items.size == 1 &&
                    items[0].id == 10L &&
                    items[0].nama == "AC" &&
                    items[0].kategori == "" &&
                    items[0].deskripsi == null &&
                    items[0].isActive
            })
            dao.insertRooms(match { rooms ->
                rooms.size == 1 &&
                    rooms[0].id == 5L &&
                    rooms[0].nama == "IGD" &&
                    rooms[0].lantai == null &&
                    rooms[0].isActive
            })
        }
    }

    // ── Sync from API — partial / empty ──────────────────

    @Test
    fun `syncFromApi does not insert items when items list is empty`() = runTest {
        coEvery { api.getItems() } returns emptyList()
        coEvery { api.getRooms() } returns emptyList()
        coEvery { dao.insertItems(any()) } returns Unit
        coEvery { dao.insertRooms(any()) } returns Unit

        val result = repository.syncFromApi()

        assertEquals("Data berhasil diperbarui", result)
        coVerify(exactly = 0) { dao.insertItems(any()) }
        coVerify(exactly = 0) { dao.insertRooms(any()) }
    }

    @Test
    fun `syncFromApi does not insert rooms when rooms list is empty`() = runTest {
        coEvery { api.getItems() } returns listOf(ItemOut(1, "Meja"))
        coEvery { api.getRooms() } returns emptyList()
        coEvery { dao.insertItems(any()) } returns Unit
        coEvery { dao.insertRooms(any()) } returns Unit

        repository.syncFromApi()

        coVerify(exactly = 1) { dao.insertItems(any()) }
        coVerify(exactly = 0) { dao.insertRooms(any()) }
    }

    @Test
    fun `syncFromApi does not insert when both lists are empty`() = runTest {
        coEvery { api.getItems() } returns emptyList()
        coEvery { api.getRooms() } returns emptyList()
        coEvery { dao.insertItems(any()) } returns Unit
        coEvery { dao.insertRooms(any()) } returns Unit

        val result = repository.syncFromApi()

        assertEquals("Data berhasil diperbarui", result)
        coVerify(exactly = 0) { dao.insertItems(any()) }
        coVerify(exactly = 0) { dao.insertRooms(any()) }
    }

    @Test
    fun `syncFromApi inserts rooms even when items list is empty`() = runTest {
        coEvery { api.getItems() } returns emptyList()
        coEvery { api.getRooms() } returns sampleRooms.map {
            RoomOut(it.id, it.nama)
        }
        coEvery { dao.insertItems(any()) } returns Unit
        coEvery { dao.insertRooms(any()) } returns Unit

        val result = repository.syncFromApi()

        assertEquals("Data berhasil diperbarui", result)
        coVerify(exactly = 0) { dao.insertItems(any()) }
        coVerify(exactly = 1) { dao.insertRooms(any()) }
    }

    // ── Sync from API — exception handling ────────────────

    @Test(expected = RuntimeException::class)
    fun `syncFromApi throws when items API call fails`() = runTest {
        coEvery { api.getItems() } throws RuntimeException("Connection timeout")

        repository.syncFromApi()
    }

    @Test(expected = RuntimeException::class)
    fun `syncFromApi throws when rooms API call fails`() = runTest {
        val apiItems = listOf(ItemOut(1, "Meja"))
        coEvery { api.getItems() } returns apiItems
        coEvery { api.getRooms() } throws RuntimeException("Rooms server error")

        repository.syncFromApi()
    }

    @Test(expected = RuntimeException::class)
    fun `syncFromApi throws when both API calls fail`() = runTest {
        coEvery { api.getItems() } throws RuntimeException("Network down")

        repository.syncFromApi()
    }
}
