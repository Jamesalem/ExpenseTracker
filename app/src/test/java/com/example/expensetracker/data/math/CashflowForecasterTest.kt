package com.example.expensetracker.data.math

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CashflowForecasterTest {

    @Test
    fun testCashflowForecasting_steadySavings() {
        val initialBalance = 5000.0
        // Net daily flow +50/day
        val historicalFlows = List(30) { 50.0 }
        val startDate = LocalDate.of(2026, 8, 1)

        val result = CashflowForecaster.forecastCashflow(
            currentBalance = initialBalance,
            historicalDailyNetFlows = historicalFlows,
            startDate = startDate,
            forecastHorizonDays = 30
        )

        assertEquals(30, result.forecastPoints.size)
        // With positive daily net flow, forecasted balance at day 30 should be higher than initial balance
        assertTrue(result.forecastedEndOfMonthBalance > initialBalance)
        assertTrue(result.estimatedRunwayDays >= 365)
        assertFalse(result.isRunwayCritical)
    }

    @Test
    fun testCashflowForecasting_burnRunway() {
        val initialBalance = 3000.0
        // Net daily flow -100/day
        val historicalFlows = List(30) { -100.0 }
        val startDate = LocalDate.of(2026, 8, 1)

        val result = CashflowForecaster.forecastCashflow(
            currentBalance = initialBalance,
            historicalDailyNetFlows = historicalFlows,
            startDate = startDate,
            forecastHorizonDays = 30
        )

        // 3000 / 100 = 30 days runway
        assertEquals(30, result.estimatedRunwayDays)
        assertTrue(result.isRunwayCritical)
    }
}
