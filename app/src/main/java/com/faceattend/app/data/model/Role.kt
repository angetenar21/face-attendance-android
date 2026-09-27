package com.faceattend.app.data.model

/**
 * User role enum.
 *
 * Stored as a STRING column in Room via [com.faceattend.app.data.local.db.Converters].
 */
enum class Role {
    ADMIN,
    STAFF
}
