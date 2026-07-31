package my.id.kentoes.rsudajibarangapp.master

/**
 * Tracks sync timestamps per endpoint for incremental sync.
 */
data class SyncState(
    val roomsSyncedAt: String? = null,
    val itemsSyncedAt: String? = null,
    val roomItemsSyncedAt: String? = null,
    val userRoomsSyncedAt: String? = null,
    val myRoomsSyncedAt: String? = null
)

/** Waktu sync terlama yang pernah tercatat di semua endpoint — indikator "Terakhir sync". */
fun SyncState.latestSyncTime(): String? =
    listOfNotNull(
        roomsSyncedAt, itemsSyncedAt, roomItemsSyncedAt,
        userRoomsSyncedAt, myRoomsSyncedAt
    ).maxOrNull()
