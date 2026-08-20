package com.example.expensetracker.data.math

import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.model.TimeEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ProductivityYieldEngineTest {

    @Test
    fun testProductivityYield_calculatesEffectiveHourlyRate() {
        val timeEntries = listOf(
            TimeEntry(id = 1, title = "Client Project A", category = "Consulting", durationSeconds = 7200, isBillable = true, hourlyRate = 100.0, isRunning = false, startTimeMillis = 0L, dateString = "2026-08-15"),
            TimeEntry(id = 2, title = "Admin & emails", category = "Consulting", durationSeconds = 3600, isBillable = false, hourlyRate = 0.0, isRunning = false, startTimeMillis = 0L, dateString = "2026-08-15")
        )
        // Total time = 3 hours. Billable earnings = 2 * 100 = 200. EHY = 200 / 3 = 66.67/hr

        val summary = ProductivityYieldEngine.analyzeYield(
            timeEntries = timeEntries,
            incomeExpenses = emptyList()
        )

        assertEquals(3.0, summary.totalTrackedHours, 0.01)
        assertEquals(200.0, summary.totalBillableIncome, 0.01)
        assertEquals(66.67, summary.overallEffectiveHourlyRate, 0.05)
        assertEquals("Consulting", summary.topYieldingCategory)
    }
}
