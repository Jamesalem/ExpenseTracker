package com.example.expensetracker.data.math

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialHealthEngineTest {

    @Test
    fun testFinancialHealth_exceptionalStanding() {
        val breakdown = FinancialHealthEngine.computeHealthScore(
            monthlyIncome = 10000.0,
            monthlyExpense = 4000.0,
            monthlyBudget = 5000.0,
            monthlyRecurringBills = 1000.0,
            liquidSavings = 30000.0 // 7.5 months runway
        )

        assertTrue(breakdown.totalScore >= 80)
        assertEquals(FinancialHealthEngine.HealthRating.EXCELLENT, breakdown.rating)
        assertEquals(100, breakdown.runwayScore)
        assertEquals(100, breakdown.savingsRateScore)
    }

    @Test
    fun testFinancialHealth_criticalStanding() {
        val breakdown = FinancialHealthEngine.computeHealthScore(
            monthlyIncome = 3000.0,
            monthlyExpense = 3500.0,
            monthlyBudget = 2500.0,
            monthlyRecurringBills = 1800.0,
            liquidSavings = 200.0
        )

        assertTrue(breakdown.totalScore < 40)
        assertEquals(FinancialHealthEngine.HealthRating.NEEDS_ATTENTION, breakdown.rating)
        assertTrue(breakdown.actionableTips.isNotEmpty())
    }
}
