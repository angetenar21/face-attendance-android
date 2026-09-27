package com.faceattend.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents an enrolled staff member tracked for attendance.
 *
 * [faceEmbedding] stores the ML Kit face feature vector as a comma-separated
 * Float string (e.g. "0.12,0.87,...").  The exact encoding is deferred until
 * the face-recognition step; swap the format by updating
 * [com.faceattend.app.data.repository.StaffRepository.updateFaceEmbedding] only.
 *
 * [enrolledAt] is an epoch-millis timestamp set when the face is first enrolled;
 * null means the staff member has not yet completed face enrollment.
 */
@Entity(
    tableName = "staff",
    indices = [Index(value = ["employeeId"], unique = true)]
)
data class StaffEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Full display name. */
    val name: String,

    /** Human-readable unique employee identifier (e.g. "EMP-001"). */
    val employeeId: String,

    /**
     * Comma-separated Float representation of the face embedding vector,
     * or null if the employee has not been enrolled yet.
     *
     * Example: "0.1234,-0.5678,0.9012,..."
     */
    val faceEmbedding: String? = null,

    /** Epoch millis when face enrollment was completed; null = not enrolled. */
    val enrolledAt: Long? = null
)
