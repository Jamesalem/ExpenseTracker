package com.example.expensetracker.data.repository

import com.example.expensetracker.data.dao.ExpenseDao
import com.example.expensetracker.data.dao.TimeEntryDao
import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.model.TimeEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeRepositoryImpl @Inject constructor(
    private val timeEntryDao: TimeEntryDao,
    private val expenseDao: ExpenseDao
) : TimeRepository {

    override fun observeAllTimeEntries(): Flow<List<TimeEntry>> =
        timeEntryDao.observeAll()

    override fun observeRunningEntry(): Flow<TimeEntry?> =
        timeEntryDao.observeRunningEntry()

    override fun observeEntriesByDate(dateString: String): Flow<List<TimeEntry>> =
        timeEntryDao.observeByDate(dateString)

    override fun observeTotalSecondsForDate(dateString: String): Flow<Long?> =
        timeEntryDao.observeTotalSecondsForDate(dateString)

    override suspend fun startTimer(
        title: String,
        category: String,
        isBillable: Boolean,
        hourlyRate: Double?
    ): Long {
        val running = timeEntryDao.getRunningEntry()
        if (running != null) {
            stopTimer(running.id)
        }

        val now = System.currentTimeMillis()
        val dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val entry = TimeEntry(
            title = title.ifBlank { "Task Session" },
            category = category,
            startTimeMillis = now,
            endTimeMillis = null,
            durationSeconds = 0L,
            isRunning = true,
            isBillable = isBillable,
            hourlyRate = hourlyRate,
            dateString = dateStr
        )
        return timeEntryDao.insert(entry)
    }

    override suspend fun stopTimer(id: Long) {
        val entry = timeEntryDao.getById(id) ?: return
        if (!entry.isRunning) return

        val now = System.currentTimeMillis()
        val elapsedSec = ((now - entry.startTimeMillis) / 1000).coerceAtLeast(0)
        val updated = entry.copy(
            endTimeMillis = now,
            durationSeconds = elapsedSec,
            isRunning = false
        )
        timeEntryDao.update(updated)
    }

    override suspend fun saveTimeEntry(entry: TimeEntry): Long {
        return if (entry.id == 0L) {
            timeEntryDao.insert(entry)
        } else {
            timeEntryDao.update(entry)
            entry.id
        }
    }

    override suspend fun deleteTimeEntry(id: Long) {
        timeEntryDao.deleteById(id)
    }

    override suspend fun convertToExpense(entry: TimeEntry, defaultCurrency: String): Long {
        val hours = entry.durationSeconds / 3600.0
        val amount = if (entry.hourlyRate != null && entry.hourlyRate > 0) {
            hours * entry.hourlyRate
        } else {
            0.0
        }

        val expense = Expense(
            title = "Time Log: ${entry.title}",
            amount = (amount * 100).toLong() / 100.0,
            date = LocalDate.now(),
            category = entry.category,
            note = "Tracked time: ${entry.durationSeconds / 60} minutes",
            type = Expense.ExpenseType.INCOME,
            currencyCode = defaultCurrency
        )
        val expenseId = expenseDao.insert(expense)
        val updatedEntry = entry.copy(associatedExpenseId = expenseId)
        timeEntryDao.update(updatedEntry)
        return expenseId
    }
}
