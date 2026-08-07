package my.id.kentoes.rsudajibarangapp.core.database

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.execSQL
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafFoto
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafInspeksi
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafItem
import my.id.kentoes.rsudajibarangapp.core.database.entity.InspectionDetailEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.InspectionEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.InspectionPhotoEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.MasterDataItem
import my.id.kentoes.rsudajibarangapp.core.database.entity.RoomItemEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.RuangEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.UserRoomEntity

@Database(
    entities = [
        MasterDataItem::class,
        RuangEntity::class,
        DrafInspeksi::class,
        DrafItem::class,
        DrafFoto::class,
        RoomItemEntity::class,
        UserRoomEntity::class,
        InspectionEntity::class,
        InspectionDetailEntity::class,
        InspectionPhotoEntity::class
    ],
    // v7: tabel `user` (UserEntity) dihapus — GET /api/auth/users admin-only (ADR-0008),
    // inspector selalu 403; lookup nama petugas pakai auth/me (ADR-0017). Migrasi destruktif
    // sesuai pola project (fallbackToDestructiveMigration) — cache di-sync ulang.
    // v8: +room_item.sortOrder — urutan checklist dari kontrak BE ADR-0013 (ADR-0019 Android).
    //     Migrasi NON-destruktif (ALTER TABLE): draf inspeksi & riwayat user SELAMAT saat upgrade.
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun masterDataDao(): MasterDataDao
    abstract fun drafDao(): DrafDao

    companion object {
        /** v7→v8: tambah kolom sortOrder di pivot room_item (ADR-0019).
         *  Pola resmi Room 3.0 KMP: `connection.execSQL(...)` — ekstensi androidx.sqlite
         *  (developer.android.com/training/data-storage/room/migrating-db-versions). */
        internal val MIGRATION_7_8 = Migration(7, 8) { connection ->
            connection.execSQL(
                "ALTER TABLE room_item ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0"
            )
        }

        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "rsud_ajibarang.db"
            )
                .addMigrations(MIGRATION_7_8)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
