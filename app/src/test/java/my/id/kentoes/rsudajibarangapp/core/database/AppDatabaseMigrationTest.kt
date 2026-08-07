package my.id.kentoes.rsudajibarangapp.core.database

import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Entity
import androidx.room3.Insert
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.Room
import androidx.room3.RoomDatabase
import kotlinx.coroutines.test.runTest
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafFoto
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafInspeksi
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafItem
import my.id.kentoes.rsudajibarangapp.core.database.entity.InspectionDetailEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.InspectionEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.InspectionPhotoEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.MasterDataItem
import my.id.kentoes.rsudajibarangapp.core.database.entity.RuangEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.UserRoomEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Schema room_item LAMA (v7) — belum ada kolom sortOrder. */
@Entity(tableName = "room_item")
internal data class RoomItemEntityV7(
    @PrimaryKey val id: Long,
    val roomId: Long,
    val itemId: Long,
    val createdAt: String? = null
)

@Dao
internal interface RoomItemV7Dao {
    @Insert
    suspend fun insert(item: RoomItemEntityV7)

    @Query("SELECT * FROM room_item")
    suspend fun all(): List<RoomItemEntityV7>
}

/** Database v7 dengan schema yang sama persis dengan AppDatabase v8 MINUS sortOrder di room_item. */
@Database(
    entities = [
        MasterDataItem::class,
        RuangEntity::class,
        DrafInspeksi::class,
        DrafItem::class,
        DrafFoto::class,
        RoomItemEntityV7::class,
        UserRoomEntity::class,
        InspectionEntity::class,
        InspectionDetailEntity::class,
        InspectionPhotoEntity::class
    ],
    version = 7,
    exportSchema = false
)
internal abstract class AppDatabaseV7 : RoomDatabase() {
    abstract fun roomItemV7Dao(): RoomItemV7Dao
}

/**
 * Verifikasi migrasi Room 7→8 (ADR-0019) — migrasi NON-destruktif pertama di project.
 *
 * Memakai pola yang sama seperti DrafDaoTest (Robolectric + Room file-based): buat DB v7
 * dengan schema lama, isi data sentinel, lalu buka dengan AppDatabase v8 → MIGRATION_7_8
 * (ALTER TABLE room_item ADD COLUMN sortOrder) harus dijalankan dan data lama SELAMAT.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppDatabaseMigrationTest {

    @Test
    fun `v7 to v8 migration preserves room_item data and adds sortOrder default 0`() = runTest {
        val context = RuntimeEnvironment.getApplication()
        val dbName = "migration_test_${System.nanoTime()}.db"

        // 1. Buat DB v7 (schema LAMA — room_item TANPA sortOrder) + data sentinel
        val v7 = Room.databaseBuilder(context, AppDatabaseV7::class.java, dbName)
            .allowMainThreadQueries()
            .build()
        v7.roomItemV7Dao().insert(
            RoomItemEntityV7(id = 1, roomId = 10, itemId = 100, createdAt = "2026-01-01T00:00:00Z")
        )
        v7.close()

        // 2. Buka dengan AppDatabase v8 → MIGRATION_7_8 (ALTER TABLE) dijalankan
        val db = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(AppDatabase.MIGRATION_7_8)
            .allowMainThreadQueries()
            .build()

        // Data lama SELAMAT + kolom sortOrder terisi default 0 dari ALTER TABLE
        val items = db.masterDataDao().getAllRoomItems()
        assertEquals(1, items.size)
        assertEquals(10L, items[0].roomId)
        assertEquals(100L, items[0].itemId)
        assertEquals("2026-01-01T00:00:00Z", items[0].createdAt)
        assertEquals(0, items[0].sortOrder)

        db.close()
    }

    @Test
    fun `fresh install at version 8 has empty room_item with sortOrder column`() = runTest {
        val context = RuntimeEnvironment.getApplication()
        val db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        assertEquals(0, db.masterDataDao().getAllRoomItems().size)
        db.close()
    }
}
