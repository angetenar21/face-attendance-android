package com.salarybox.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.salarybox.app.data.local.entity.UserEntity

/**
 * DAO for [UserEntity] — authentication queries.
 */
@Dao
interface UserDao {

    // --- Write ---

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    @Delete
    suspend fun delete(user: UserEntity)

    // --- Read ---

    /**
     * Returns the user matching [username] and [password], or null if credentials
     * are invalid.  Used by the login flow.
     */
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): UserEntity?

    /** Look up a user by username only (e.g. to check existence). */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): UserEntity?

    /** Return all users (admin only — management screen). */
    @Query("SELECT * FROM users ORDER BY username ASC")
    suspend fun getAllUsers(): List<UserEntity>
}
