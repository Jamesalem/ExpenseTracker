package com.example.expensetracker.data.math

import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.model.Subscription
import kotlin.math.abs

/**
 * On-Device Anomaly & Price-Creep Detection Engine.
 * Uses robust non-parametric statistics (Median Absolute Deviation / Modified Z-Score)
 * to detect abnormal spending spikes and subscription price jumps.
 */
object AnomalyDetector {

    data class AnomalyResult(
        val expenseId: Long,
        val title: String,
        val category: String,
        val amount: Double,
        val medianCategorySpend: Double,
        val modifiedZScore: Double,
        val isAnomaly: Boolean,
        val reason: String
    )

    data class SubscriptionCreepAlert(
        val subscriptionId: Long,
        val title: String,
        val previousAmount: Double,
        val currentAmount: Double,
        val percentageIncrease: Double,
        val alertMessage: String
    )

    /**
     * Identifies unusual spending spikes in a list of expenses by category.
     *
     * @param expenses All recent expenses to analyze
     * @param threshold Modified Z-score threshold (standard is 3.0 or 3.5)
     */
    fun detectExpenseAnomalies(
        expenses: List<Expense>,
        threshold: Double = 3.0
    ): List<AnomalyResult> {
        val expenseItems = expenses.filter { it.type == Expense.ExpenseType.EXPENSE && it.amount > 0 }
        if (expenseItems.size < 5) return emptyList()

        val results = mutableListOf<AnomalyResult>()
        val byCategory = expenseItems.groupBy { it.category }

        for ((category, items) in byCategory) {
            if (items.size < 4) continue // Need enough samples per category

            val amounts = items.map { it.amount }.sorted()
            val median = calculateMedian(amounts)

            val absoluteDeviations = amounts.map { abs(it - median) }.sorted()
            val mad = calculateMedian(absoluteDeviations)

            if (mad <= 0.0001) continue // Avoid division by zero when amounts are identical

            for (expense in items) {
                // Modified Z-Score: M_i = 0.6745 * (x_i - median) / MAD
                val modifiedZ = (0.6745 * (expense.amount - median)) / mad
                if (modifiedZ >= threshold) {
                    val multiplier = String.format(java.util.Locale.getDefault(), "%.1f", expense.amount / median.coerceAtLeast(1.0))
                    results.add(
                        AnomalyResult(
                            expenseId = expense.id,
                            title = expense.title,
                            category = category,
                            amount = expense.amount,
                            medianCategorySpend = median,
                            modifiedZScore = modifiedZ,
                            isAnomaly = true,
                            reason = "Unusually large ${expense.category} expense (${multiplier}x higher than your usual ${String.format(java.util.Locale.getDefault(), "%.2f", median)})"
                        )
                    )
                }
            }
        }

        return results.sortedByDescending { it.modifiedZScore }
    }

    /**
     * Checks if a recurring subscription had an unexpected price hike.
     */
    fun checkSubscriptionCreep(
        currentSub: Subscription,
        previousPaidAmount: Double?
    ): SubscriptionCreepAlert? {
        if (previousPaidAmount == null || previousPaidAmount <= 0) return null
        if (currentSub.amount > previousPaidAmount) {
            val increase = currentSub.amount - previousPaidAmount
            val pct = (increase / previousPaidAmount) * 100.0
            if (pct >= 5.0) { // At least 5% increase
                return SubscriptionCreepAlert(
                    subscriptionId = currentSub.id,
                    title = currentSub.title,
                    previousAmount = previousPaidAmount,
                    currentAmount = currentSub.amount,
                    percentageIncrease = pct,
                    alertMessage = "'${currentSub.title}' increased by ${String.format(java.util.Locale.getDefault(), "%.1f", pct)}% (from ${String.format(java.util.Locale.getDefault(), "%.2f", previousPaidAmount)} to ${String.format(java.util.Locale.getDefault(), "%.2f", currentSub.amount)})"
                )
            }
        }
        return null
    }

    private fun calculateMedian(sortedList: List<Double>): Double {
        if (sortedList.isEmpty()) return 0.0
        val size = sortedList.size
        return if (size % 2 == 1) {
            sortedList[size / 2]
        } else {
            (sortedList[size / 2 - 1] + sortedList[size / 2]) / 2.0
        }
    }
}
