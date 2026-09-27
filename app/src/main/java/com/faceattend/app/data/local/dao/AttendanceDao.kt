package com.faceattend.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.faceattend.app.data.local.entity.AttendanceEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for [AttendanceEntity] — attendance record queries.
 */
@Dao
interface AttendanceDao {

    // --- Write ---

    /**
     * Inserts a new attendance record and returns its generated row ID.
     * Uses ABORT so duplicate insertions surface as an exception rather than
     * silently overwriting existing records.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAttendance(record: AttendanceEntity): Long

    @Delete
    suspend fun delete(record: AttendanceEntity)

    /** Hard-deletes all records for a staff member (called before staff deletion). */
    @Query("DELETE FROM attendance WHERE staffId = :staffId")
    suspend fun deleteAllForStaff(staffId: Long)

    // --- Read ---

    /**
     * Observes all attendance records for a specific staff member,
     * newest first.  Emits whenever the table changes.
     */
    @Query(
        """
        SELECT * FROM attendance
        WHERE staffId = :staffId
        ORDER BY timestamp DESC
        """
    )
    fun getAttendanceForStaff(staffId: Long): Flow<List<AttendanceEntity>>

    /**
     * Observes the full attendance log across all staff, newest first.
     * Used by the Admin dashboard.
     */
    @Query("SELECT * FROM attendance ORDER BY timestamp DESC")
    fun getAllAttendance(): Flow<List<AttendanceEntity>>

    /**
     * Returns the most recent attendance record for a staff member.
     * Useful to determine whether the employee has already checked in today.
     */
    @Query(
        """
        SELECT * FROM attendance
        WHERE staffId = :staffId
        ORDER BY timestamp DESC
        LIMIT 1
        """
    )
    suspend fun getLatestForStaff(staffId: Long): AttendanceEntity?

    /**
     * Returns all attendance records in the given epoch-millis time range.
     * Useful for generating payroll/report exports.
     */
    @Query(
        """
        SELECT * FROM attendance
        WHERE timestamp BETWEEN :from AND :to
        ORDER BY timestamp DESC
        """
    )
    fun getAttendanceInRange(from: Long, to: Long): Flow<List<AttendanceEntity>>
}
