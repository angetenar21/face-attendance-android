package com.salarybox.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.salarybox.app.data.model.Role

/**
 * Represents an app user (admin or staff member) who can log in.
 *
 * NOTE: Password is stored as plain text intentionally for this prototype.
 *       Replace with a hashed value (e.g., BCrypt) before production use.
 */
@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Unique login name. */
    val username: String,

    /** Plain-text password (prototype only). */
    val password: String,

    /** ADMIN or STAFF — stored as a string via [com.salarybox.app.data.local.db.Converters]. */
    val role: Role,

    /**
     * FK to [StaffEntity.id] — non-null only for STAFF role users.
     * Links the login account to the staff record for face-verified attendance.
     */
    val staffId: Long? = null
)
