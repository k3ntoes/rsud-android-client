package my.id.kentoes.rsudajibarangapp.master

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persistence untuk [SyncState] — menyimpan `synced_at` per endpoint master data.
 *
 * ADR-0012: sync berikutnya mengirim `since=<synced_at dari response sebelumnya>`.
 * Timestamp ini non-sensitif, cukup SharedPreferences (bukan token yang butuh Tink).
 */
@Singleton
class SyncStateStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): SyncState = SyncState(
        roomsSyncedAt = prefs.getString(KEY_ROOMS, null),
        itemsSyncedAt = prefs.getString(KEY_ITEMS, null),
        roomItemsSyncedAt = prefs.getString(KEY_ROOM_ITEMS, null),
        userRoomsSyncedAt = prefs.getString(KEY_USER_ROOMS, null),
        myRoomsSyncedAt = prefs.getString(KEY_MY_ROOMS, null)
    )

    fun save(state: SyncState) {
        prefs.edit()
            .putString(KEY_ROOMS, state.roomsSyncedAt)
            .putString(KEY_ITEMS, state.itemsSyncedAt)
            .putString(KEY_ROOM_ITEMS, state.roomItemsSyncedAt)
            .putString(KEY_USER_ROOMS, state.userRoomsSyncedAt)
            .putString(KEY_MY_ROOMS, state.myRoomsSyncedAt)
            .apply()
    }

    /**
     * Atomic read-modify-write: load → [transform] → save dalam satu langkah.
     * Hindari lost-update jika dua sync berjalan bersamaan (load-copy-save terpisah
     * bisa saling menimpa timestamp endpoint lain).
     */
    fun update(transform: (SyncState) -> SyncState) {
        save(transform(load()))
    }

    /** Hapus semua timestamp sync — dipakai saat logout/ganti akun agar akun berikutnya sync penuh dari epoch. */
    fun clear() {
        prefs.edit()
            .remove(KEY_ROOMS)
            .remove(KEY_ITEMS)
            .remove(KEY_ROOM_ITEMS)
            .remove(KEY_USER_ROOMS)
            .remove(KEY_MY_ROOMS)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "sync_state"
        const val KEY_ROOMS = "rooms_synced_at"
        const val KEY_ITEMS = "items_synced_at"
        const val KEY_ROOM_ITEMS = "room_items_synced_at"
        const val KEY_USER_ROOMS = "user_rooms_synced_at"
        const val KEY_MY_ROOMS = "my_rooms_synced_at"
    }
}
