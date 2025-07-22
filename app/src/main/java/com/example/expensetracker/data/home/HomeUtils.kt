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
import com.example.expensetracker.ui.theme.generateCategoryColor // Import generateCategoryColor
import java.util.Locale

object HomeUtils {
    fun getRecentTransactions(expenses: List<Expense>): List<Transaction> {
        // sort descending by date (LocalDate) then take top 4
        return expenses
            .sortedByDescending { it.date }
            .take(4)
            .map { exp ->
                val categoryName = exp.category // Get category name from expense
                Transaction(
                    id = exp.id.toString(),
                    title = exp.title,
                    amount = if (exp.type == Expense.ExpenseType.EXPENSE) -exp.amount else exp.amount,
                    date = DateFormatter.formatShortDate(exp.date), // Use DateFormatter for LocalDate
                    categoryDisplay = CategoryDisplay( // Use CategoryDisplay
                        name = categoryName,
                        icon = when (categoryName.lowercase(Locale.getDefault())) {
                            "food" -> Icons.Filled.ShoppingCart
                            "income" -> Icons.Filled.AccountBalance
                            "utilities" -> Icons.Filled.Lightbulb
                            "dining" -> Icons.Filled.Coffee
                            else -> Icons.Filled.Wallet // Default icon
                        },
                        color = generateCategoryColor(categoryName.hashCode().toLong()) // Consistent color generation
                    ),
                    isExpense = exp.type == Expense.ExpenseType.EXPENSE
                )
            }
    }
}