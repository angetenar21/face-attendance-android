package com.salarybox.app.data.model

/**
 * User role enum.
 *
 * Stored as a STRING column in Room via [com.salarybox.app.data.local.db.Converters].
 */
enum class Role {
    ADMIN,
    STAFF
}
