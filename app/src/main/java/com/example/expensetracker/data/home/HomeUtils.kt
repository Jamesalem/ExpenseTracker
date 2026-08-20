// data/home/HomeUtils.kt
package com.example.expensetracker.data.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Wallet
import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.util.DateFormatter
import com.example.expensetracker.ui.theme.generateCategoryColor
import java.util.Locale

object HomeUtils {
    fun getRecentTransactions(expenses: List<Expense>): List<Transaction> {
        // sort descending by date (LocalDate) then take top 6
        return expenses
            .sortedByDescending { it.date }
            .take(6)
            .map { exp ->
                val categoryName = exp.category
                Transaction(
                    id = exp.id.toString(),
                    title = exp.title,
                    amount = if (exp.type == Expense.ExpenseType.EXPENSE) -exp.amount else exp.amount,
                    date = DateFormatter.formatShortDate(exp.date),
                    categoryDisplay = CategoryDisplay(
                        name = categoryName,
                        icon = when (categoryName.lowercase(Locale.getDefault())) {
                            "food", "groceries", "food & dining" -> Icons.Filled.ShoppingCart
                            "income", "salary" -> Icons.Filled.AccountBalance
                            "utilities", "bills" -> Icons.Filled.Lightbulb
                            "dining", "coffee" -> Icons.Filled.Coffee
                            else -> Icons.Filled.Wallet
                        },
                        color = generateCategoryColor(categoryName.hashCode().toLong())
                    ),
                    isExpense = exp.type == Expense.ExpenseType.EXPENSE,
                    account = exp.account,
                    tags = exp.tags
                )
            }
    }
}