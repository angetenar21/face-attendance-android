package com.faceattend.app.data.repository

import com.faceattend.app.data.local.dao.AttendanceDao
import com.faceattend.app.data.local.entity.AttendanceEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for attendance record operations.
 *
 * All read queries that drive UI lists are [Flow]-based so Compose state
 * updates automatically.  The insert is a suspend function — it returns the
 * new record's ID which can be used for optimistic confirmation feedback.
 */
class AttendanceRepository(private val attendanceDao: AttendanceDao) {

    // --- Observation (Flow) ---

    /**
     * Emits attendance records for a specific staff member, newest first.
     * Use in Staff detail screens.
     */
    fun getAttendanceForStaff(staffId: Long): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendanceForStaff(staffId)

    /**
     * Emits the full attendance log across all staff, newest first.
     * Use in the Admin dashboard.
     */
    fun getAllAttendance(): Flow<List<AttendanceEntity>> =
        attendanceDao.getAllAttendance()

    /**
     * Emits attendance records within an epoch-millis range.
     * Useful for payroll period exports.
     */
    fun getAttendanceInRange(from: Long, to: Long): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendanceInRange(from, to)

    // --- One-shot reads ---

    /**
     * Returns the most recent attendance record for [staffId], or null.
     * Use to check whether an employee has already checked in today.
     */
    suspend fun getLatestForStaff(staffId: Long): AttendanceEntity? =
        attendanceDao.getLatestForStaff(staffId)

    // --- Mutations ---

    /**
     * Inserts a new attendance check-in record.
     *
     * @return Auto-generated row ID of the inserted record.
     */
    suspend fun insertAttendance(record: AttendanceEntity): Long =
        attendanceDao.insertAttendance(record)

    suspend fun deleteAttendance(record: AttendanceEntity) =
        attendanceDao.delete(record)

    /** Removes all attendance records for a staff member (used before staff deletion). */
    suspend fun deleteAllForStaff(staffId: Long) =
        attendanceDao.deleteAllForStaff(staffId)
}
