package com.example.expensetracker.data.math

import java.time.LocalDate
import kotlin.math.max

/**
 * On-Device Cash Flow & Runway Forecasting Engine.
 * Implements Holt-Winters additive exponential smoothing and trend regression
 * to forecast daily balances and financial runway over 30, 60, and 90 days.
 */
object CashflowForecaster {

    data class DailyForecastPoint(
        val date: LocalDate,
        val forecastedNetFlow: Double,
        val forecastedBalance: Double,
        val lowerBound: Double,
        val upperBound: Double
    )

    data class CashflowForecastResult(
        val startingBalance: Double,
        val forecastedEndOfMonthBalance: Double,
        val estimatedRunwayDays: Int,
        val dailyBurnRate: Double,
        val forecastPoints: List<DailyForecastPoint>,
        val isRunwayCritical: Boolean
    )

    /**
     * Projects daily balance and runway for [forecastHorizonDays] into the future.
     *
     * @param currentBalance Current net liquid funds
     * @param historicalDailyNetFlows Chronological list of past daily net flows (Income - Expense)
     * @param startDate Forecast start date (usually tomorrow or today)
     * @param forecastHorizonDays Number of days to project (default 30)
     */
    fun forecastCashflow(
        currentBalance: Double,
        historicalDailyNetFlows: List<Double>,
        startDate: LocalDate = LocalDate.now(),
        forecastHorizonDays: Int = 30
    ): CashflowForecastResult {
        if (historicalDailyNetFlows.isEmpty()) {
            val emptyPoints = (0 until forecastHorizonDays).map { offset ->
                val date = startDate.plusDays(offset.toLong())
                DailyForecastPoint(
                    date = date,
                    forecastedNetFlow = 0.0,
                    forecastedBalance = currentBalance,
                    lowerBound = currentBalance,
                    upperBound = currentBalance
                )
            }
            return CashflowForecastResult(
                startingBalance = currentBalance,
                forecastedEndOfMonthBalance = currentBalance,
                estimatedRunwayDays = if (currentBalance > 0) 365 else 0,
                dailyBurnRate = 0.0,
                forecastPoints = emptyPoints,
                isRunwayCritical = currentBalance <= 0
            )
        }

        // Fit Holt-Winters / Double Exponential Smoothing:
        // alpha: level smoothing, beta: trend smoothing
        val alpha = 0.25
        val beta = 0.10

        var level = historicalDailyNetFlows.first()
        var trend = if (historicalDailyNetFlows.size > 1) {
            historicalDailyNetFlows[1] - historicalDailyNetFlows[0]
        } else 0.0

        for (i in 1 until historicalDailyNetFlows.size) {
            val y = historicalDailyNetFlows[i]
            val lastLevel = level
            level = alpha * y + (1 - alpha) * (level + trend)
            trend = beta * (level - lastLevel) + (1 - beta) * trend
        }

        // Estimate residual variance for prediction interval bands
        val meanFlow = historicalDailyNetFlows.average()
        val variance = historicalDailyNetFlows.sumOf { (it - meanFlow) * (it - meanFlow) } / max(1, historicalDailyNetFlows.size - 1)
        val stdDev = kotlin.math.sqrt(variance)

        // Generate forecasts
        var runningBalance = currentBalance
        val points = mutableListOf<DailyForecastPoint>()

        for (h in 1..forecastHorizonDays) {
            val date = startDate.plusDays((h - 1).toLong())
            val predictedFlow = level + (h * trend)
            runningBalance += predictedFlow

            // Prediction interval widens with sqrt(h)
            val errorMargin = 1.96 * stdDev * kotlin.math.sqrt(h.toDouble())

            points.add(
                DailyForecastPoint(
                    date = date,
                    forecastedNetFlow = predictedFlow,
                    forecastedBalance = runningBalance,
                    lowerBound = runningBalance - errorMargin,
                    upperBound = runningBalance + errorMargin
                )
            )
        }

        // Calculate Daily Burn Rate (mean negative flow)
        val expensesOnly = historicalDailyNetFlows.filter { it < 0 }.map { kotlin.math.abs(it) }
        val dailyBurnRate = if (expensesOnly.isNotEmpty()) expensesOnly.average() else 0.0

        val estimatedRunway = if (dailyBurnRate > 0 && currentBalance > 0) {
            (currentBalance / dailyBurnRate).toInt()
        } else if (currentBalance > 0) {
            999 // Self-sustaining or no expenses
        } else {
            0
        }

        val eomBalance = points.lastOrNull()?.forecastedBalance ?: currentBalance
        val isCritical = estimatedRunway <= 30 || currentBalance < 0

        return CashflowForecastResult(
            startingBalance = currentBalance,
            forecastedEndOfMonthBalance = eomBalance,
            estimatedRunwayDays = estimatedRunway,
            dailyBurnRate = dailyBurnRate,
            forecastPoints = points,
            isRunwayCritical = isCritical
        )
    }
}
