package com.salarybox.app.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.salarybox.app.data.local.dao.AttendanceDao
import com.salarybox.app.data.local.dao.StaffDao
import com.salarybox.app.data.local.dao.UserDao
import com.salarybox.app.data.local.entity.AttendanceEntity
import com.salarybox.app.data.local.entity.StaffEntity
import com.salarybox.app.data.local.entity.UserEntity
import com.salarybox.app.data.model.Role
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * SalaryBox Room database.
 *
 * ## Versioning
 * Increment [version] and add a [androidx.room.migration.Migration] object to
 * [MIGRATIONS] whenever the schema changes.  Never reset [version] — Room
 * will throw if it detects a regression.
 *
 * ## Seeding
 * Two dummy users are inserted via [SeedCallback.onCreate], which Room calls
 * exactly once when the database file is first created.  This approach was
 * chosen over WorkManager because:
 *  - It runs atomically inside the DB creation transaction.
 *  - It requires zero extra dependencies.
 *  - It fires before any DAO call can succeed, ensuring the data is always
 *    present by the time the app UI is ready.
 */
@Database(
    entities = [
        UserEntity::class,
        StaffEntity::class,
        AttendanceEntity::class,
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun staffDao(): StaffDao
    abstract fun attendanceDao(): AttendanceDao

    // -----------------------------------------------------------------------
    // Singleton
    // -----------------------------------------------------------------------

    companion object {

        private const val DATABASE_NAME = "salarybox.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }

        /**
         * Migration from v1 → v2: adds staffId column to users table.
         * Uses NULL as default (existing admin/staff accounts have no linked staff record).
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN staffId INTEGER")
            }
        }

        private fun buildDatabase(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .addCallback(SeedCallback())
                .addMigrations(MIGRATION_1_2)
                .build()
    }

    // -----------------------------------------------------------------------
    // Seed callback
    // -----------------------------------------------------------------------

    /**
     * Inserts dummy seed data the very first time the database is created.
     *
     * Runs on a background coroutine so the calling thread (main) is not blocked.
     * The data is available immediately once the first DB transaction completes.
     */
    private class SeedCallback : Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            // Launch on IO — INSTANCE is guaranteed non-null here because Room
            // populates it before firing onCreate callbacks.
            CoroutineScope(Dispatchers.IO).launch {
                seedDatabase(INSTANCE!!)
            }
        }

        private suspend fun seedDatabase(database: AppDatabase) {
            val userDao = database.userDao()

            // Only seed if the table is empty (extra safety guard).
            if (userDao.getAllUsers().isNotEmpty()) return

            // Seed a single admin user. Staff user accounts are created automatically
            // when the admin registers a staff member in AddStaffScreen.
            userDao.insertAll(
                listOf(
                    UserEntity(
                        username = "admin",
                        password = "admin123",   // plain text — prototype only
                        role = Role.ADMIN
                    )
                )
            )
        }
    }
}
