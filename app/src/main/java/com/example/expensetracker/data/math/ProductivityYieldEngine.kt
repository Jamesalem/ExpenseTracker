package com.example.expensetracker.data.math

import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.model.TimeEntry

/**
 * On-Device Freelance & Productivity Yield Optimization Engine.
 * Correlates tracked focus time entries with earned income to calculate
 * the true Effective Hourly Yield (EHY) across clients, categories, and projects.
 */
object ProductivityYieldEngine {

    data class CategoryYieldResult(
        val category: String,
        val totalHoursTracked: Double,
        val totalIncomeEarned: Double,
        val effectiveHourlyRate: Double,
        val billableHours: Double,
        val nonBillableHours: Double,
        val billableRatioPercent: Double
    )

    data class ProductivityYieldSummary(
        val totalTrackedHours: Double,
        val totalBillableIncome: Double,
        val overallEffectiveHourlyRate: Double,
        val categoryBreakdown: List<CategoryYieldResult>,
        val topYieldingCategory: String?,
        val lowestYieldingCategory: String?
    )

    /**
     * Analyzes time logs and related incomes to produce the productivity yield summary.
     */
    fun analyzeYield(
        timeEntries: List<TimeEntry>,
        incomeExpenses: List<Expense>
    ): ProductivityYieldSummary {
        val byCategory = timeEntries.groupBy { it.category }
        val categoryResults = mutableListOf<CategoryYieldResult>()

        var totalTrackedSeconds = 0L
        var totalDirectEarnings = 0.0

        for ((category, entries) in byCategory) {
            val totalSeconds = entries.sumOf { it.durationSeconds }
            val totalHours = totalSeconds / 3600.0
            totalTrackedSeconds += totalSeconds

            val billableEntries = entries.filter { it.isBillable }
            val billableSeconds = billableEntries.sumOf { it.durationSeconds }
            val billableHours = billableSeconds / 3600.0
            val nonBillableHours = totalHours - billableHours

            // Direct earnings recorded in time entries (hourlyRate * hours)
            val entryEarnings = billableEntries.sumOf { (it.durationSeconds / 3600.0) * (it.hourlyRate ?: 0.0) }

            // Plus any income logged under the same category name
            val matchedIncome = incomeExpenses
                .filter { it.type == Expense.ExpenseType.INCOME && it.category.equals(category, ignoreCase = true) }
                .sumOf { it.amount }

            val totalCategoryIncome = maxOf(entryEarnings, matchedIncome)
            totalDirectEarnings += totalCategoryIncome

            val ehy = if (totalHours > 0) totalCategoryIncome / totalHours else 0.0
            val billableRatio = if (totalHours > 0) (billableHours / totalHours) * 100.0 else 0.0

            categoryResults.add(
                CategoryYieldResult(
                    category = category,
                    totalHoursTracked = totalHours,
                    totalIncomeEarned = totalCategoryIncome,
                    effectiveHourlyRate = ehy,
                    billableHours = billableHours,
                    nonBillableHours = nonBillableHours,
                    billableRatioPercent = billableRatio
                )
            )
        }

        val sorted = categoryResults.sortedByDescending { it.effectiveHourlyRate }
        val totalOverallHours = totalTrackedSeconds / 3600.0
        val overallRate = if (totalOverallHours > 0) totalDirectEarnings / totalOverallHours else 0.0

        return ProductivityYieldSummary(
            totalTrackedHours = totalOverallHours,
            totalBillableIncome = totalDirectEarnings,
            overallEffectiveHourlyRate = overallRate,
            categoryBreakdown = sorted,
            topYieldingCategory = sorted.firstOrNull()?.category,
            lowestYieldingCategory = sorted.lastOrNull { it.totalHoursTracked > 0.5 }?.category
        )
    }
}
