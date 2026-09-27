package com.salarybox.app.`data`.local.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.salarybox.app.`data`.local.entity.StaffEntity
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

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class StaffDao_Impl(
  __db: RoomDatabase,
) : StaffDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfStaffEntity: EntityInsertAdapter<StaffEntity>

  private val __insertAdapterOfStaffEntity_1: EntityInsertAdapter<StaffEntity>

  private val __deleteAdapterOfStaffEntity: EntityDeleteOrUpdateAdapter<StaffEntity>

  private val __updateAdapterOfStaffEntity: EntityDeleteOrUpdateAdapter<StaffEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfStaffEntity = object : EntityInsertAdapter<StaffEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `staff` (`id`,`name`,`employeeId`,`faceEmbedding`,`enrolledAt`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: StaffEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.employeeId)
        val _tmpFaceEmbedding: String? = entity.faceEmbedding
        if (_tmpFaceEmbedding == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpFaceEmbedding)
        }
        val _tmpEnrolledAt: Long? = entity.enrolledAt
        if (_tmpEnrolledAt == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpEnrolledAt)
        }
      }
    }
    this.__insertAdapterOfStaffEntity_1 = object : EntityInsertAdapter<StaffEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `staff` (`id`,`name`,`employeeId`,`faceEmbedding`,`enrolledAt`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: StaffEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.employeeId)
        val _tmpFaceEmbedding: String? = entity.faceEmbedding
        if (_tmpFaceEmbedding == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpFaceEmbedding)
        }
        val _tmpEnrolledAt: Long? = entity.enrolledAt
        if (_tmpEnrolledAt == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpEnrolledAt)
        }
      }
    }
    this.__deleteAdapterOfStaffEntity = object : EntityDeleteOrUpdateAdapter<StaffEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `staff` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: StaffEntity) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfStaffEntity = object : EntityDeleteOrUpdateAdapter<StaffEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `staff` SET `id` = ?,`name` = ?,`employeeId` = ?,`faceEmbedding` = ?,`enrolledAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: StaffEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.employeeId)
        val _tmpFaceEmbedding: String? = entity.faceEmbedding
        if (_tmpFaceEmbedding == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpFaceEmbedding)
        }
        val _tmpEnrolledAt: Long? = entity.enrolledAt
        if (_tmpEnrolledAt == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpEnrolledAt)
        }
        statement.bindLong(6, entity.id)
      }
    }
  }

  public override suspend fun insert(staff: StaffEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfStaffEntity.insertAndReturnId(_connection, staff)
    _result
  }

  public override suspend fun insertAll(staff: List<StaffEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfStaffEntity_1.insert(_connection, staff)
  }

  public override suspend fun delete(staff: StaffEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfStaffEntity.handle(_connection, staff)
  }

  public override suspend fun update(staff: StaffEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfStaffEntity.handle(_connection, staff)
  }

  public override fun getAllStaff(): Flow<List<StaffEntity>> {
    val _sql: String = "SELECT * FROM staff ORDER BY name ASC"
    return createFlow(__db, false, arrayOf("staff")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfEmployeeId: Int = getColumnIndexOrThrow(_stmt, "employeeId")
        val _columnIndexOfFaceEmbedding: Int = getColumnIndexOrThrow(_stmt, "faceEmbedding")
        val _columnIndexOfEnrolledAt: Int = getColumnIndexOrThrow(_stmt, "enrolledAt")
        val _result: MutableList<StaffEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: StaffEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpEmployeeId: String
          _tmpEmployeeId = _stmt.getText(_columnIndexOfEmployeeId)
          val _tmpFaceEmbedding: String?
          if (_stmt.isNull(_columnIndexOfFaceEmbedding)) {
            _tmpFaceEmbedding = null
          } else {
            _tmpFaceEmbedding = _stmt.getText(_columnIndexOfFaceEmbedding)
          }
          val _tmpEnrolledAt: Long?
          if (_stmt.isNull(_columnIndexOfEnrolledAt)) {
            _tmpEnrolledAt = null
          } else {
            _tmpEnrolledAt = _stmt.getLong(_columnIndexOfEnrolledAt)
          }
          _item = StaffEntity(_tmpId,_tmpName,_tmpEmployeeId,_tmpFaceEmbedding,_tmpEnrolledAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getStaffById(id: Long): StaffEntity? {
    val _sql: String = "SELECT * FROM staff WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfEmployeeId: Int = getColumnIndexOrThrow(_stmt, "employeeId")
        val _columnIndexOfFaceEmbedding: Int = getColumnIndexOrThrow(_stmt, "faceEmbedding")
        val _columnIndexOfEnrolledAt: Int = getColumnIndexOrThrow(_stmt, "enrolledAt")
        val _result: StaffEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpEmployeeId: String
          _tmpEmployeeId = _stmt.getText(_columnIndexOfEmployeeId)
          val _tmpFaceEmbedding: String?
          if (_stmt.isNull(_columnIndexOfFaceEmbedding)) {
            _tmpFaceEmbedding = null
          } else {
            _tmpFaceEmbedding = _stmt.getText(_columnIndexOfFaceEmbedding)
          }
          val _tmpEnrolledAt: Long?
          if (_stmt.isNull(_columnIndexOfEnrolledAt)) {
            _tmpEnrolledAt = null
          } else {
            _tmpEnrolledAt = _stmt.getLong(_columnIndexOfEnrolledAt)
          }
          _result = StaffEntity(_tmpId,_tmpName,_tmpEmployeeId,_tmpFaceEmbedding,_tmpEnrolledAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByEmployeeId(employeeId: String): StaffEntity? {
    val _sql: String = "SELECT * FROM staff WHERE employeeId = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, employeeId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfEmployeeId: Int = getColumnIndexOrThrow(_stmt, "employeeId")
        val _columnIndexOfFaceEmbedding: Int = getColumnIndexOrThrow(_stmt, "faceEmbedding")
        val _columnIndexOfEnrolledAt: Int = getColumnIndexOrThrow(_stmt, "enrolledAt")
        val _result: StaffEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpEmployeeId: String
          _tmpEmployeeId = _stmt.getText(_columnIndexOfEmployeeId)
          val _tmpFaceEmbedding: String?
          if (_stmt.isNull(_columnIndexOfFaceEmbedding)) {
            _tmpFaceEmbedding = null
          } else {
            _tmpFaceEmbedding = _stmt.getText(_columnIndexOfFaceEmbedding)
          }
          val _tmpEnrolledAt: Long?
          if (_stmt.isNull(_columnIndexOfEnrolledAt)) {
            _tmpEnrolledAt = null
          } else {
            _tmpEnrolledAt = _stmt.getLong(_columnIndexOfEnrolledAt)
          }
          _result = StaffEntity(_tmpId,_tmpName,_tmpEmployeeId,_tmpFaceEmbedding,_tmpEnrolledAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getEnrolledStaff(): List<StaffEntity> {
    val _sql: String = "SELECT * FROM staff WHERE faceEmbedding IS NOT NULL"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfEmployeeId: Int = getColumnIndexOrThrow(_stmt, "employeeId")
        val _columnIndexOfFaceEmbedding: Int = getColumnIndexOrThrow(_stmt, "faceEmbedding")
        val _columnIndexOfEnrolledAt: Int = getColumnIndexOrThrow(_stmt, "enrolledAt")
        val _result: MutableList<StaffEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: StaffEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpEmployeeId: String
          _tmpEmployeeId = _stmt.getText(_columnIndexOfEmployeeId)
          val _tmpFaceEmbedding: String?
          if (_stmt.isNull(_columnIndexOfFaceEmbedding)) {
            _tmpFaceEmbedding = null
          } else {
            _tmpFaceEmbedding = _stmt.getText(_columnIndexOfFaceEmbedding)
          }
          val _tmpEnrolledAt: Long?
          if (_stmt.isNull(_columnIndexOfEnrolledAt)) {
            _tmpEnrolledAt = null
          } else {
            _tmpEnrolledAt = _stmt.getLong(_columnIndexOfEnrolledAt)
          }
          _item = StaffEntity(_tmpId,_tmpName,_tmpEmployeeId,_tmpFaceEmbedding,_tmpEnrolledAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateFaceEmbedding(
    id: Long,
    embedding: String,
    enrolledAt: Long,
  ) {
    val _sql: String = """
        |
        |        UPDATE staff
        |        SET faceEmbedding = ?,
        |            enrolledAt    = ?
        |        WHERE id = ?
        |        
        """.trimMargin()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, embedding)
        _argIndex = 2
        _stmt.bindLong(_argIndex, enrolledAt)
        _argIndex = 3
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearFaceEmbedding(id: Long) {
    val _sql: String = "UPDATE staff SET faceEmbedding = NULL, enrolledAt = NULL WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
