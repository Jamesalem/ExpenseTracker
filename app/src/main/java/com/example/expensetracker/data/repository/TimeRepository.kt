package com.example.expensetracker.data.repository

import com.example.expensetracker.data.model.TimeEntry
import kotlinx.coroutines.flow.Flow

interface TimeRepository {
    fun observeAllTimeEntries(): Flow<List<TimeEntry>>
    fun observeRunningEntry(): Flow<TimeEntry?>
    fun observeEntriesByDate(dateString: String): Flow<List<TimeEntry>>
    fun observeTotalSecondsForDate(dateString: String): Flow<Long?>

    suspend fun startTimer(title: String, category: String, isBillable: Boolean, hourlyRate: Double?): Long
    suspend fun stopTimer(id: Long)
    suspend fun saveTimeEntry(entry: TimeEntry): Long
    suspend fun deleteTimeEntry(id: Long)
    suspend fun convertToExpense(entry: TimeEntry, defaultCurrency: String): Long
}
