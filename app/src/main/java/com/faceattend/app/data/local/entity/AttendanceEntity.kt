package com.faceattend.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single attendance check-in/check-out record for a staff member.
 *
 * [staffId] is a foreign key referencing [StaffEntity.id].  Deleting a
 * staff member cascades to delete all their attendance records.
 *
 * [timestamp]       – epoch millis of the check-in moment.
 * [selfiePath]      – absolute file path of the captured selfie (stored on-device).
 * [latitude]        – GPS latitude at the time of check-in.
 * [longitude]       – GPS longitude at the time of check-in.
 * [matchConfidence] – 0.0–1.0 score from the face-matching step.
 */
@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = StaffEntity::class,
            parentColumns = ["id"],
            childColumns = ["staffId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["staffId"]),
        Index(value = ["timestamp"])
    ]
)
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** References [StaffEntity.id]. */
    val staffId: Long,

    /** Epoch millis — when the check-in was recorded. */
    val timestamp: Long,

    /** Absolute path to the selfie image file saved on device storage. */
    val selfiePath: String,

    /** Latitude from the Fused Location Provider at check-in time. */
    val latitude: Double,

    /** Longitude from the Fused Location Provider at check-in time. */
    val longitude: Double,

    /** Face-match confidence score in the range [0.0, 1.0]. */
    val matchConfidence: Float
)
