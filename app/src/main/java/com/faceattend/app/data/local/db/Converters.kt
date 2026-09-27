package com.faceattend.app.data.local.db

import androidx.room.TypeConverter
import com.faceattend.app.data.model.Role

/**
 * Room type converters for non-primitive column types.
 *
 * Registered globally on [AppDatabase] via @TypeConverters annotation.
 */
class Converters {

    // --- Role <-> String ---

    @TypeConverter
    fun fromRole(role: Role): String = role.name

    @TypeConverter
    fun toRole(value: String): Role = Role.valueOf(value)
}
