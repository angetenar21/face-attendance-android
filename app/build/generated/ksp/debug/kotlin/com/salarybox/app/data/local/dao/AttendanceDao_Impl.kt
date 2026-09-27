package com.salarybox.app.`data`.local.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.salarybox.app.`data`.local.entity.AttendanceEntity
import javax.`annotation`.processing.Generated
import kotlin.Double
import kotlin.Float
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
public class AttendanceDao_Impl(
  __db: RoomDatabase,
) : AttendanceDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfAttendanceEntity: EntityInsertAdapter<AttendanceEntity>

  private val __deleteAdapterOfAttendanceEntity: EntityDeleteOrUpdateAdapter<AttendanceEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfAttendanceEntity = object : EntityInsertAdapter<AttendanceEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `attendance` (`id`,`staffId`,`timestamp`,`selfiePath`,`latitude`,`longitude`,`matchConfidence`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: AttendanceEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.staffId)
        statement.bindLong(3, entity.timestamp)
        statement.bindText(4, entity.selfiePath)
        statement.bindDouble(5, entity.latitude)
        statement.bindDouble(6, entity.longitude)
        statement.bindDouble(7, entity.matchConfidence.toDouble())
      }
    }
    this.__deleteAdapterOfAttendanceEntity = object : EntityDeleteOrUpdateAdapter<AttendanceEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `attendance` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: AttendanceEntity) {
        statement.bindLong(1, entity.id)
      }
    }
  }

  public override suspend fun insertAttendance(record: AttendanceEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfAttendanceEntity.insertAndReturnId(_connection, record)
    _result
  }

  public override suspend fun delete(record: AttendanceEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfAttendanceEntity.handle(_connection, record)
  }

  public override fun getAttendanceForStaff(staffId: Long): Flow<List<AttendanceEntity>> {
    val _sql: String = """
        |
        |        SELECT * FROM attendance
        |        WHERE staffId = ?
        |        ORDER BY timestamp DESC
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("attendance")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, staffId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfStaffId: Int = getColumnIndexOrThrow(_stmt, "staffId")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfSelfiePath: Int = getColumnIndexOrThrow(_stmt, "selfiePath")
        val _columnIndexOfLatitude: Int = getColumnIndexOrThrow(_stmt, "latitude")
        val _columnIndexOfLongitude: Int = getColumnIndexOrThrow(_stmt, "longitude")
        val _columnIndexOfMatchConfidence: Int = getColumnIndexOrThrow(_stmt, "matchConfidence")
        val _result: MutableList<AttendanceEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: AttendanceEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpStaffId: Long
          _tmpStaffId = _stmt.getLong(_columnIndexOfStaffId)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpSelfiePath: String
          _tmpSelfiePath = _stmt.getText(_columnIndexOfSelfiePath)
          val _tmpLatitude: Double
          _tmpLatitude = _stmt.getDouble(_columnIndexOfLatitude)
          val _tmpLongitude: Double
          _tmpLongitude = _stmt.getDouble(_columnIndexOfLongitude)
          val _tmpMatchConfidence: Float
          _tmpMatchConfidence = _stmt.getDouble(_columnIndexOfMatchConfidence).toFloat()
          _item = AttendanceEntity(_tmpId,_tmpStaffId,_tmpTimestamp,_tmpSelfiePath,_tmpLatitude,_tmpLongitude,_tmpMatchConfidence)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllAttendance(): Flow<List<AttendanceEntity>> {
    val _sql: String = "SELECT * FROM attendance ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("attendance")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfStaffId: Int = getColumnIndexOrThrow(_stmt, "staffId")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfSelfiePath: Int = getColumnIndexOrThrow(_stmt, "selfiePath")
        val _columnIndexOfLatitude: Int = getColumnIndexOrThrow(_stmt, "latitude")
        val _columnIndexOfLongitude: Int = getColumnIndexOrThrow(_stmt, "longitude")
        val _columnIndexOfMatchConfidence: Int = getColumnIndexOrThrow(_stmt, "matchConfidence")
        val _result: MutableList<AttendanceEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: AttendanceEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpStaffId: Long
          _tmpStaffId = _stmt.getLong(_columnIndexOfStaffId)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpSelfiePath: String
          _tmpSelfiePath = _stmt.getText(_columnIndexOfSelfiePath)
          val _tmpLatitude: Double
          _tmpLatitude = _stmt.getDouble(_columnIndexOfLatitude)
          val _tmpLongitude: Double
          _tmpLongitude = _stmt.getDouble(_columnIndexOfLongitude)
          val _tmpMatchConfidence: Float
          _tmpMatchConfidence = _stmt.getDouble(_columnIndexOfMatchConfidence).toFloat()
          _item = AttendanceEntity(_tmpId,_tmpStaffId,_tmpTimestamp,_tmpSelfiePath,_tmpLatitude,_tmpLongitude,_tmpMatchConfidence)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLatestForStaff(staffId: Long): AttendanceEntity? {
    val _sql: String = """
        |
        |        SELECT * FROM attendance
        |        WHERE staffId = ?
        |        ORDER BY timestamp DESC
        |        LIMIT 1
        |        
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, staffId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfStaffId: Int = getColumnIndexOrThrow(_stmt, "staffId")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfSelfiePath: Int = getColumnIndexOrThrow(_stmt, "selfiePath")
        val _columnIndexOfLatitude: Int = getColumnIndexOrThrow(_stmt, "latitude")
        val _columnIndexOfLongitude: Int = getColumnIndexOrThrow(_stmt, "longitude")
        val _columnIndexOfMatchConfidence: Int = getColumnIndexOrThrow(_stmt, "matchConfidence")
        val _result: AttendanceEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpStaffId: Long
          _tmpStaffId = _stmt.getLong(_columnIndexOfStaffId)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpSelfiePath: String
          _tmpSelfiePath = _stmt.getText(_columnIndexOfSelfiePath)
          val _tmpLatitude: Double
          _tmpLatitude = _stmt.getDouble(_columnIndexOfLatitude)
          val _tmpLongitude: Double
          _tmpLongitude = _stmt.getDouble(_columnIndexOfLongitude)
          val _tmpMatchConfidence: Float
          _tmpMatchConfidence = _stmt.getDouble(_columnIndexOfMatchConfidence).toFloat()
          _result = AttendanceEntity(_tmpId,_tmpStaffId,_tmpTimestamp,_tmpSelfiePath,_tmpLatitude,_tmpLongitude,_tmpMatchConfidence)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAttendanceInRange(from: Long, to: Long): Flow<List<AttendanceEntity>> {
    val _sql: String = """
        |
        |        SELECT * FROM attendance
        |        WHERE timestamp BETWEEN ? AND ?
        |        ORDER BY timestamp DESC
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("attendance")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, from)
        _argIndex = 2
        _stmt.bindLong(_argIndex, to)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfStaffId: Int = getColumnIndexOrThrow(_stmt, "staffId")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfSelfiePath: Int = getColumnIndexOrThrow(_stmt, "selfiePath")
        val _columnIndexOfLatitude: Int = getColumnIndexOrThrow(_stmt, "latitude")
        val _columnIndexOfLongitude: Int = getColumnIndexOrThrow(_stmt, "longitude")
        val _columnIndexOfMatchConfidence: Int = getColumnIndexOrThrow(_stmt, "matchConfidence")
        val _result: MutableList<AttendanceEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: AttendanceEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpStaffId: Long
          _tmpStaffId = _stmt.getLong(_columnIndexOfStaffId)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpSelfiePath: String
          _tmpSelfiePath = _stmt.getText(_columnIndexOfSelfiePath)
          val _tmpLatitude: Double
          _tmpLatitude = _stmt.getDouble(_columnIndexOfLatitude)
          val _tmpLongitude: Double
          _tmpLongitude = _stmt.getDouble(_columnIndexOfLongitude)
          val _tmpMatchConfidence: Float
          _tmpMatchConfidence = _stmt.getDouble(_columnIndexOfMatchConfidence).toFloat()
          _item = AttendanceEntity(_tmpId,_tmpStaffId,_tmpTimestamp,_tmpSelfiePath,_tmpLatitude,_tmpLongitude,_tmpMatchConfidence)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAllForStaff(staffId: Long) {
    val _sql: String = "DELETE FROM attendance WHERE staffId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, staffId)
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
