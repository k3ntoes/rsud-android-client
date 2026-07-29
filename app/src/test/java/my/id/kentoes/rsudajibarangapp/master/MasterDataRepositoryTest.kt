package my.id.kentoes.rsudajibarangapp.master

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import my.id.kentoes.rsudajibarangapp.auth.api.AuthApi
import my.id.kentoes.rsudajibarangapp.auth.api.UserOut
import my.id.kentoes.rsudajibarangapp.auth.api.UserRoomDto
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.MasterDataItem
import my.id.kentoes.rsudajibarangapp.core.database.entity.RoomItemEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.RuangEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.UserEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.UserRoomEntity
import my.id.kentoes.rsudajibarangapp.core.model.SyncResponse
import my.id.kentoes.rsudajibarangapp.master.api.ItemOut
import my.id.kentoes.rsudajibarangapp.master.api.MasterDataApi
import my.id.kentoes.rsudajibarangapp.master.api.RoomItemDto
import my.id.kentoes.rsudajibarangapp.master.api.RoomOut
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MasterDataRepositoryTest {

    private lateinit var api: MasterDataApi
    private lateinit var authApi: AuthApi
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
        authApi = mockk()
        dao = mockk()
        every { dao.getAllItems() } returns flowOf(sampleItems)
        every { dao.getAllRooms() } returns flowOf(sampleRooms)
        repository = MasterDataRepository(api, authApi, dao)
    }

    // ── Cache availability ──

    @Test
    fun `isCacheAvailable returns true when items exist`() = runTest {
        assertTrue(repository.isCacheAvailable())
    }

    @Test
    fun `isCacheAvailable returns false when items empty`() = runTest {
        every { dao.getAllItems() } returns flowOf(emptyList())
        assertFalse(repository.isCacheAvailable())
    }

    // ── Sync from API — success ──

    @Test
    fun `syncFromApi inserts mapped items and rooms on full success`() = runTest {
        val apiItems = SyncResponse(data = listOf(ItemOut(1, "Meja"), ItemOut(2, "Kursi")))
        val apiRooms = SyncResponse(data = listOf(RoomOut(1, "Ruang 1")))
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
        val apiItems = SyncResponse(data = listOf(ItemOut(id = 10, name = "AC", isActive = true)))
        val apiRooms = SyncResponse(data = listOf(RoomOut(id = 5, name = "IGD", isActive = true)))
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

    // ── Sync from API — partial / empty ──

    @Test
    fun `syncFromApi does not insert items when items list is empty`() = runTest {
        coEvery { api.getItems() } returns SyncResponse(data = emptyList())
        coEvery { api.getRooms() } returns SyncResponse(data = emptyList())
        coEvery { dao.insertItems(any()) } returns Unit
        coEvery { dao.insertRooms(any()) } returns Unit

        val result = repository.syncFromApi()

        assertEquals("Data berhasil diperbarui", result)
        coVerify(exactly = 0) { dao.insertItems(any()) }
        coVerify(exactly = 0) { dao.insertRooms(any()) }
    }

    @Test
    fun `syncFromApi does not insert rooms when rooms list is empty`() = runTest {
        coEvery { api.getItems() } returns SyncResponse(data = listOf(ItemOut(1, "Meja")))
        coEvery { api.getRooms() } returns SyncResponse(data = emptyList())
        coEvery { dao.insertItems(any()) } returns Unit
        coEvery { dao.insertRooms(any()) } returns Unit

        repository.syncFromApi()

        coVerify(exactly = 1) { dao.insertItems(any()) }
        coVerify(exactly = 0) { dao.insertRooms(any()) }
    }

    @Test
    fun `syncFromApi does not insert when both lists are empty`() = runTest {
        coEvery { api.getItems() } returns SyncResponse(data = emptyList())
        coEvery { api.getRooms() } returns SyncResponse(data = emptyList())
        coEvery { dao.insertItems(any()) } returns Unit
        coEvery { dao.insertRooms(any()) } returns Unit

        val result = repository.syncFromApi()

        assertEquals("Data berhasil diperbarui", result)
        coVerify(exactly = 0) { dao.insertItems(any()) }
        coVerify(exactly = 0) { dao.insertRooms(any()) }
    }

    @Test
    fun `syncFromApi inserts rooms even when items list is empty`() = runTest {
        coEvery { api.getItems() } returns SyncResponse(data = emptyList())
        coEvery { api.getRooms() } returns SyncResponse(data = sampleRooms.map { RoomOut(it.id, it.nama) })
        coEvery { dao.insertItems(any()) } returns Unit
        coEvery { dao.insertRooms(any()) } returns Unit

        val result = repository.syncFromApi()

        assertEquals("Data berhasil diperbarui", result)
        coVerify(exactly = 0) { dao.insertItems(any()) }
        coVerify(exactly = 1) { dao.insertRooms(any()) }
    }

    // ── Sync from API — exception handling ──

    @Test(expected = RuntimeException::class)
    fun `syncFromApi throws when items API call fails`() = runTest {
        coEvery { api.getItems() } throws RuntimeException("Connection timeout")
        repository.syncFromApi()
    }

    @Test(expected = RuntimeException::class)
    fun `syncFromApi throws when rooms API call fails`() = runTest {
        coEvery { api.getItems() } returns SyncResponse(data = listOf(ItemOut(1, "Meja")))
        coEvery { api.getRooms() } throws RuntimeException("Rooms server error")
        repository.syncFromApi()
    }

    @Test(expected = RuntimeException::class)
    fun `syncFromApi throws when both API calls fail`() = runTest {
        coEvery { api.getItems() } throws RuntimeException("Network down")
        repository.syncFromApi()
    }

    // ═══════════════════════════════════════════════
    // syncRoomItems
    // ═══════════════════════════════════════════════

    @Test
    fun `syncRoomItems clears then inserts mapped items`() = runTest {
        val apiResult = SyncResponse(data = listOf(
            RoomItemDto(id = 1, roomId = 10, itemId = 100, createdAt = "2026-01-01T00:00:00Z"),
            RoomItemDto(id = 2, roomId = 10, itemId = 200),
        ))
        coEvery { api.getRoomItems() } returns apiResult
        coEvery { dao.clearRoomItems() } returns Unit
        coEvery { dao.insertRoomItems(any()) } returns Unit

        repository.syncRoomItems()

        coVerify(exactly = 1) { dao.clearRoomItems() }
        coVerify {
            dao.insertRoomItems(match { items ->
                items.size == 2 &&
                    items[0].id == 1L &&
                    items[0].roomId == 10L &&
                    items[0].itemId == 100L &&
                    items[0].createdAt == "2026-01-01T00:00:00Z" &&
                    items[1].id == 2L &&
                    items[1].roomId == 10L &&
                    items[1].itemId == 200L &&
                    items[1].createdAt == null
            })
        }
    }

    @Test
    fun `syncRoomItems clears but does not insert when API returns empty`() = runTest {
        coEvery { api.getRoomItems() } returns SyncResponse(data = emptyList())
        coEvery { dao.clearRoomItems() } returns Unit

        repository.syncRoomItems()

        coVerify(exactly = 1) { dao.clearRoomItems() }
        coVerify(exactly = 0) { dao.insertRoomItems(any()) }
    }

    @Test(expected = RuntimeException::class)
    fun `syncRoomItems throws when API fails`() = runTest {
        coEvery { api.getRoomItems() } throws RuntimeException("Room items timeout")
        repository.syncRoomItems()
    }

    // ═══════════════════════════════════════════════
    // syncUsers
    // ═══════════════════════════════════════════════

    @Test
    fun `syncUsers clears then inserts mapped users`() = runTest {
        val apiUsers = listOf(
            UserOut(id = 1, username = "petugas01", role = "inspector", isActive = true),
            UserOut(id = 2, username = "supervisor01", role = "supervisor", isActive = true),
        )
        coEvery { authApi.getUsers() } returns apiUsers
        coEvery { dao.clearUsers() } returns Unit
        coEvery { dao.insertUsers(any()) } returns Unit

        repository.syncUsers()

        coVerify(exactly = 1) { dao.clearUsers() }
        coVerify {
            dao.insertUsers(match { users ->
                users.size == 2 &&
                    users[0].id == 1 &&
                    users[0].username == "petugas01" &&
                    users[0].role == "inspector" &&
                    users[0].isActive &&
                    users[1].id == 2 &&
                    users[1].username == "supervisor01" &&
                    users[1].role == "supervisor"
            })
        }
    }

    @Test
    fun `syncUsers clears but does not insert when API returns empty`() = runTest {
        coEvery { authApi.getUsers() } returns emptyList()
        coEvery { dao.clearUsers() } returns Unit

        repository.syncUsers()

        coVerify(exactly = 1) { dao.clearUsers() }
        coVerify(exactly = 0) { dao.insertUsers(any()) }
    }

    @Test(expected = RuntimeException::class)
    fun `syncUsers throws when API fails`() = runTest {
        coEvery { authApi.getUsers() } throws RuntimeException("Users timeout")
        repository.syncUsers()
    }

    // ═══════════════════════════════════════════════
    // syncMyRooms
    // ═══════════════════════════════════════════════

    @Test
    fun `syncMyRooms fetches and inserts rooms`() = runTest {
        val apiResult = SyncResponse(data = listOf(
            RoomOut(id = 1, name = "Ruang A", isActive = true),
            RoomOut(id = 2, name = "Ruang B", isActive = false),
        ))
        coEvery { authApi.getMyRooms() } returns apiResult
        coEvery { dao.insertRooms(any()) } returns Unit

        repository.syncMyRooms()

        coVerify {
            dao.insertRooms(match { rooms ->
                rooms.size == 2 &&
                    rooms[0].id == 1L &&
                    rooms[0].nama == "Ruang A" &&
                    rooms[0].isActive &&
                    rooms[1].id == 2L &&
                    rooms[1].nama == "Ruang B" &&
                    !rooms[1].isActive
            })
        }
    }

    @Test
    fun `syncMyRooms does not insert when API returns empty`() = runTest {
        coEvery { authApi.getMyRooms() } returns SyncResponse(data = emptyList())

        repository.syncMyRooms()

        coVerify(exactly = 0) { dao.insertRooms(any()) }
    }

    @Test(expected = RuntimeException::class)
    fun `syncMyRooms throws when API fails`() = runTest {
        coEvery { authApi.getMyRooms() } throws RuntimeException("My rooms timeout")
        repository.syncMyRooms()
    }

    // ═══════════════════════════════════════════════
    // syncUserRooms
    // ═══════════════════════════════════════════════

    @Test
    fun `syncUserRooms clears then inserts mapped items`() = runTest {
        val apiResult = SyncResponse(data = listOf(
            UserRoomDto(id = 1, userId = 5, roomId = 10, createdAt = "2026-01-01T00:00:00Z"),
            UserRoomDto(id = 2, userId = 5, roomId = 20),
        ))
        coEvery { authApi.getUserRooms() } returns apiResult
        coEvery { dao.clearUserRooms() } returns Unit
        coEvery { dao.insertUserRooms(any()) } returns Unit

        repository.syncUserRooms()

        coVerify(exactly = 1) { dao.clearUserRooms() }
        coVerify {
            dao.insertUserRooms(match { items ->
                items.size == 2 &&
                    items[0].id == 1L &&
                    items[0].userId == 5 &&
                    items[0].roomId == 10L &&
                    items[0].createdAt == "2026-01-01T00:00:00Z" &&
                    items[1].id == 2L &&
                    items[1].userId == 5 &&
                    items[1].roomId == 20L &&
                    items[1].createdAt == null
            })
        }
    }

    @Test
    fun `syncUserRooms clears but does not insert when API returns empty`() = runTest {
        coEvery { authApi.getUserRooms() } returns SyncResponse(data = emptyList())
        coEvery { dao.clearUserRooms() } returns Unit

        repository.syncUserRooms()

        coVerify(exactly = 1) { dao.clearUserRooms() }
        coVerify(exactly = 0) { dao.insertUserRooms(any()) }
    }

    @Test(expected = RuntimeException::class)
    fun `syncUserRooms throws when API fails`() = runTest {
        coEvery { authApi.getUserRooms() } throws RuntimeException("User rooms timeout")
        repository.syncUserRooms()
    }

    // ═══════════════════════════════════════════════
    // getRoomItemMap
    // ═══════════════════════════════════════════════

    @Test
    fun `getRoomItemMap returns map of roomId to list of itemIds`() = runTest {
        val roomItems = listOf(
            RoomItemEntity(id = 1, roomId = 10, itemId = 100),
            RoomItemEntity(id = 2, roomId = 10, itemId = 200),
            RoomItemEntity(id = 3, roomId = 20, itemId = 300),
        )
        coEvery { dao.getAllRoomItems() } returns roomItems

        val result = repository.getRoomItemMap()

        assertEquals(2, result.size)
        assertEquals(listOf(100L, 200L), result[10L])
        assertEquals(listOf(300L), result[20L])
    }

    @Test
    fun `getRoomItemMap returns empty map when no room items`() = runTest {
        coEvery { dao.getAllRoomItems() } returns emptyList()

        val result = repository.getRoomItemMap()

        assertTrue(result.isEmpty())
    }
}
