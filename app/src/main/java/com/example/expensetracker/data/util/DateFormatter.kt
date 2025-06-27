package com.example.expensetracker.data.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {
    /** e.g. “Jun 25, 2025” */
    @SuppressLint("ConstantLocale")
    private val displayFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    /** e.g. “2025-06-25” for sorting or APIs */
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun toDisplay(date: Date): String = displayFormat.format(date)
    fun toIso(date: Date): String = isoFormat.format(date)
}
