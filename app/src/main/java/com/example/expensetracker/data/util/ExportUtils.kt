package com.example.expensetracker.data.util

import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.model.TimeEntry

object ExportUtils {

    fun generateExpensesCsv(expenses: List<Expense>): String {
        val sb = StringBuilder()
        sb.append("ID,Title,Amount,Type,Category,Date,Currency,Note\n")
        for (e in expenses) {
            val noteClean = (e.note ?: "").replace(",", " ")
            val titleClean = e.title.replace(",", " ")
            sb.append("${e.id},$titleClean,${e.amount},${e.type},${e.category},${e.date},${e.currencyCode},$noteClean\n")
        }
        return sb.toString()
    }

    fun generateTimeLogsCsv(timeEntries: List<TimeEntry>): String {
        val sb = StringBuilder()
        sb.append("ID,Title,Category,Date,DurationMinutes,IsBillable,HourlyRate,Earnings\n")
        for (t in timeEntries) {
            val minutes = t.durationSeconds / 60
            val earnings = if (t.isBillable && t.hourlyRate != null) (t.durationSeconds / 3600.0) * t.hourlyRate else 0.0
            val titleClean = t.title.replace(",", " ")
            sb.append("${t.id},$titleClean,${t.category},${t.dateString},$minutes,${t.isBillable},${t.hourlyRate ?: 0.0},${String.format("%.2f", earnings)}\n")
        }
        return sb.toString()
    }
}
