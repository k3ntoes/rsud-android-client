package my.id.kentoes.rsudajibarangapp.master

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import my.id.kentoes.rsudajibarangapp.auth.api.AuthApi
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.MasterDataItem
import my.id.kentoes.rsudajibarangapp.core.database.entity.RoomItemEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.RuangEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.UserEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.UserRoomEntity
import my.id.kentoes.rsudajibarangapp.master.api.MasterDataApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MasterDataRepository @Inject constructor(
    private val masterDataApi: MasterDataApi,
    private val authApi: AuthApi,
    private val masterDataDao: MasterDataDao,
    private val syncStateStore: SyncStateStore
) {
    /** Observasi items dari cache lokal */
    val items: Flow<List<MasterDataItem>> = masterDataDao.getAllItems()

    /** Observasi rooms dari cache lokal */
    val rooms: Flow<List<RuangEntity>> = masterDataDao.getAllRooms()

    /** Cek apakah item cache lokal sudah terisi */
    suspend fun isCacheAvailable(): Boolean {
        val itemsCount = masterDataDao.getAllItems().first().size
        return itemsCount > 0
    }

    /** Dapatkan set room IDs yang sudah diinspeksi hari ini (draft + inspection) */
    suspend fun getInspectedRoomIdsForDate(date: String): Set<Long> {
        val draftIds = masterDataDao.getDraftRoomIdsForDate(date)
        val inspectionIds = masterDataDao.getInspectedRoomIdsForDate(date)
        return (draftIds + inspectionIds).toSet()
    }

    /** Build mapping roomId → list of itemIds dari cache lokal */
    suspend fun getRoomItemMap(): Map<Long, List<Long>> {
        val items = masterDataDao.getAllRoomItems()
        return items.groupBy({ it.roomId }, { it.itemId })
    }

    /** Fetch items & rooms dari API, simpan ke Room. */
    suspend fun syncFromApi(): String {
        syncItems()
        syncRooms()
        return "Data berhasil diperbarui"
    }

    /** Epoch timestamp untuk first-time sync — server akan return semua data. */
    private val firstSyncSince = "1970-01-01T00:00:00Z"

    /** Resolve `since` untuk sync: param eksplisit > timestamp tersimpan > epoch (first-time). */
    private fun resolveSince(explicit: String?, stored: String?): String =
        explicit ?: stored ?: firstSyncSince

    suspend fun syncItems(since: String? = null) {
        val effectiveSince = resolveSince(since, syncStateStore.load().itemsSyncedAt)
        val response = masterDataApi.getItems(since = effectiveSince)
        val apiItems = response.data
        if (apiItems.isNotEmpty()) {
            val items = apiItems.map { api ->
                MasterDataItem(
                    id = api.id,
                    nama = api.name,
                    kategori = "",
                    deskripsi = null,
                    isActive = api.isActive,
                    updatedAt = api.updatedAt
                )
            }
            masterDataDao.insertItems(items)
            // Watermark HANYA maju saat ada data — response kosong tidak boleh memajukan
            // watermark: jika server bermasalah (mis. filter since mengecualikan baris NULL),
            // sync berikutnya tetap minta sejak timestamp lama → data lama tetap ter-download.
            response.syncedAt?.let { syncedAt ->
                syncStateStore.update { it.copy(itemsSyncedAt = syncedAt) }
            }
        }
    }

    suspend fun syncRooms(since: String? = null) {
        val effectiveSince = resolveSince(since, syncStateStore.load().roomsSyncedAt)
        val response = masterDataApi.getRooms(since = effectiveSince)
        val apiRooms = response.data
        if (apiRooms.isNotEmpty()) {
            val rooms = apiRooms.map { api ->
                RuangEntity(
                    id = api.id,
                    nama = api.name,
                    lantai = null,
                    isActive = api.isActive,
                    // Penanda "room saya" dimiliki syncMyRooms — room umum tidak ditandai.
                    isMyRoom = false,
                    updatedAt = api.updatedAt
                )
            }
            masterDataDao.insertRooms(rooms)
            // Watermark HANYA maju saat ada data — response kosong tidak boleh memajukan
            // watermark: jika server bermasalah (mis. filter since mengecualikan baris NULL),
            // sync berikutnya tetap minta sejak timestamp lama → data lama tetap ter-download.
            response.syncedAt?.let { syncedAt ->
                syncStateStore.update { it.copy(roomsSyncedAt = syncedAt) }
            }
        }
    }

    suspend fun syncRoomItems(since: String? = null) {
        // Pivot = replace-all (sync/CONTEXT.md): endpoint mengembalikan snapshot penuh SEMUA
        // asosiasi. SELALU minta sejak epoch agar snapshot dijamin lengkap — kalau backend
        // kelak delta-filter berdasarkan since, clear+insert justru menghapus semua baris lama.
        val effectiveSince = since ?: firstSyncSince
        val response = masterDataApi.getRoomItems(since = effectiveSince)
        val apiItems = response.data
        // Clear SETELAH fetch sukses agar relasi yang dihapus server ikut terhapus lokal,
        // tanpa risiko kehilangan data jika sync gagal.
        masterDataDao.clearRoomItems()
        // TOMBSTONE (is_active=false, kontrak §2.2): relasi sudah dilepas server — jangan
        // diinsert ke mapping lokal. Berlaku di full sync pertama DAN delta (?since=).
        val activeItems = apiItems.filter { it.isActive }
        if (activeItems.isNotEmpty()) {
            masterDataDao.insertRoomItems(activeItems.map { dto ->
                RoomItemEntity(
                    id = dto.id,
                    roomId = dto.roomId,
                    itemId = dto.itemId,
                    createdAt = dto.createdAt
                )
            })
        }
        // roomItemsSyncedAt tidak dipakai lagi sebagai since (pivot selalu snapshot penuh),
        // tapi tetap disimpan untuk forward-compat jika BE kelak menambah updated_at delta.
        response.syncedAt?.let { syncedAt ->
            syncStateStore.update { it.copy(roomItemsSyncedAt = syncedAt) }
        }
    }

    suspend fun syncMyRooms(since: String? = null) {
        // Pivot = replace-all (sync/CONTEXT.md): endpoint mengembalikan snapshot penuh
        // assignment user. SELALU minta sejak epoch agar snapshot dijamin lengkap — kalau
        // backend kelak delta-filter berdasarkan since, reset flag justru menghapus penanda
        // room yang masih di-assign.
        val response = authApi.getMyRooms(since = since ?: firstSyncSince)
        val apiRooms = response.data
        // HARDENING: reset penanda isMyRoom HANYA saat ada data. Response kosong tidak
        // boleh menghapus penanda — jika server bermasalah (mis. filter since mengecualikan
        // baris updated_at NULL, lihat bug /me/rooms di BE), reset akan membuat daftar room
        // input inspeksi kosong permanen untuk role non-admin. Saat data ada, reset lalu
        // tandai ulang (replace-all semantics: assignment yang dicabut admin tidak tampil).
        if (apiRooms.isNotEmpty()) {
            masterDataDao.resetMyRooms()
            val rooms = apiRooms.map { api ->
                RuangEntity(
                    id = api.id,
                    nama = api.name,
                    lantai = null,
                    isActive = api.isActive,
                    isMyRoom = true,
                    updatedAt = api.updatedAt
                )
            }
            masterDataDao.insertRooms(rooms)
        }
        // myRoomsSyncedAt tidak dipakai lagi sebagai since (pivot selalu snapshot penuh),
        // tapi tetap disimpan untuk forward-compat jika BE kelak menambah updated_at delta.
        response.syncedAt?.let { syncedAt ->
            syncStateStore.update { it.copy(myRoomsSyncedAt = syncedAt) }
        }
    }

    /** Ukuran halaman saat sync users — batas atas per_page endpoint auth/users = 100. */
    private val usersPerPage = 100

    suspend fun syncUsers() {
        // Loop semua halaman (server-driven totalPages) — jangan hanya page 1
        val allUsers = mutableListOf<UserEntity>()
        var page = 1
        var totalPages = 1
        do {
            val response = authApi.getUsers(page = page, perPage = usersPerPage)
            allUsers += response.items.map { user ->
                UserEntity(
                    id = user.id,
                    username = user.username,
                    role = user.role,
                    isActive = user.isActive
                )
            }
            totalPages = response.totalPages
            page++
        } while (page <= totalPages)

        // Clear + insert sekali setelah semua halaman terkumpul (hindari partial wipe)
        masterDataDao.clearUsers()
        if (allUsers.isNotEmpty()) {
            masterDataDao.insertUsers(allUsers)
        }
    }

    suspend fun syncUserRooms(since: String? = null) {
        // Pivot = replace-all (sync/CONTEXT.md): endpoint mengembalikan snapshot penuh SEMUA
        // asosiasi. SELALU minta sejak epoch agar snapshot dijamin lengkap — kalau backend
        // kelak delta-filter berdasarkan since, clear+insert justru menghapus semua baris lama.
        val effectiveSince = since ?: firstSyncSince
        val response = authApi.getUserRooms(since = effectiveSince)
        val apiItems = response.data
        // Clear SETELAH fetch sukses agar assignment yang dicabut admin ikut terhapus lokal,
        // tanpa risiko kehilangan data jika sync gagal.
        masterDataDao.clearUserRooms()
        // TOMBSTONE (is_active=false, kontrak §2.3): assignment sudah dilepas admin — jangan
        // diinsert ke daftar lokal. Berlaku di full sync pertama DAN delta (?since=).
        val activeItems = apiItems.filter { it.isActive }
        if (activeItems.isNotEmpty()) {
            masterDataDao.insertUserRooms(activeItems.map { dto ->
                UserRoomEntity(
                    id = dto.id,
                    userId = dto.userId,
                    roomId = dto.roomId,
                    createdAt = dto.createdAt
                )
            })
        }
        // userRoomsSyncedAt tidak dipakai lagi sebagai since (pivot selalu snapshot penuh),
        // tapi tetap disimpan untuk forward-compat jika BE kelak menambah updated_at delta.
        response.syncedAt?.let { syncedAt ->
            syncStateStore.update { it.copy(userRoomsSyncedAt = syncedAt) }
        }
    }
}
