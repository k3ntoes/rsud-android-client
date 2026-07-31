package my.id.kentoes.rsudajibarangapp.master

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import my.id.kentoes.rsudajibarangapp.auth.api.AuthApi
import my.id.kentoes.rsudajibarangapp.auth.api.UserOut
import my.id.kentoes.rsudajibarangapp.auth.api.UserRoomDto
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.MasterDataItem
import my.id.kentoes.rsudajibarangapp.core.database.entity.RoomItemEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.RuangEntity
import my.id.kentoes.rsudajibarangapp.core.model.PaginatedResponse
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
    private lateinit var syncStateStore: SyncStateStore
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
        syncStateStore = mockk()
        every { syncStateStore.load() } returns SyncState()
        every { syncStateStore.update(any()) } returns Unit
        every { dao.getAllItems() } returns flowOf(sampleItems)
        every { dao.getAllRooms() } returns flowOf(sampleRooms)
        repository = MasterDataRepository(api, authApi, dao, syncStateStore)
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
        coEvery { api.getItems(any()) } returns apiItems
        coEvery { api.getRooms(any()) } returns apiRooms
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
        coEvery { api.getItems(any()) } returns apiItems
        coEvery { api.getRooms(any()) } returns apiRooms
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
                        rooms[0].isActive &&
                        !rooms[0].isMyRoom // room umum dari /rooms tidak ditandai
            })
        }
    }

    // ── Sync from API — partial / empty ──

    @Test
    fun `syncFromApi does not insert items when items list is empty`() = runTest {
        coEvery { api.getItems(any()) } returns SyncResponse(data = emptyList())
        coEvery { api.getRooms(any()) } returns SyncResponse(data = emptyList())
        coEvery { dao.insertItems(any()) } returns Unit
        coEvery { dao.insertRooms(any()) } returns Unit

        val result = repository.syncFromApi()

        assertEquals("Data berhasil diperbarui", result)
        coVerify(exactly = 0) { dao.insertItems(any()) }
        coVerify(exactly = 0) { dao.insertRooms(any()) }
    }

    @Test
    fun `syncFromApi does not insert rooms when rooms list is empty`() = runTest {
        coEvery { api.getItems(any()) } returns SyncResponse(data = listOf(ItemOut(1, "Meja")))
        coEvery { api.getRooms(any()) } returns SyncResponse(data = emptyList())

        coEvery { dao.insertItems(any()) } returns Unit
        coEvery { dao.insertRooms(any()) } returns Unit

        repository.syncFromApi()

        coVerify(exactly = 1) { dao.insertItems(any()) }
        coVerify(exactly = 0) { dao.insertRooms(any()) }
    }

    @Test
    fun `syncFromApi does not insert when both lists are empty`() = runTest {
        coEvery { api.getItems(any()) } returns SyncResponse(data = emptyList())
        coEvery { api.getRooms(any()) } returns SyncResponse(data = emptyList())
        coEvery { dao.insertItems(any()) } returns Unit
        coEvery { dao.insertRooms(any()) } returns Unit

        val result = repository.syncFromApi()

        assertEquals("Data berhasil diperbarui", result)
        coVerify(exactly = 0) { dao.insertItems(any()) }
        coVerify(exactly = 0) { dao.insertRooms(any()) }
    }

    @Test
    fun `syncFromApi inserts rooms even when items list is empty`() = runTest {
        coEvery { api.getItems(any()) } returns SyncResponse(data = emptyList())
        coEvery { api.getRooms(any()) } returns SyncResponse(data = sampleRooms.map {
            RoomOut(
                it.id,
                it.nama
            )
        })
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
        coEvery { api.getItems(any()) } throws RuntimeException("Connection timeout")
        repository.syncFromApi()
    }

    @Test(expected = RuntimeException::class)
    fun `syncFromApi throws when rooms API call fails`() = runTest {
        coEvery { api.getItems(any()) } returns SyncResponse(data = listOf(ItemOut(1, "Meja")))
        coEvery { api.getRooms(any()) } throws RuntimeException("Rooms server error")
        repository.syncFromApi()
    }

    @Test(expected = RuntimeException::class)
    fun `syncFromApi throws when both API calls fail`() = runTest {
        coEvery { api.getItems(any()) } throws RuntimeException("Network down")
        repository.syncFromApi()
    }

    // ═══════════════════════════════════════════════
    // syncRoomItems
    // ═══════════════════════════════════════════════

    @Test
    fun `syncRoomItems clears then inserts mapped items`() = runTest {
        val apiResult = SyncResponse(
            data = listOf(
                RoomItemDto(id = 1, roomId = 10, itemId = 100, createdAt = "2026-01-01T00:00:00Z"),
                RoomItemDto(id = 2, roomId = 10, itemId = 200),
            )
        )
        coEvery { api.getRoomItems(any()) } returns apiResult
        coEvery { dao.clearRoomItems() } returns Unit
        coEvery { dao.insertRoomItems(any()) } returns Unit

        repository.syncRoomItems()

        // Pivot = replace-all — relasi yang dihapus server ikut terhapus lokal
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
    fun `syncRoomItems skips tombstone rows with is_active false`() = runTest {
        // Kontrak §2.2: is_active=false = relasi SUDAH dilepas — TIDAK boleh masuk mapping lokal.
        val apiResult = SyncResponse(
            data = listOf(
                RoomItemDto(id = 1, roomId = 10, itemId = 100, isActive = true),
                RoomItemDto(id = 4, roomId = 10, itemId = 300, isActive = false),
            )
        )
        coEvery { api.getRoomItems(any()) } returns apiResult
        coEvery { dao.clearRoomItems() } returns Unit
        coEvery { dao.insertRoomItems(any()) } returns Unit

        repository.syncRoomItems()

        coVerify(exactly = 1) { dao.clearRoomItems() }
        coVerify {
            dao.insertRoomItems(match { items ->
                items.size == 1 &&
                        items[0].id == 1L &&
                        items[0].itemId == 100L
            })
        }
    }

    @Test
    fun `syncRoomItems inserts nothing when all rows are tombstones`() = runTest {
        // Semua response adalah tombstone → clear tetap jalan (replace-all), insert tidak perlu.
        val apiResult = SyncResponse(
            data = listOf(
                RoomItemDto(id = 4, roomId = 10, itemId = 300, isActive = false),
                RoomItemDto(id = 5, roomId = 20, itemId = 400, isActive = false),
            )
        )
        coEvery { api.getRoomItems(any()) } returns apiResult
        coEvery { dao.clearRoomItems() } returns Unit

        repository.syncRoomItems()

        coVerify(exactly = 1) { dao.clearRoomItems() }
        coVerify(exactly = 0) { dao.insertRoomItems(any()) }
    }

    @Test
    fun `syncRoomItems clears even when API returns empty`() = runTest {
        coEvery { api.getRoomItems(any()) } returns SyncResponse(data = emptyList())
        coEvery { dao.clearRoomItems() } returns Unit

        repository.syncRoomItems()

        // Replace-all tetap clear meski response kosong → semua relasi lokal ikut terhapus
        coVerify(exactly = 1) { dao.clearRoomItems() }
        coVerify(exactly = 0) { dao.insertRoomItems(any()) }
    }

    @Test(expected = RuntimeException::class)
    fun `syncRoomItems throws when API fails`() = runTest {
        coEvery { api.getRoomItems(any()) } throws RuntimeException("Room items timeout")
        repository.syncRoomItems()
    }

    // ═══════════════════════════════════════════════
    // syncUsers
    // ═══════════════════════════════════════════════

    @Test
    fun `syncUsers clears then inserts mapped users`() = runTest {
        val apiUsers = PaginatedResponse(
            items = listOf(
                UserOut(id = 1, username = "petugas01", role = "inspector", isActive = true),
                UserOut(id = 2, username = "supervisor01", role = "supervisor", isActive = true),
            )
        )
        coEvery { authApi.getUsers(any(), any()) } returns apiUsers
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
        coEvery { authApi.getUsers(any(), any()) } returns PaginatedResponse(items = emptyList())
        coEvery { dao.clearUsers() } returns Unit

        repository.syncUsers()

        coVerify(exactly = 1) { dao.clearUsers() }
        coVerify(exactly = 0) { dao.insertUsers(any()) }
    }

    @Test(expected = RuntimeException::class)
    fun `syncUsers throws when API fails`() = runTest {
        coEvery { authApi.getUsers(any(), any()) } throws RuntimeException("Users timeout")
        repository.syncUsers()
    }

    @Test
    fun `syncUsers fetches all pages when paginated`() = runTest {
        // Server returns 30 users split across 2 pages
        val page1 = PaginatedResponse(
            items = (1..20).map { UserOut(it, "user$it", "inspector", true) },
            total = 30, page = 1, perPage = 100, totalPages = 2
        )
        val page2 = PaginatedResponse(
            items = (21..30).map { UserOut(it, "user$it", "inspector", true) },
            total = 30, page = 2, perPage = 100, totalPages = 2
        )
        coEvery { authApi.getUsers(page = 1, perPage = any()) } returns page1
        coEvery { authApi.getUsers(page = 2, perPage = any()) } returns page2
        coEvery { dao.clearUsers() } returns Unit
        coEvery { dao.insertUsers(any()) } returns Unit

        repository.syncUsers()

        // Semua halaman harus di-fetch — bukan hanya page 1
        coVerify(exactly = 1) { authApi.getUsers(page = 1, perPage = any()) }
        coVerify(exactly = 1) { authApi.getUsers(page = 2, perPage = any()) }
        // Semua 30 user masuk dalam SATU insert batch (clear+insert setelah semua halaman)
        coVerify(exactly = 1) {
            dao.insertUsers(match { it.size == 30 })
        }
    }

    // ═══════════════════════════════════════════════
    // syncMyRooms
    // ═══════════════════════════════════════════════

    @Test
    fun `syncMyRooms fetches and inserts rooms`() = runTest {
        val apiResult = SyncResponse(
            data = listOf(
                RoomOut(id = 1, name = "Ruang A", isActive = true),
                RoomOut(id = 2, name = "Ruang B", isActive = false),
            )
        )
        coEvery { authApi.getMyRooms(any()) } returns apiResult
        coEvery { dao.resetMyRooms() } returns Unit
        coEvery { dao.insertRooms(any()) } returns Unit

        repository.syncMyRooms()

        // Pivot = replace-all — reset penanda dulu, lalu tandai hanya room yang di-assign
        coVerify(exactly = 1) { dao.resetMyRooms() }
        coVerify {
            dao.insertRooms(match { rooms ->
                rooms.size == 2 &&
                        rooms[0].id == 1L &&
                        rooms[0].nama == "Ruang A" &&
                        rooms[0].isActive &&
                        rooms[0].isMyRoom &&
                        rooms[1].id == 2L &&
                        rooms[1].nama == "Ruang B" &&
                        !rooms[1].isActive &&
                        rooms[1].isMyRoom
            })
        }
    }

    @Test
    fun `syncMyRooms does not reset flags when API returns empty`() = runTest {
        // HARDENING: response kosong TIDAK boleh menghapus penanda isMyRoom — jika server
        // bermasalah (mis. filter since mengecualikan baris updated_at NULL), reset penanda
        // akan membuat daftar room input inspeksi kosong permanen untuk role non-admin.
        coEvery { authApi.getMyRooms(any()) } returns SyncResponse(data = emptyList())
        coEvery { dao.resetMyRooms() } returns Unit

        repository.syncMyRooms()

        coVerify(exactly = 0) { dao.resetMyRooms() }
        coVerify(exactly = 0) { dao.insertRooms(any()) }
    }

    @Test
    fun `syncMyRooms resets then re-flags only when data present`() = runTest {
        // Saat data ada → reset penanda lama, lalu tandai ulang hanya room yang di-assign
        // (replace-all semantics — assignment yang dicabut admin tidak lagi tampil).
        val apiResult = SyncResponse(
            data = listOf(
                RoomOut(id = 7, name = "Ruang Baru", isActive = true),
            )
        )
        coEvery { authApi.getMyRooms(any()) } returns apiResult
        coEvery { dao.resetMyRooms() } returns Unit
        coEvery { dao.insertRooms(any()) } returns Unit

        repository.syncMyRooms()

        coVerify(exactly = 1) { dao.resetMyRooms() }
        coVerify(exactly = 1) { dao.insertRooms(any()) }
    }

    @Test(expected = RuntimeException::class)
    fun `syncMyRooms throws when API fails`() = runTest {
        coEvery { authApi.getMyRooms(any()) } throws RuntimeException("My rooms timeout")
        repository.syncMyRooms()
    }

    // ═══════════════════════════════════════════════
    // syncUserRooms
    // ═══════════════════════════════════════════════

    @Test
    fun `syncUserRooms clears then inserts mapped items`() = runTest {
        val apiResult = SyncResponse(
            data = listOf(
                UserRoomDto(id = 1, userId = 5, roomId = 10, createdAt = "2026-01-01T00:00:00Z"),
                UserRoomDto(id = 2, userId = 5, roomId = 20),
            )
        )
        coEvery { authApi.getUserRooms(any()) } returns apiResult
        coEvery { dao.clearUserRooms() } returns Unit
        coEvery { dao.insertUserRooms(any()) } returns Unit

        repository.syncUserRooms()

        // Pivot = replace-all — assignment yang dicabut admin ikut terhapus lokal
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
    fun `syncUserRooms skips tombstone rows with is_active false`() = runTest {
        // Kontrak §2.3: is_active=false = assignment dilepas admin — TIDAK boleh masuk daftar lokal.
        val apiResult = SyncResponse(
            data = listOf(
                UserRoomDto(id = 1, userId = 5, roomId = 10, isActive = true),
                UserRoomDto(id = 4, userId = 5, roomId = 20, isActive = false),
            )
        )
        coEvery { authApi.getUserRooms(any()) } returns apiResult
        coEvery { dao.clearUserRooms() } returns Unit
        coEvery { dao.insertUserRooms(any()) } returns Unit

        repository.syncUserRooms()

        coVerify(exactly = 1) { dao.clearUserRooms() }
        coVerify {
            dao.insertUserRooms(match { items ->
                items.size == 1 &&
                        items[0].id == 1L &&
                        items[0].roomId == 10L
            })
        }
    }

    @Test
    fun `syncUserRooms inserts nothing when all rows are tombstones`() = runTest {
        val apiResult = SyncResponse(
            data = listOf(
                UserRoomDto(id = 4, userId = 5, roomId = 20, isActive = false),
            )
        )
        coEvery { authApi.getUserRooms(any()) } returns apiResult
        coEvery { dao.clearUserRooms() } returns Unit

        repository.syncUserRooms()

        coVerify(exactly = 1) { dao.clearUserRooms() }
        coVerify(exactly = 0) { dao.insertUserRooms(any()) }
    }

    @Test
    fun `syncUserRooms clears even when API returns empty`() = runTest {
        coEvery { authApi.getUserRooms(any()) } returns SyncResponse(data = emptyList())
        coEvery { dao.clearUserRooms() } returns Unit

        repository.syncUserRooms()

        // Replace-all tetap clear meski response kosong → semua asosiasi lokal ikut terhapus
        coVerify(exactly = 1) { dao.clearUserRooms() }
        coVerify(exactly = 0) { dao.insertUserRooms(any()) }
    }

    @Test(expected = RuntimeException::class)
    fun `syncUserRooms throws when API fails`() = runTest {
        coEvery { authApi.getUserRooms(any()) } throws RuntimeException("User rooms timeout")
        repository.syncUserRooms()
    }

    // ═══════════════════════════════════════════════
    // Incremental sync (ADR-0012) — since from stored synced_at
    // ═══════════════════════════════════════════════

    @Test
    fun `syncItems falls back to epoch since on first-time sync`() = runTest {
        coEvery { api.getItems(any()) } returns SyncResponse(data = emptyList())

        repository.syncItems()

        coVerify(exactly = 1) { api.getItems(since = "1970-01-01T00:00:00Z") }
    }

    @Test
    fun `syncItems uses stored synced_at as since on next sync`() = runTest {
        every { syncStateStore.load() } returns SyncState(itemsSyncedAt = "2026-07-28T10:00:00Z")
        coEvery { api.getItems(any()) } returns SyncResponse(data = emptyList())

        repository.syncItems()

        coVerify(exactly = 1) { api.getItems(since = "2026-07-28T10:00:00Z") }
    }

    @Test
    fun `syncItems persists response synced_at when data present`() = runTest {
        coEvery { api.getItems(any()) } returns SyncResponse(
            data = listOf(ItemOut(1, "Meja")),
            syncedAt = "2026-07-29T08:30:00Z"
        )
        coEvery { dao.insertItems(any()) } returns Unit

        repository.syncItems()

        val transform = slot<(SyncState) -> SyncState>()
        coVerify(exactly = 1) { syncStateStore.update(capture(transform)) }
        assertEquals("2026-07-29T08:30:00Z", transform.captured(SyncState()).itemsSyncedAt)
    }

    @Test
    fun `syncItems does not advance watermark when data empty`() = runTest {
        // Response kosong TIDAK boleh memajukan watermark — kalau server bermasalah
        // (filter since mengecualikan baris NULL), sync berikutnya harus tetap minta
        // sejak timestamp lama agar data lama tetap ter-download.
        coEvery { api.getItems(any()) } returns SyncResponse(
            data = emptyList(),
            syncedAt = "2026-07-29T08:30:00Z"
        )

        repository.syncItems()

        coVerify(exactly = 0) { syncStateStore.update(any()) }
    }

    @Test
    fun `syncItems explicit since wins over stored synced_at`() = runTest {
        every { syncStateStore.load() } returns SyncState(itemsSyncedAt = "2026-07-28T10:00:00Z")
        coEvery { api.getItems(any()) } returns SyncResponse(data = emptyList())

        repository.syncItems(since = "2026-01-01T00:00:00Z")

        coVerify(exactly = 1) { api.getItems(since = "2026-01-01T00:00:00Z") }
    }

    @Test
    fun `syncRooms persists response synced_at when data present`() = runTest {
        coEvery { api.getRooms(any()) } returns SyncResponse(
            data = listOf(RoomOut(1, "Ruang 1")),
            syncedAt = "2026-07-29T08:30:00Z"
        )
        coEvery { dao.insertRooms(any()) } returns Unit

        repository.syncRooms()

        val transform = slot<(SyncState) -> SyncState>()
        coVerify(exactly = 1) { syncStateStore.update(capture(transform)) }
        assertEquals("2026-07-29T08:30:00Z", transform.captured(SyncState()).roomsSyncedAt)
    }

    @Test
    fun `syncRooms does not advance watermark when data empty`() = runTest {
        // Response kosong TIDAK boleh memajukan watermark — lihat penjelasan di syncItems.
        coEvery { api.getRooms(any()) } returns SyncResponse(
            data = emptyList(),
            syncedAt = "2026-07-29T08:30:00Z"
        )

        repository.syncRooms()

        coVerify(exactly = 0) { syncStateStore.update(any()) }
    }

    @Test
    fun `syncRoomItems always requests full snapshot from epoch`() = runTest {
        // Stored synced_at diabaikan — replace-all butuh snapshot penuh, bukan delta
        every { syncStateStore.load() } returns SyncState(roomItemsSyncedAt = "2026-07-28T10:00:00Z")
        coEvery { api.getRoomItems(any()) } returns SyncResponse(data = emptyList())
        coEvery { dao.clearRoomItems() } returns Unit

        repository.syncRoomItems()

        coVerify(exactly = 1) { api.getRoomItems(since = "1970-01-01T00:00:00Z") }
    }

    @Test
    fun `syncMyRooms always requests full snapshot from epoch`() = runTest {
        // Stored synced_at diabaikan — replace-all butuh snapshot penuh, bukan delta
        every { syncStateStore.load() } returns SyncState(myRoomsSyncedAt = "2026-07-28T10:00:00Z")
        coEvery { authApi.getMyRooms(any()) } returns SyncResponse(data = emptyList())
        coEvery { dao.resetMyRooms() } returns Unit

        repository.syncMyRooms()

        coVerify(exactly = 1) { authApi.getMyRooms(since = "1970-01-01T00:00:00Z") }
    }

    @Test
    fun `syncUserRooms always requests full snapshot from epoch`() = runTest {
        // Stored synced_at diabaikan — replace-all butuh snapshot penuh, bukan delta
        every { syncStateStore.load() } returns SyncState(userRoomsSyncedAt = "2026-07-28T10:00:00Z")
        coEvery { authApi.getUserRooms(any()) } returns SyncResponse(data = emptyList())
        coEvery { dao.clearUserRooms() } returns Unit

        repository.syncUserRooms()

        coVerify(exactly = 1) { authApi.getUserRooms(since = "1970-01-01T00:00:00Z") }
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

    // ═══════════════════════════════════════════════
    // getInspectedRoomIdsForDate  (Phase 4)
    // ═══════════════════════════════════════════════

    @Test
    fun `getInspectedRoomIdsForDate merges draft and inspection IDs`() = runTest {
        coEvery { dao.getDraftRoomIdsForDate("2026-07-30") } returns listOf(1L, 2L)
        coEvery { dao.getInspectedRoomIdsForDate("2026-07-30") } returns listOf(2L, 3L)

        val result = repository.getInspectedRoomIdsForDate("2026-07-30")

        assertEquals(setOf(1L, 2L, 3L), result)
    }

    @Test
    fun `getInspectedRoomIdsForDate returns empty set when no data`() = runTest {
        coEvery { dao.getDraftRoomIdsForDate(any()) } returns emptyList()
        coEvery { dao.getInspectedRoomIdsForDate(any()) } returns emptyList()

        val result = repository.getInspectedRoomIdsForDate("2026-07-29")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getInspectedRoomIdsForDate deduplicates overlapping IDs`() = runTest {
        // Same room has both a draft AND an inspection today
        coEvery { dao.getDraftRoomIdsForDate("2026-07-30") } returns listOf(1L, 2L, 3L)
        coEvery { dao.getInspectedRoomIdsForDate("2026-07-30") } returns listOf(
            1L,
            4L
        ) // 1 is overlap

        val result = repository.getInspectedRoomIdsForDate("2026-07-30")

        assertEquals(setOf(1L, 2L, 3L, 4L), result)
        assertEquals(4, result.size) // no duplicate
    }

    @Test
    fun `getInspectedRoomIdsForDate returns only draft IDs when no inspections`() = runTest {
        coEvery { dao.getDraftRoomIdsForDate("2026-07-30") } returns listOf(5L, 6L)
        coEvery { dao.getInspectedRoomIdsForDate("2026-07-30") } returns emptyList()

        val result = repository.getInspectedRoomIdsForDate("2026-07-30")

        assertEquals(setOf(5L, 6L), result)
    }

    @Test
    fun `getInspectedRoomIdsForDate returns only inspection IDs when no drafts`() = runTest {
        coEvery { dao.getDraftRoomIdsForDate("2026-07-30") } returns emptyList()
        coEvery { dao.getInspectedRoomIdsForDate("2026-07-30") } returns listOf(10L, 20L)

        val result = repository.getInspectedRoomIdsForDate("2026-07-30")

        assertEquals(setOf(10L, 20L), result)
    }

    @Test
    fun `getInspectedRoomIdsForDate passes correct date to DAO`() = runTest {
        coEvery { dao.getDraftRoomIdsForDate(any()) } returns emptyList()
        coEvery { dao.getInspectedRoomIdsForDate(any()) } returns emptyList()

        repository.getInspectedRoomIdsForDate("2026-08-01")

        coVerify { dao.getDraftRoomIdsForDate("2026-08-01") }
        coVerify { dao.getInspectedRoomIdsForDate("2026-08-01") }
    }
}
