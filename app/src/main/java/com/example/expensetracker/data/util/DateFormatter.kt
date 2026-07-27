package com.example.expensetracker.data.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Collections
import java.util.Date
import java.util.Locale

object DateFormatter {
    private val cachedFormatters = Collections.synchronizedMap(mutableMapOf<String, DateTimeFormatter>())

    fun toDisplay(date: Date, style: FormatStyle = FormatStyle.MEDIUM): String {
        val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val pattern = when (style) {
            FormatStyle.SHORT  -> "MMM dd"
            FormatStyle.MEDIUM -> "MMM dd, yyyy"
            FormatStyle.LONG   -> "MMMM dd, yyyy"
            else               -> "MMM dd, yyyy"
        }
        return getFormatter(pattern).format(localDate)
    }

    fun formatMonthYear(yearMonth: YearMonth): String =
        getFormatter("MMMM yyyy").format(yearMonth)

    // CORRECTED: Using DateTimeFormatter for LocalDate
    fun formatShortDate(date: LocalDate): String =
        DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()).format(date)

    fun formatForDatabase(date: LocalDate): String =
        DateTimeFormatter.ISO_LOCAL_DATE.format(date)

    private fun getFormatter(pattern: String): DateTimeFormatter =
        cachedFormatters.getOrPut(pattern) {
            DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
        }
}