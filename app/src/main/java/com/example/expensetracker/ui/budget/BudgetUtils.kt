// ui/budget/BudgetUtils.kt
package com.example.expensetracker.ui.budget

import com.example.expensetracker.data.model.Budget
import com.example.expensetracker.data.model.Expense
import java.time.YearMonth
import java.time.format.DateTimeFormatter

object BudgetUtils {
    private val periodKeyFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

    /** Returns "YYYY-MM" for the current date. */
    fun getCurrentPeriodKey(): String =
        YearMonth.now().format(periodKeyFormatter)

    /** Sum of all EXPENSE‑type amounts. */
    fun calculateTotalSpent(expenses: List<Expense>): Double =
        expenses
            .filter { it.type == Expense.ExpenseType.EXPENSE }
            .sumOf { it.amount }

    /** Find the budget whose periodKey matches the current month. */
    fun findCurrentBudget(budgets: List<Budget>): Budget? =
        budgets.firstOrNull { it.periodKey == getCurrentPeriodKey() }

    /**
     * Turn a periodKey "yyyy-MM" into a user‑friendly "MMMM yyyy".
     * If parsing fails, just return the raw key.
     */
    fun getPeriodDisplayName(periodKey: String): String {
        return try {
            val ym = YearMonth.parse(periodKey, periodKeyFormatter)
            ym.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        } catch (e: Exception) {
            periodKey
        }
    }
}
