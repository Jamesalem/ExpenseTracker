// data/home/Transaction.kt
package com.example.expensetracker.data.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Transaction(
    val id: String,
    val title: String,
    val amount: Double,
    val date: String,
    val categoryDisplay: CategoryDisplay, // UPDATED: Use CategoryDisplay
    val isExpense: Boolean
)

// Represents category data for UI display (icon and color)
data class CategoryDisplay(
    val name: String,
    val icon: ImageVector, // Icon for UI
    val color: Color       // Color for UI
)