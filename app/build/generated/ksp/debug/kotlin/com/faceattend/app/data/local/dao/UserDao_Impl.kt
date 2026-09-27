package com.faceattend.app.`data`.local.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.faceattend.app.`data`.local.db.Converters
import com.faceattend.app.`data`.local.entity.UserEntity
import com.faceattend.app.`data`.model.Role
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

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class UserDao_Impl(
  __db: RoomDatabase,
) : UserDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfUserEntity: EntityInsertAdapter<UserEntity>

  private val __converters: Converters = Converters()

  private val __insertAdapterOfUserEntity_1: EntityInsertAdapter<UserEntity>

  private val __deleteAdapterOfUserEntity: EntityDeleteOrUpdateAdapter<UserEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfUserEntity = object : EntityInsertAdapter<UserEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `users` (`id`,`username`,`password`,`role`,`staffId`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: UserEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.username)
        statement.bindText(3, entity.password)
        val _tmp: String = __converters.fromRole(entity.role)
        statement.bindText(4, _tmp)
        val _tmpStaffId: Long? = entity.staffId
        if (_tmpStaffId == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpStaffId)
        }
      }
    }
    this.__insertAdapterOfUserEntity_1 = object : EntityInsertAdapter<UserEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `users` (`id`,`username`,`password`,`role`,`staffId`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: UserEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.username)
        statement.bindText(3, entity.password)
        val _tmp: String = __converters.fromRole(entity.role)
        statement.bindText(4, _tmp)
        val _tmpStaffId: Long? = entity.staffId
        if (_tmpStaffId == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpStaffId)
        }
      }
    }
    this.__deleteAdapterOfUserEntity = object : EntityDeleteOrUpdateAdapter<UserEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `users` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: UserEntity) {
        statement.bindLong(1, entity.id)
      }
    }
  }

  public override suspend fun insert(user: UserEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfUserEntity.insertAndReturnId(_connection, user)
    _result
  }

  public override suspend fun insertAll(users: List<UserEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfUserEntity_1.insert(_connection, users)
  }

  public override suspend fun delete(user: UserEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfUserEntity.handle(_connection, user)
  }

  public override suspend fun login(username: String, password: String): UserEntity? {
    val _sql: String = "SELECT * FROM users WHERE username = ? AND password = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, username)
        _argIndex = 2
        _stmt.bindText(_argIndex, password)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUsername: Int = getColumnIndexOrThrow(_stmt, "username")
        val _columnIndexOfPassword: Int = getColumnIndexOrThrow(_stmt, "password")
        val _columnIndexOfRole: Int = getColumnIndexOrThrow(_stmt, "role")
        val _columnIndexOfStaffId: Int = getColumnIndexOrThrow(_stmt, "staffId")
        val _result: UserEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpUsername: String
          _tmpUsername = _stmt.getText(_columnIndexOfUsername)
          val _tmpPassword: String
          _tmpPassword = _stmt.getText(_columnIndexOfPassword)
          val _tmpRole: Role
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfRole)
          _tmpRole = __converters.toRole(_tmp)
          val _tmpStaffId: Long?
          if (_stmt.isNull(_columnIndexOfStaffId)) {
            _tmpStaffId = null
          } else {
            _tmpStaffId = _stmt.getLong(_columnIndexOfStaffId)
          }
          _result = UserEntity(_tmpId,_tmpUsername,_tmpPassword,_tmpRole,_tmpStaffId)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByUsername(username: String): UserEntity? {
    val _sql: String = "SELECT * FROM users WHERE username = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, username)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUsername: Int = getColumnIndexOrThrow(_stmt, "username")
        val _columnIndexOfPassword: Int = getColumnIndexOrThrow(_stmt, "password")
        val _columnIndexOfRole: Int = getColumnIndexOrThrow(_stmt, "role")
        val _columnIndexOfStaffId: Int = getColumnIndexOrThrow(_stmt, "staffId")
        val _result: UserEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpUsername: String
          _tmpUsername = _stmt.getText(_columnIndexOfUsername)
          val _tmpPassword: String
          _tmpPassword = _stmt.getText(_columnIndexOfPassword)
          val _tmpRole: Role
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfRole)
          _tmpRole = __converters.toRole(_tmp)
          val _tmpStaffId: Long?
          if (_stmt.isNull(_columnIndexOfStaffId)) {
            _tmpStaffId = null
          } else {
            _tmpStaffId = _stmt.getLong(_columnIndexOfStaffId)
          }
          _result = UserEntity(_tmpId,_tmpUsername,_tmpPassword,_tmpRole,_tmpStaffId)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllUsers(): List<UserEntity> {
    val _sql: String = "SELECT * FROM users ORDER BY username ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUsername: Int = getColumnIndexOrThrow(_stmt, "username")
        val _columnIndexOfPassword: Int = getColumnIndexOrThrow(_stmt, "password")
        val _columnIndexOfRole: Int = getColumnIndexOrThrow(_stmt, "role")
        val _columnIndexOfStaffId: Int = getColumnIndexOrThrow(_stmt, "staffId")
        val _result: MutableList<UserEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: UserEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpUsername: String
          _tmpUsername = _stmt.getText(_columnIndexOfUsername)
          val _tmpPassword: String
          _tmpPassword = _stmt.getText(_columnIndexOfPassword)
          val _tmpRole: Role
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfRole)
          _tmpRole = __converters.toRole(_tmp)
          val _tmpStaffId: Long?
          if (_stmt.isNull(_columnIndexOfStaffId)) {
            _tmpStaffId = null
          } else {
            _tmpStaffId = _stmt.getLong(_columnIndexOfStaffId)
          }
          _item = UserEntity(_tmpId,_tmpUsername,_tmpPassword,_tmpRole,_tmpStaffId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateStaffId(username: String, staffId: Long) {
    val _sql: String = "UPDATE users SET staffId = ? WHERE username = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, staffId)
        _argIndex = 2
        _stmt.bindText(_argIndex, username)
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
