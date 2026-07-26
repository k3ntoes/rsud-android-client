package my.id.kentoes.rsudajibarangapp.core.database.dao

import androidx.room3.EntityInsertAdapter
import androidx.room3.RoomDatabase
import androidx.room3.coroutines.createFlow
import androidx.room3.util.getColumnIndexOrThrow
import androidx.room3.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow
import my.id.kentoes.rsudajibarangapp.core.database.entity.MasterDataItem
import my.id.kentoes.rsudajibarangapp.core.database.entity.RuangEntity

@Generated(value = ["androidx.room3.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL", "MemberExtensionConflict"])
internal class MasterDataDao_Impl(
  __db: RoomDatabase,
) : MasterDataDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfMasterDataItem: EntityInsertAdapter<MasterDataItem>

  private val __insertAdapterOfRuangEntity: EntityInsertAdapter<RuangEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfMasterDataItem = object : EntityInsertAdapter<MasterDataItem>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `master_data_item` (`id`,`nama`,`kategori`,`deskripsi`,`isActive`) VALUES (?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: MasterDataItem) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.nama)
        statement.bindText(3, entity.kategori)
        val _tmpDeskripsi: String? = entity.deskripsi
        if (_tmpDeskripsi == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpDeskripsi)
        }
        val _tmp: Int = if (entity.isActive) 1 else 0
        statement.bindLong(5, _tmp.toLong())
      }
    }
    this.__insertAdapterOfRuangEntity = object : EntityInsertAdapter<RuangEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `ruang` (`id`,`nama`,`lantai`,`isActive`) VALUES (?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: RuangEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.nama)
        val _tmpLantai: String? = entity.lantai
        if (_tmpLantai == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpLantai)
        }
        val _tmp: Int = if (entity.isActive) 1 else 0
        statement.bindLong(4, _tmp.toLong())
      }
    }
  }

  public override suspend fun insertItems(items: List<MasterDataItem>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfMasterDataItem.insert(_connection, items)
  }

  public override suspend fun insertRooms(rooms: List<RuangEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfRuangEntity.insert(_connection, rooms)
  }

  public override fun getAllItems(): Flow<List<MasterDataItem>> {
    val _sql: String = "SELECT * FROM master_data_item WHERE isActive = 1 ORDER BY kategori, nama"
    return createFlow(__db, false, arrayOf("master_data_item")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfNama: Int = getColumnIndexOrThrow(_stmt, "nama")
        val _columnIndexOfKategori: Int = getColumnIndexOrThrow(_stmt, "kategori")
        val _columnIndexOfDeskripsi: Int = getColumnIndexOrThrow(_stmt, "deskripsi")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _result: MutableList<MasterDataItem> = mutableListOf()
        while (_stmt.step()) {
          val _item: MasterDataItem
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpNama: String
          _tmpNama = _stmt.getText(_columnIndexOfNama)
          val _tmpKategori: String
          _tmpKategori = _stmt.getText(_columnIndexOfKategori)
          val _tmpDeskripsi: String?
          if (_stmt.isNull(_columnIndexOfDeskripsi)) {
            _tmpDeskripsi = null
          } else {
            _tmpDeskripsi = _stmt.getText(_columnIndexOfDeskripsi)
          }
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          _item = MasterDataItem(_tmpId,_tmpNama,_tmpKategori,_tmpDeskripsi,_tmpIsActive)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllRooms(): Flow<List<RuangEntity>> {
    val _sql: String = "SELECT * FROM ruang WHERE isActive = 1 ORDER BY nama"
    return createFlow(__db, false, arrayOf("ruang")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfNama: Int = getColumnIndexOrThrow(_stmt, "nama")
        val _columnIndexOfLantai: Int = getColumnIndexOrThrow(_stmt, "lantai")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _result: MutableList<RuangEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: RuangEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpNama: String
          _tmpNama = _stmt.getText(_columnIndexOfNama)
          val _tmpLantai: String?
          if (_stmt.isNull(_columnIndexOfLantai)) {
            _tmpLantai = null
          } else {
            _tmpLantai = _stmt.getText(_columnIndexOfLantai)
          }
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          _item = RuangEntity(_tmpId,_tmpNama,_tmpLantai,_tmpIsActive)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredColumnConverters(): List<KClass<*>> = emptyList()

    public fun getRequiredDaoReturnTypeConverters(): List<KClass<*>> = emptyList()
  }
}
