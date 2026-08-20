package com.example.expensetracker.data.math

import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Mathematical Engine for Dynamic Safe-to-Spend Velocity & Overrun Probability.
 * Provides on-device real-time financial pacing intelligence.
 */
object SafeSpendEngine {

    data class SafeSpendResult(
        val dailyAllowance: Double,
        val totalSpentSoFar: Double,
        val remainingBudget: Double,
        val burnVelocity: Double, // < 1.0 = saving pace, 1.0 = nominal, > 1.0 = overspending pace
        val overrunProbability: Double, // 0.0 to 1.0 (0% to 100%)
        val paceStatus: PaceStatus,
        val projectedMonthEndSpend: Double,
        val daysRemainingInMonth: Int,
        val totalDaysInMonth: Int
    )

    enum class PaceStatus(val label: String, val description: String) {
        SAFE("Safe Pace", "You are spending below your budget velocity."),
        ON_TRACK("On Track", "Your spending pace matches your target budget."),
        CAUTION("Caution", "Spending velocity is elevated. Consider slowing down."),
        OVER_BUDGET("Over Budget", "You have exceeded your target monthly budget.")
    }

    /**
     * Calculates the dynamic daily safe-to-spend allowance and velocity.
     *
     * @param totalBudget Monthly target budget
     * @param currentSpent Total spent in the current month to date
     * @param pendingFixedBills Sum of recurring bills still due this month
     * @param dailySpendHistory List of daily spend amounts this month for variance estimation
     * @param currentDate Current date (defaults to LocalDate.now())
     */
    fun calculateSafeSpend(
        totalBudget: Double,
        currentSpent: Double,
        pendingFixedBills: Double = 0.0,
        dailySpendHistory: List<Double> = emptyList(),
        currentDate: LocalDate = LocalDate.now()
    ): SafeSpendResult {
        val yearMonth = YearMonth.from(currentDate)
        val totalDays = yearMonth.lengthOfMonth()
        val currentDay = currentDate.dayOfMonth
        val daysRemaining = max(1, totalDays - currentDay + 1)

        val remainingBudget = max(0.0, totalBudget - currentSpent)
        val discretionaryPool = max(0.0, totalBudget - currentSpent - pendingFixedBills)
        val dailyAllowance = if (daysRemaining > 0) discretionaryPool / daysRemaining else 0.0

        // Velocity Calculation: V = (Spent / currentDay) / (Budget / totalDays)
        val expectedPacePerDay = if (totalDays > 0 && totalBudget > 0) totalBudget / totalDays else 1.0
        val actualPacePerDay = if (currentDay > 0) currentSpent / currentDay else 0.0
        val burnVelocity = if (expectedPacePerDay > 0) actualPacePerDay / expectedPacePerDay else 1.0

        // Month-End Projection: Current Spent + (Days Remaining * Mean Daily Variable Spend)
        val meanDailySpend = if (dailySpendHistory.isNotEmpty()) {
            dailySpendHistory.average()
        } else {
            actualPacePerDay
        }
        val projectedMonthEnd = currentSpent + ((totalDays - currentDay) * meanDailySpend) + pendingFixedBills

        // Variance & Gaussian Overrun Probability
        val overrunProbability = if (totalBudget <= 0) {
            0.0
        } else if (currentSpent >= totalBudget) {
            1.0
        } else {
            val variance = if (dailySpendHistory.size >= 3) {
                val mean = dailySpendHistory.average()
                dailySpendHistory.sumOf { (it - mean) * (it - mean) } / (dailySpendHistory.size - 1)
            } else {
                max(10.0, (meanDailySpend * 0.4) * (meanDailySpend * 0.4))
            }
            val stdDevRemaining = sqrt(variance * max(1, totalDays - currentDay))
            if (stdDevRemaining > 0) {
                val zScore = (totalBudget - projectedMonthEnd) / stdDevRemaining
                // Normal CDF approximation (Abramowitz & Stegun approximation)
                val cdf = normalCdf(zScore)
                (1.0 - cdf).coerceIn(0.0, 1.0)
            } else {
                if (projectedMonthEnd > totalBudget) 1.0 else 0.0
            }
        }

        val paceStatus = when {
            totalBudget > 0 && currentSpent >= totalBudget -> PaceStatus.OVER_BUDGET
            burnVelocity > 1.25 || overrunProbability > 0.70 -> PaceStatus.CAUTION
            burnVelocity > 0.90 -> PaceStatus.ON_TRACK
            else -> PaceStatus.SAFE
        }

        return SafeSpendResult(
            dailyAllowance = dailyAllowance,
            totalSpentSoFar = currentSpent,
            remainingBudget = remainingBudget,
            burnVelocity = burnVelocity,
            overrunProbability = overrunProbability,
            paceStatus = paceStatus,
            projectedMonthEndSpend = projectedMonthEnd,
            daysRemainingInMonth = daysRemaining,
            totalDaysInMonth = totalDays
        )
    }

    /**
     * Standard Normal Cumulative Distribution Function approximation.
     */
    private fun normalCdf(z: Double): Double {
        if (z < -8.0) return 0.0
        if (z > 8.0) return 1.0

        val b1 = 0.319381530
        val b2 = -0.356563782
        val b3 = 1.781477937
        val b4 = -1.821255978
        val b5 = 1.330274429
        val p = 0.2316419
        val c = 0.3989422804014327 // 1 / sqrt(2 * PI)

        val absZ = abs(z)
        val t = 1.0 / (1.0 + p * absZ)
        val poly = t * (b1 + t * (b2 + t * (b3 + t * (b4 + t * b5))))
        val pdf = c * kotlin.math.exp(-0.5 * absZ * absZ)
        val cdfAbs = 1.0 - pdf * poly

        return if (z >= 0.0) cdfAbs else 1.0 - cdfAbs
    }
}
