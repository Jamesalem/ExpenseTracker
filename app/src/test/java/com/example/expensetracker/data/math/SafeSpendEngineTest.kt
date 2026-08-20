package com.example.expensetracker.data.math

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SafeSpendEngineTest {

    @Test
    fun testSafeSpendCalculation_onTrack() {
        val today = LocalDate.of(2026, 8, 15) // Day 15 of 31 (17 days remaining including today)
        val result = SafeSpendEngine.calculateSafeSpend(
            totalBudget = 3100.0,
            currentSpent = 1500.0,
            pendingFixedBills = 0.0,
            dailySpendHistory = listOf(100.0, 100.0, 100.0, 100.0),
            currentDate = today
        )

        // remaining = 3100 - 1500 = 1600. Days remaining = 17. Daily allowance = 1600 / 17 ~ 94.12
        assertEquals(17, result.daysRemainingInMonth)
        assertTrue(result.dailyAllowance > 90.0 && result.dailyAllowance < 100.0)
        assertEquals(SafeSpendEngine.PaceStatus.ON_TRACK, result.paceStatus)
    }

    @Test
    fun testSafeSpendCalculation_overBudget() {
        val today = LocalDate.of(2026, 8, 15)
        val result = SafeSpendEngine.calculateSafeSpend(
            totalBudget = 1000.0,
            currentSpent = 1200.0,
            pendingFixedBills = 100.0,
            dailySpendHistory = emptyList(),
            currentDate = today
        )

        assertEquals(0.0, result.dailyAllowance, 0.001)
        assertEquals(SafeSpendEngine.PaceStatus.OVER_BUDGET, result.paceStatus)
        assertEquals(1.0, result.overrunProbability, 0.001)
    }

    @Test
    fun testSafeSpendCalculation_safePace() {
        val today = LocalDate.of(2026, 8, 15)
        val result = SafeSpendEngine.calculateSafeSpend(
            totalBudget = 3000.0,
            currentSpent = 500.0,
            pendingFixedBills = 200.0,
            dailySpendHistory = listOf(30.0, 30.0, 40.0),
            currentDate = today
        )

        assertEquals(SafeSpendEngine.PaceStatus.SAFE, result.paceStatus)
        assertTrue(result.burnVelocity < 0.8)
    }
}
