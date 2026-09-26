package com.salarybox.app.util

import android.content.Context
import android.widget.Toast

// ---------------------------------------------------------------------------
// Extension helpers
// ---------------------------------------------------------------------------

/** Show a short [Toast] from any [Context]. */
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/** Capitalise the first character of a [String] and lowercase the rest. */
fun String.titleCase(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
