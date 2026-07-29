package my.id.kentoes.rsudajibarangapp.core.database

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
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
import my.id.kentoes.rsudajibarangapp.core.database.entity.UserEntity
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
        UserEntity::class,
        InspectionEntity::class,
        InspectionDetailEntity::class,
        InspectionPhotoEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun masterDataDao(): MasterDataDao
    abstract fun drafDao(): DrafDao

    companion object {
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "rsud_ajibarang.db"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
