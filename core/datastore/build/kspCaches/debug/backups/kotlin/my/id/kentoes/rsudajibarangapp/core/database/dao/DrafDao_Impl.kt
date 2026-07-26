package my.id.kentoes.rsudajibarangapp.core.database.dao

import androidx.room3.EntityDeleteOrUpdateAdapter
import androidx.room3.EntityInsertAdapter
import androidx.room3.RoomDatabase
import androidx.room3.coroutines.createFlow
import androidx.room3.util.getColumnIndexOrThrow
import androidx.room3.util.performInTransactionSuspending
import androidx.room3.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
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
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafFoto
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafInspeksi
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafItem

@Generated(value = ["androidx.room3.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL", "MemberExtensionConflict"])
internal class DrafDao_Impl(
  __db: RoomDatabase,
) : DrafDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfDrafInspeksi: EntityInsertAdapter<DrafInspeksi>

  private val __insertAdapterOfDrafItem: EntityInsertAdapter<DrafItem>

  private val __insertAdapterOfDrafFoto: EntityInsertAdapter<DrafFoto>

  private val __deleteAdapterOfDrafInspeksi: EntityDeleteOrUpdateAdapter<DrafInspeksi>

  private val __deleteAdapterOfDrafFoto: EntityDeleteOrUpdateAdapter<DrafFoto>
  init {
    this.__db = __db
    this.__insertAdapterOfDrafInspeksi = object : EntityInsertAdapter<DrafInspeksi>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `draf_inspeksi` (`id`,`roomId`,`localTimestamp`,`inspectorId`,`status`,`catatan`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DrafInspeksi) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.roomId)
        statement.bindText(3, entity.localTimestamp)
        val _tmpInspectorId: String? = entity.inspectorId
        if (_tmpInspectorId == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpInspectorId)
        }
        statement.bindText(5, entity.status)
        val _tmpCatatan: String? = entity.catatan
        if (_tmpCatatan == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpCatatan)
        }
        statement.bindLong(7, entity.createdAt)
      }
    }
    this.__insertAdapterOfDrafItem = object : EntityInsertAdapter<DrafItem>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `draf_item` (`id`,`drafId`,`itemId`,`skor`,`catatan`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DrafItem) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.drafId)
        statement.bindLong(3, entity.itemId)
        statement.bindLong(4, entity.skor.toLong())
        val _tmpCatatan: String? = entity.catatan
        if (_tmpCatatan == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpCatatan)
        }
      }
    }
    this.__insertAdapterOfDrafFoto = object : EntityInsertAdapter<DrafFoto>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `draf_foto` (`id`,`drafItemId`,`pathLokal`) VALUES (nullif(?, 0),?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DrafFoto) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.drafItemId)
        statement.bindText(3, entity.pathLokal)
      }
    }
    this.__deleteAdapterOfDrafInspeksi = object : EntityDeleteOrUpdateAdapter<DrafInspeksi>() {
      protected override fun createQuery(): String = "DELETE FROM `draf_inspeksi` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: DrafInspeksi) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__deleteAdapterOfDrafFoto = object : EntityDeleteOrUpdateAdapter<DrafFoto>() {
      protected override fun createQuery(): String = "DELETE FROM `draf_foto` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: DrafFoto) {
        statement.bindLong(1, entity.id)
      }
    }
  }

  public override suspend fun insertDraft(draft: DrafInspeksi): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfDrafInspeksi.insertAndReturnId(_connection, draft)
    _result
  }

  public override suspend fun insertItem(item: DrafItem): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfDrafItem.insertAndReturnId(_connection, item)
    _result
  }

  public override suspend fun insertPhoto(photo: DrafFoto): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfDrafFoto.insertAndReturnId(_connection, photo)
    _result
  }

  public override suspend fun deleteDraft(draft: DrafInspeksi): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfDrafInspeksi.handle(_connection, draft)
  }

  public override suspend fun deletePhoto(photo: DrafFoto): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfDrafFoto.handle(_connection, photo)
  }

  public override suspend fun deleteDraftCascade(draft: DrafInspeksi): Unit = performInTransactionSuspending(__db) {
    super@DrafDao_Impl.deleteDraftCascade(draft)
  }

  public override fun getAllDrafts(): Flow<List<DrafInspeksi>> {
    val _sql: String = "SELECT * FROM draf_inspeksi ORDER BY createdAt DESC"
    return createFlow(__db, false, arrayOf("draf_inspeksi")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRoomId: Int = getColumnIndexOrThrow(_stmt, "roomId")
        val _columnIndexOfLocalTimestamp: Int = getColumnIndexOrThrow(_stmt, "localTimestamp")
        val _columnIndexOfInspectorId: Int = getColumnIndexOrThrow(_stmt, "inspectorId")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfCatatan: Int = getColumnIndexOrThrow(_stmt, "catatan")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<DrafInspeksi> = mutableListOf()
        while (_stmt.step()) {
          val _item: DrafInspeksi
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRoomId: Long
          _tmpRoomId = _stmt.getLong(_columnIndexOfRoomId)
          val _tmpLocalTimestamp: String
          _tmpLocalTimestamp = _stmt.getText(_columnIndexOfLocalTimestamp)
          val _tmpInspectorId: String?
          if (_stmt.isNull(_columnIndexOfInspectorId)) {
            _tmpInspectorId = null
          } else {
            _tmpInspectorId = _stmt.getText(_columnIndexOfInspectorId)
          }
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCatatan: String?
          if (_stmt.isNull(_columnIndexOfCatatan)) {
            _tmpCatatan = null
          } else {
            _tmpCatatan = _stmt.getText(_columnIndexOfCatatan)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = DrafInspeksi(_tmpId,_tmpRoomId,_tmpLocalTimestamp,_tmpInspectorId,_tmpStatus,_tmpCatatan,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getDraftsByStatus(status: String): Flow<List<DrafInspeksi>> {
    val _sql: String = "SELECT * FROM draf_inspeksi WHERE status = ? ORDER BY createdAt DESC"
    return createFlow(__db, false, arrayOf("draf_inspeksi")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, status)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRoomId: Int = getColumnIndexOrThrow(_stmt, "roomId")
        val _columnIndexOfLocalTimestamp: Int = getColumnIndexOrThrow(_stmt, "localTimestamp")
        val _columnIndexOfInspectorId: Int = getColumnIndexOrThrow(_stmt, "inspectorId")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfCatatan: Int = getColumnIndexOrThrow(_stmt, "catatan")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<DrafInspeksi> = mutableListOf()
        while (_stmt.step()) {
          val _item: DrafInspeksi
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRoomId: Long
          _tmpRoomId = _stmt.getLong(_columnIndexOfRoomId)
          val _tmpLocalTimestamp: String
          _tmpLocalTimestamp = _stmt.getText(_columnIndexOfLocalTimestamp)
          val _tmpInspectorId: String?
          if (_stmt.isNull(_columnIndexOfInspectorId)) {
            _tmpInspectorId = null
          } else {
            _tmpInspectorId = _stmt.getText(_columnIndexOfInspectorId)
          }
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCatatan: String?
          if (_stmt.isNull(_columnIndexOfCatatan)) {
            _tmpCatatan = null
          } else {
            _tmpCatatan = _stmt.getText(_columnIndexOfCatatan)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = DrafInspeksi(_tmpId,_tmpRoomId,_tmpLocalTimestamp,_tmpInspectorId,_tmpStatus,_tmpCatatan,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getDraftById(id: Long): DrafInspeksi? {
    val _sql: String = "SELECT * FROM draf_inspeksi WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRoomId: Int = getColumnIndexOrThrow(_stmt, "roomId")
        val _columnIndexOfLocalTimestamp: Int = getColumnIndexOrThrow(_stmt, "localTimestamp")
        val _columnIndexOfInspectorId: Int = getColumnIndexOrThrow(_stmt, "inspectorId")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfCatatan: Int = getColumnIndexOrThrow(_stmt, "catatan")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: DrafInspeksi?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRoomId: Long
          _tmpRoomId = _stmt.getLong(_columnIndexOfRoomId)
          val _tmpLocalTimestamp: String
          _tmpLocalTimestamp = _stmt.getText(_columnIndexOfLocalTimestamp)
          val _tmpInspectorId: String?
          if (_stmt.isNull(_columnIndexOfInspectorId)) {
            _tmpInspectorId = null
          } else {
            _tmpInspectorId = _stmt.getText(_columnIndexOfInspectorId)
          }
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCatatan: String?
          if (_stmt.isNull(_columnIndexOfCatatan)) {
            _tmpCatatan = null
          } else {
            _tmpCatatan = _stmt.getText(_columnIndexOfCatatan)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result = DrafInspeksi(_tmpId,_tmpRoomId,_tmpLocalTimestamp,_tmpInspectorId,_tmpStatus,_tmpCatatan,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getItemsForDraft(drafId: Long): List<DrafItem> {
    val _sql: String = "SELECT * FROM draf_item WHERE drafId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, drafId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDrafId: Int = getColumnIndexOrThrow(_stmt, "drafId")
        val _columnIndexOfItemId: Int = getColumnIndexOrThrow(_stmt, "itemId")
        val _columnIndexOfSkor: Int = getColumnIndexOrThrow(_stmt, "skor")
        val _columnIndexOfCatatan: Int = getColumnIndexOrThrow(_stmt, "catatan")
        val _result: MutableList<DrafItem> = mutableListOf()
        while (_stmt.step()) {
          val _item: DrafItem
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDrafId: Long
          _tmpDrafId = _stmt.getLong(_columnIndexOfDrafId)
          val _tmpItemId: Long
          _tmpItemId = _stmt.getLong(_columnIndexOfItemId)
          val _tmpSkor: Int
          _tmpSkor = _stmt.getLong(_columnIndexOfSkor).toInt()
          val _tmpCatatan: String?
          if (_stmt.isNull(_columnIndexOfCatatan)) {
            _tmpCatatan = null
          } else {
            _tmpCatatan = _stmt.getText(_columnIndexOfCatatan)
          }
          _item = DrafItem(_tmpId,_tmpDrafId,_tmpItemId,_tmpSkor,_tmpCatatan)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getPhotosForItem(drafItemId: Long): List<DrafFoto> {
    val _sql: String = "SELECT * FROM draf_foto WHERE drafItemId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, drafItemId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDrafItemId: Int = getColumnIndexOrThrow(_stmt, "drafItemId")
        val _columnIndexOfPathLokal: Int = getColumnIndexOrThrow(_stmt, "pathLokal")
        val _result: MutableList<DrafFoto> = mutableListOf()
        while (_stmt.step()) {
          val _item: DrafFoto
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDrafItemId: Long
          _tmpDrafItemId = _stmt.getLong(_columnIndexOfDrafItemId)
          val _tmpPathLokal: String
          _tmpPathLokal = _stmt.getText(_columnIndexOfPathLokal)
          _item = DrafFoto(_tmpId,_tmpDrafItemId,_tmpPathLokal)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateDraftStatus(id: Long, status: String) {
    val _sql: String = "UPDATE draf_inspeksi SET status = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, status)
        _argIndex = 2
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
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
