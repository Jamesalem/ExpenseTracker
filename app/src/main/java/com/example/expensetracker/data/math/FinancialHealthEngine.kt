package com.example.expensetracker.data.math

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * On-Device Multi-Criteria Financial Health Index Engine ($FHI \in [0, 100]$).
 * Evaluates user financial resilience across 4 core dimensions:
 * 1. Savings Rate Index (30%)
 * 2. Budget Velocity Adherence (30%)
 * 3. Emergency Runway Buffer (25%)
 * 4. Fixed Commitment Burden (15%)
 */
object FinancialHealthEngine {

    data class HealthScoreBreakdown(
        val totalScore: Int, // 0 to 100
        val rating: HealthRating,
        val savingsRateScore: Int, // 0 to 100
        val savingsRatePercent: Double,
        val budgetAdherenceScore: Int, // 0 to 100
        val runwayScore: Int, // 0 to 100
        val runwayMonths: Double,
        val fixedCostScore: Int, // 0 to 100
        val fixedCostPercent: Double,
        val primaryRecommendation: String,
        val actionableTips: List<String>
    )

    enum class HealthRating(val label: String, val colorHex: Long) {
        EXCELLENT("Exceptional", 0xFF10B981), // Emerald
        GOOD("Strong", 0xFF3B82F6),           // Blue
        FAIR("Fair", 0xFFF59E0B),             // Amber
        NEEDS_ATTENTION("Needs Work", 0xFFEF4444) // Red
    }

    /**
     * Computes the composite Financial Health Index.
     */
    fun computeHealthScore(
        monthlyIncome: Double,
        monthlyExpense: Double,
        monthlyBudget: Double,
        monthlyRecurringBills: Double,
        liquidSavings: Double
    ): HealthScoreBreakdown {
        val income = max(0.0, monthlyIncome)
        val expense = max(0.0, monthlyExpense)

        // 1. Savings Rate Score (Weight: 30%)
        // Benchmark: 20% savings = 60/100, 50%+ savings = 100/100
        val savingsRate = if (income > 0) (income - expense) / income else 0.0
        val savingsScore = when {
            income <= 0 -> 40
            savingsRate <= 0.0 -> max(0, (40 + (savingsRate * 100)).toInt())
            savingsRate >= 0.50 -> 100
            else -> (savingsRate / 0.50 * 100).toInt().coerceIn(0, 100)
        }

        // 2. Budget Adherence Score (Weight: 30%)
        // Benchmark: Within 0-100% of budget
        val adherenceScore = when {
            monthlyBudget <= 0 -> 70 // Neutral if no budget set
            expense <= monthlyBudget -> {
                val ratio = expense / monthlyBudget
                (100 - (ratio * 15)).toInt().coerceIn(70, 100)
            }
            else -> {
                val overRatio = (expense - monthlyBudget) / monthlyBudget
                max(0, (70 - (overRatio * 100)).toInt())
            }
        }

        // 3. Emergency Runway Score (Weight: 25%)
        // Benchmark: 6+ months runway = 100/100, 3 months = 60/100, <1 month = low
        val monthlyBurn = if (expense > 0) expense else monthlyBudget.coerceAtLeast(500.0)
        val runwayMonths = if (monthlyBurn > 0 && liquidSavings > 0) liquidSavings / monthlyBurn else 0.0
        val runwayScore = when {
            runwayMonths >= 6.0 -> 100
            runwayMonths <= 0.0 -> 10
            else -> (runwayMonths / 6.0 * 100).toInt().coerceIn(10, 100)
        }

        // 4. Fixed Commitment Score (Weight: 15%)
        // Benchmark: Fixed bills < 30% of income = 100, > 60% = critical
        val fixedRatio = if (income > 0) monthlyRecurringBills / income else 0.5
        val fixedScore = when {
            income <= 0 -> 50
            fixedRatio <= 0.25 -> 100
            fixedRatio >= 0.70 -> 10
            else -> ((0.70 - fixedRatio) / (0.70 - 0.25) * 100).toInt().coerceIn(10, 100)
        }

        // Weighted Total: 0.30 * S + 0.30 * B + 0.25 * R + 0.15 * F
        val composite = (0.30 * savingsScore) + (0.30 * adherenceScore) + (0.25 * runwayScore) + (0.15 * fixedScore)
        val finalScore = composite.toInt().coerceIn(0, 100)

        val rating = when {
            finalScore >= 80 -> HealthRating.EXCELLENT
            finalScore >= 65 -> HealthRating.GOOD
            finalScore >= 45 -> HealthRating.FAIR
            else -> HealthRating.NEEDS_ATTENTION
        }

        val tips = mutableListOf<String>()
        if (savingsRate < 0.20 && income > 0) {
            tips.add("Aim to save at least 20% of your income each month.")
        }
        if (monthlyBudget > 0 && expense > monthlyBudget) {
            tips.add("Spending is exceeding your monthly budget limit. Review discretionary expenses.")
        }
        if (runwayMonths < 3.0) {
            tips.add("Build an emergency fund covering 3 to 6 months of expenses.")
        }
        if (fixedRatio > 0.40 && income > 0) {
            tips.add("Recurring subscriptions and fixed bills consume >40% of income. Audit active subscriptions.")
        }
        if (tips.isEmpty()) {
            tips.add("Your finances are in excellent shape. Maintain your disciplined pace!")
        }

        val primaryRec = when {
            finalScore < 45 -> "Critical: Reduce spending velocity and build a basic emergency buffer."
            finalScore < 65 -> "Good progress: Optimize recurring bills and increase your savings rate."
            finalScore < 80 -> "Strong finances: Maintain your savings rate to expand your runway."
            else -> "Exceptional: Your financial discipline and runway are top tier!"
        }

        return HealthScoreBreakdown(
            totalScore = finalScore,
            rating = rating,
            savingsRateScore = savingsScore,
            savingsRatePercent = savingsRate * 100.0,
            budgetAdherenceScore = adherenceScore,
            runwayScore = runwayScore,
            runwayMonths = runwayMonths,
            fixedCostScore = fixedScore,
            fixedCostPercent = fixedRatio * 100.0,
            primaryRecommendation = primaryRec,
            actionableTips = tips
        )
    }
}
