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
