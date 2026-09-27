package com.salarybox.app.`data`.local.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.salarybox.app.`data`.local.dao.AttendanceDao
import com.salarybox.app.`data`.local.dao.AttendanceDao_Impl
import com.salarybox.app.`data`.local.dao.StaffDao
import com.salarybox.app.`data`.local.dao.StaffDao_Impl
import com.salarybox.app.`data`.local.dao.UserDao
import com.salarybox.app.`data`.local.dao.UserDao_Impl
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

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _userDao: Lazy<UserDao> = lazy {
    UserDao_Impl(this)
  }

  private val _staffDao: Lazy<StaffDao> = lazy {
    StaffDao_Impl(this)
  }

  private val _attendanceDao: Lazy<AttendanceDao> = lazy {
    AttendanceDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(2, "9bea1bef71931a6c281e5de28da68727", "cf27a4696c116fe32b9a1493a7b5ff37") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `users` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `username` TEXT NOT NULL, `password` TEXT NOT NULL, `role` TEXT NOT NULL, `staffId` INTEGER)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_username` ON `users` (`username`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `staff` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `employeeId` TEXT NOT NULL, `faceEmbedding` TEXT, `enrolledAt` INTEGER)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_staff_employeeId` ON `staff` (`employeeId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `attendance` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `staffId` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `selfiePath` TEXT NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `matchConfidence` REAL NOT NULL, FOREIGN KEY(`staffId`) REFERENCES `staff`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_attendance_staffId` ON `attendance` (`staffId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_attendance_timestamp` ON `attendance` (`timestamp`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '9bea1bef71931a6c281e5de28da68727')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `users`")
        connection.execSQL("DROP TABLE IF EXISTS `staff`")
        connection.execSQL("DROP TABLE IF EXISTS `attendance`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsUsers: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsUsers.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUsers.put("username", TableInfo.Column("username", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUsers.put("password", TableInfo.Column("password", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUsers.put("role", TableInfo.Column("role", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUsers.put("staffId", TableInfo.Column("staffId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysUsers: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesUsers: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesUsers.add(TableInfo.Index("index_users_username", true, listOf("username"), listOf("ASC")))
        val _infoUsers: TableInfo = TableInfo("users", _columnsUsers, _foreignKeysUsers, _indicesUsers)
        val _existingUsers: TableInfo = read(connection, "users")
        if (!_infoUsers.equals(_existingUsers)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |users(com.salarybox.app.data.local.entity.UserEntity).
              | Expected:
              |""".trimMargin() + _infoUsers + """
              |
              | Found:
              |""".trimMargin() + _existingUsers)
        }
        val _columnsStaff: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsStaff.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStaff.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStaff.put("employeeId", TableInfo.Column("employeeId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStaff.put("faceEmbedding", TableInfo.Column("faceEmbedding", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStaff.put("enrolledAt", TableInfo.Column("enrolledAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysStaff: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesStaff: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesStaff.add(TableInfo.Index("index_staff_employeeId", true, listOf("employeeId"), listOf("ASC")))
        val _infoStaff: TableInfo = TableInfo("staff", _columnsStaff, _foreignKeysStaff, _indicesStaff)
        val _existingStaff: TableInfo = read(connection, "staff")
        if (!_infoStaff.equals(_existingStaff)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |staff(com.salarybox.app.data.local.entity.StaffEntity).
              | Expected:
              |""".trimMargin() + _infoStaff + """
              |
              | Found:
              |""".trimMargin() + _existingStaff)
        }
        val _columnsAttendance: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsAttendance.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttendance.put("staffId", TableInfo.Column("staffId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttendance.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttendance.put("selfiePath", TableInfo.Column("selfiePath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttendance.put("latitude", TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttendance.put("longitude", TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttendance.put("matchConfidence", TableInfo.Column("matchConfidence", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysAttendance: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysAttendance.add(TableInfo.ForeignKey("staff", "CASCADE", "NO ACTION", listOf("staffId"), listOf("id")))
        val _indicesAttendance: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesAttendance.add(TableInfo.Index("index_attendance_staffId", false, listOf("staffId"), listOf("ASC")))
        _indicesAttendance.add(TableInfo.Index("index_attendance_timestamp", false, listOf("timestamp"), listOf("ASC")))
        val _infoAttendance: TableInfo = TableInfo("attendance", _columnsAttendance, _foreignKeysAttendance, _indicesAttendance)
        val _existingAttendance: TableInfo = read(connection, "attendance")
        if (!_infoAttendance.equals(_existingAttendance)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |attendance(com.salarybox.app.data.local.entity.AttendanceEntity).
              | Expected:
              |""".trimMargin() + _infoAttendance + """
              |
              | Found:
              |""".trimMargin() + _existingAttendance)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "users", "staff", "attendance")
  }

  public override fun clearAllTables() {
    super.performClear(true, "users", "staff", "attendance")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(UserDao::class, UserDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(StaffDao::class, StaffDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(AttendanceDao::class, AttendanceDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun userDao(): UserDao = _userDao.value

  public override fun staffDao(): StaffDao = _staffDao.value

  public override fun attendanceDao(): AttendanceDao = _attendanceDao.value
}
