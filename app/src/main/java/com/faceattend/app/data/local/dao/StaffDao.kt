package com.faceattend.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.faceattend.app.data.local.entity.StaffEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for [StaffEntity] — staff member management queries.
 */
@Dao
interface StaffDao {

    // --- Write ---

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(staff: StaffEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(staff: List<StaffEntity>)

    @Update
    suspend fun update(staff: StaffEntity)

    @Delete
    suspend fun delete(staff: StaffEntity)

    // --- Targeted updates ---

    /**
     * Persists the face embedding string and enrollment timestamp for a specific
     * staff member.  Called once the ML Kit enrollment step completes.
     *
     * @param id            Primary key of the staff member.
     * @param embedding     Comma-separated Float string from the face encoder.
     * @param enrolledAt    Epoch millis of enrollment completion.
     */
    @Query(
        """
        UPDATE staff
        SET faceEmbedding = :embedding,
            enrolledAt    = :enrolledAt
        WHERE id = :id
        """
    )
    suspend fun updateFaceEmbedding(id: Long, embedding: String, enrolledAt: Long)

    /** Clears face enrollment data (e.g. re-enroll flow). */
    @Query("UPDATE staff SET faceEmbedding = NULL, enrolledAt = NULL WHERE id = :id")
    suspend fun clearFaceEmbedding(id: Long)

    // --- Read ---

    /**
     * Observes the full staff list sorted by name.
     * Emits a new list whenever the table changes — suitable for LazyColumn.
     */
    @Query("SELECT * FROM staff ORDER BY name ASC")
    fun getAllStaff(): Flow<List<StaffEntity>>

    /** One-shot fetch of a single staff member by primary key. */
    @Query("SELECT * FROM staff WHERE id = :id LIMIT 1")
    suspend fun getStaffById(id: Long): StaffEntity?

    /** One-shot fetch by the human-readable employee ID string. */
    @Query("SELECT * FROM staff WHERE employeeId = :employeeId LIMIT 1")
    suspend fun getByEmployeeId(employeeId: String): StaffEntity?

    /** Returns all staff members who have completed face enrollment. */
    @Query("SELECT * FROM staff WHERE faceEmbedding IS NOT NULL")
    suspend fun getEnrolledStaff(): List<StaffEntity>
}
