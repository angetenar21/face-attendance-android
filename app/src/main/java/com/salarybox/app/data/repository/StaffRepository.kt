package com.salarybox.app.data.repository

import com.salarybox.app.data.local.dao.StaffDao
import com.salarybox.app.data.local.entity.StaffEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for staff member management operations.
 *
 * Exposes [getAllStaff] as a [Flow] so the UI can react to table changes
 * without manual refresh calls.  Mutation operations are suspend functions.
 */
class StaffRepository(private val staffDao: StaffDao) {

    // --- Observation ---

    /**
     * Cold Flow that emits the full staff list whenever the table changes.
     * Collect in a ViewModel with [androidx.lifecycle.viewModelScope].
     */
    fun getAllStaff(): Flow<List<StaffEntity>> = staffDao.getAllStaff()

    // --- One-shot reads ---

    suspend fun getStaffById(id: Long): StaffEntity? = staffDao.getStaffById(id)

    suspend fun getByEmployeeId(employeeId: String): StaffEntity? =
        staffDao.getByEmployeeId(employeeId)

    /** Returns only staff members who have completed face enrollment. */
    suspend fun getEnrolledStaff(): List<StaffEntity> = staffDao.getEnrolledStaff()

    // --- Mutations ---

    /**
     * Adds a new staff member record.
     *
     * @return Auto-generated primary key.
     * @throws android.database.sqlite.SQLiteConstraintException if [StaffEntity.employeeId] is not unique.
     */
    suspend fun addStaff(staff: StaffEntity): Long = staffDao.insert(staff)

    suspend fun updateStaff(staff: StaffEntity) = staffDao.update(staff)

    suspend fun deleteStaff(staff: StaffEntity) = staffDao.delete(staff)

    /**
     * Persists the face embedding vector and marks the enrollment timestamp.
     *
     * @param id         Primary key of the staff member.
     * @param embedding  Comma-separated Float string from ML Kit (e.g. "0.12,-0.56,...").
     * @param enrolledAt Epoch millis of enrollment — defaults to now.
     */
    suspend fun updateFaceEmbedding(
        id: Long,
        embedding: String,
        enrolledAt: Long = System.currentTimeMillis()
    ) = staffDao.updateFaceEmbedding(id, embedding, enrolledAt)

    /** Clears face enrollment so the staff member can re-enroll. */
    suspend fun clearFaceEmbedding(id: Long) = staffDao.clearFaceEmbedding(id)
}
