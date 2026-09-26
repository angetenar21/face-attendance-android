package com.salarybox.app.data.repository

import com.salarybox.app.data.local.dao.UserDao
import com.salarybox.app.data.local.entity.UserEntity
import com.salarybox.app.data.model.Role

/**
 * Repository for authentication operations.
 *
 * Abstracts the DAO behind a domain-level API consumed by [AuthViewModel].
 * All functions are suspend so callers run them in a coroutine scope — there
 * is intentionally no Flow here because login is a one-shot event, not a stream.
 */
class AuthRepository(private val userDao: UserDao) {

    /**
     * Attempts to authenticate with the given credentials.
     *
     * @return The matching [UserEntity], or null if credentials are invalid.
     */
    suspend fun login(username: String, password: String): UserEntity? =
        userDao.login(username.trim(), password)

    /**
     * Returns true if a user with [username] already exists.
     * Useful for registration / duplicate-check flows.
     */
    suspend fun userExists(username: String): Boolean =
        userDao.getByUsername(username.trim()) != null

    /**
     * Creates a new user account.
     *
     * @return The auto-generated row ID of the inserted user.
     * @throws android.database.sqlite.SQLiteConstraintException if [username] is not unique.
     */
    suspend fun createUser(username: String, password: String, role: Role): Long =
        userDao.insert(
            UserEntity(username = username.trim(), password = password, role = role)
        )

    /** Returns all users — for admin management screens only. */
    suspend fun getAllUsers(): List<UserEntity> = userDao.getAllUsers()
}
