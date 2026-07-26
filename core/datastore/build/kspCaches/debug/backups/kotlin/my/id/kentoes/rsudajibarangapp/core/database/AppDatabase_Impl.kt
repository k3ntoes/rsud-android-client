package my.id.kentoes.rsudajibarangapp.core.database

import androidx.room3.InvalidationTracker
import androidx.room3.RoomOpenDelegate
import androidx.room3.migration.AutoMigrationSpec
import androidx.room3.migration.Migration
import androidx.room3.util.TableInfo
import androidx.room3.util.TableInfo.Companion.read
import androidx.room3.util.dropFtsSyncTriggers
import androidx.room3.util.performClear
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao_Impl
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao_Impl

@Generated(value = ["androidx.room3.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL", "MemberExtensionConflict"])
internal class AppDatabase_Impl : AppDatabase() {
  private val _masterDataDao: Lazy<MasterDataDao> = lazy {
    MasterDataDao_Impl(this)
  }

  private val _drafDao: Lazy<DrafDao> = lazy {
    DrafDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1, "240509997b537055ef2e81b15ab5be00", "d0d053ca227d865dba85163606940c6f") {
      public override suspend fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `master_data_item` (`id` INTEGER NOT NULL, `nama` TEXT NOT NULL, `kategori` TEXT NOT NULL, `deskripsi` TEXT, `isActive` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `ruang` (`id` INTEGER NOT NULL, `nama` TEXT NOT NULL, `lantai` TEXT, `isActive` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `draf_inspeksi` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `roomId` INTEGER NOT NULL, `localTimestamp` TEXT NOT NULL, `inspectorId` TEXT, `status` TEXT NOT NULL, `catatan` TEXT, `createdAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `draf_item` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `drafId` INTEGER NOT NULL, `itemId` INTEGER NOT NULL, `skor` INTEGER NOT NULL, `catatan` TEXT, FOREIGN KEY(`drafId`) REFERENCES `draf_inspeksi`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_draf_item_drafId` ON `draf_item` (`drafId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `draf_foto` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `drafItemId` INTEGER NOT NULL, `pathLokal` TEXT NOT NULL, FOREIGN KEY(`drafItemId`) REFERENCES `draf_item`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_draf_foto_drafItemId` ON `draf_foto` (`drafItemId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '240509997b537055ef2e81b15ab5be00')")
      }

      public override suspend fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `master_data_item`")
        connection.execSQL("DROP TABLE IF EXISTS `ruang`")
        connection.execSQL("DROP TABLE IF EXISTS `draf_inspeksi`")
        connection.execSQL("DROP TABLE IF EXISTS `draf_item`")
        connection.execSQL("DROP TABLE IF EXISTS `draf_foto`")
      }

      public override suspend fun onCreate(connection: SQLiteConnection) {
      }

      public override suspend fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override suspend fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override suspend fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override suspend fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsMasterDataItem: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsMasterDataItem.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMasterDataItem.put("nama", TableInfo.Column("nama", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMasterDataItem.put("kategori", TableInfo.Column("kategori", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMasterDataItem.put("deskripsi", TableInfo.Column("deskripsi", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMasterDataItem.put("isActive", TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysMasterDataItem: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesMasterDataItem: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoMasterDataItem: TableInfo = TableInfo("master_data_item", _columnsMasterDataItem, _foreignKeysMasterDataItem, _indicesMasterDataItem)
        val _existingMasterDataItem: TableInfo = read(connection, "master_data_item")
        if (!_infoMasterDataItem.equals(_existingMasterDataItem)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |master_data_item(my.id.kentoes.rsudajibarangapp.core.database.entity.MasterDataItem).
              | Expected:
              |""".trimMargin() + _infoMasterDataItem + """
              |
              | Found:
              |""".trimMargin() + _existingMasterDataItem)
        }
        val _columnsRuang: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsRuang.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRuang.put("nama", TableInfo.Column("nama", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRuang.put("lantai", TableInfo.Column("lantai", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRuang.put("isActive", TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysRuang: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesRuang: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoRuang: TableInfo = TableInfo("ruang", _columnsRuang, _foreignKeysRuang, _indicesRuang)
        val _existingRuang: TableInfo = read(connection, "ruang")
        if (!_infoRuang.equals(_existingRuang)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |ruang(my.id.kentoes.rsudajibarangapp.core.database.entity.RuangEntity).
              | Expected:
              |""".trimMargin() + _infoRuang + """
              |
              | Found:
              |""".trimMargin() + _existingRuang)
        }
        val _columnsDrafInspeksi: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDrafInspeksi.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDrafInspeksi.put("roomId", TableInfo.Column("roomId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDrafInspeksi.put("localTimestamp", TableInfo.Column("localTimestamp", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDrafInspeksi.put("inspectorId", TableInfo.Column("inspectorId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDrafInspeksi.put("status", TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDrafInspeksi.put("catatan", TableInfo.Column("catatan", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDrafInspeksi.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDrafInspeksi: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesDrafInspeksi: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoDrafInspeksi: TableInfo = TableInfo("draf_inspeksi", _columnsDrafInspeksi, _foreignKeysDrafInspeksi, _indicesDrafInspeksi)
        val _existingDrafInspeksi: TableInfo = read(connection, "draf_inspeksi")
        if (!_infoDrafInspeksi.equals(_existingDrafInspeksi)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |draf_inspeksi(my.id.kentoes.rsudajibarangapp.core.database.entity.DrafInspeksi).
              | Expected:
              |""".trimMargin() + _infoDrafInspeksi + """
              |
              | Found:
              |""".trimMargin() + _existingDrafInspeksi)
        }
        val _columnsDrafItem: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDrafItem.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDrafItem.put("drafId", TableInfo.Column("drafId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDrafItem.put("itemId", TableInfo.Column("itemId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDrafItem.put("skor", TableInfo.Column("skor", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDrafItem.put("catatan", TableInfo.Column("catatan", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDrafItem: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysDrafItem.add(TableInfo.ForeignKey("draf_inspeksi", "CASCADE", "NO ACTION", listOf("drafId"), listOf("id")))
        val _indicesDrafItem: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesDrafItem.add(TableInfo.Index("index_draf_item_drafId", false, listOf("drafId"), listOf("ASC")))
        val _infoDrafItem: TableInfo = TableInfo("draf_item", _columnsDrafItem, _foreignKeysDrafItem, _indicesDrafItem)
        val _existingDrafItem: TableInfo = read(connection, "draf_item")
        if (!_infoDrafItem.equals(_existingDrafItem)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |draf_item(my.id.kentoes.rsudajibarangapp.core.database.entity.DrafItem).
              | Expected:
              |""".trimMargin() + _infoDrafItem + """
              |
              | Found:
              |""".trimMargin() + _existingDrafItem)
        }
        val _columnsDrafFoto: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDrafFoto.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDrafFoto.put("drafItemId", TableInfo.Column("drafItemId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDrafFoto.put("pathLokal", TableInfo.Column("pathLokal", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDrafFoto: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysDrafFoto.add(TableInfo.ForeignKey("draf_item", "CASCADE", "NO ACTION", listOf("drafItemId"), listOf("id")))
        val _indicesDrafFoto: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesDrafFoto.add(TableInfo.Index("index_draf_foto_drafItemId", false, listOf("drafItemId"), listOf("ASC")))
        val _infoDrafFoto: TableInfo = TableInfo("draf_foto", _columnsDrafFoto, _foreignKeysDrafFoto, _indicesDrafFoto)
        val _existingDrafFoto: TableInfo = read(connection, "draf_foto")
        if (!_infoDrafFoto.equals(_existingDrafFoto)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |draf_foto(my.id.kentoes.rsudajibarangapp.core.database.entity.DrafFoto).
              | Expected:
              |""".trimMargin() + _infoDrafFoto + """
              |
              | Found:
              |""".trimMargin() + _existingDrafFoto)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "master_data_item", "ruang", "draf_inspeksi", "draf_item", "draf_foto")
  }

  public override suspend fun clearAllTables() {
    performClear(this, true, "master_data_item", "ruang", "draf_inspeksi", "draf_item", "draf_foto")
  }

  protected override fun getRequiredColumnTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _columnTypeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _columnTypeConvertersMap.put(MasterDataDao::class, MasterDataDao_Impl.getRequiredColumnConverters())
    _columnTypeConvertersMap.put(DrafDao::class, DrafDao_Impl.getRequiredColumnConverters())
    return _columnTypeConvertersMap
  }

  protected override fun getRequiredDaoReturnTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _daoReturnTypeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _daoReturnTypeConvertersMap.put(MasterDataDao::class, MasterDataDao_Impl.getRequiredDaoReturnTypeConverters())
    _daoReturnTypeConvertersMap.put(DrafDao::class, DrafDao_Impl.getRequiredDaoReturnTypeConverters())
    return _daoReturnTypeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun masterDataDao(): MasterDataDao = _masterDataDao.value

  public override fun drafDao(): DrafDao = _drafDao.value
}
