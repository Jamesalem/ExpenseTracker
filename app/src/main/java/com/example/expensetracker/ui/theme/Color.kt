// ui/theme/Color.kt
package com.example.expensetracker.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.absoluteValue

// Primary palette
val Primary      = Color(0xFF4361EE)
val PrimaryLight = Color(0xFF6F86FF)
val PrimaryDark  = Color(0xFF0039BB)

// Secondary palette
val Secondary      = Color(0xFF4CC9F0)
val SecondaryLight = Color(0xFF8BFDFF)
val SecondaryDark  = Color(0xFF0098BD)

// Status
val Success = Color(0xFF4CAF50)
val Warning = Color(0xFFFFC107)
val Error   = Color(0xFFF44336)

// Neutral
val Background      = Color(0xFFF8F9FA)
val Surface         = Color(0xFFFFFFFF)
val OnSurface       = Color(0xFF212529)
val DarkBackground  = Color(0xFF121212)
val DarkSurface     = Color(0xFF1E1E1E)
val DarkOnSurface   = Color(0xFFE9ECEF)

// Functional
val IncomeColor  = Color(0xFF2E7D32)
val ExpenseColor = Color(0xFFC62828)
val BudgetColor  = Color(0xFF1565C0)

// Chart palette
val ChartPalette = listOf(
    Primary, Secondary, PrimaryLight, SecondaryLight,
    Color(0xFFFF6B6B), Color(0xFF4ECDC4),
    Color(0xFFFFD166), Color(0xFF6A0572)
)

/**
 * Deterministically pick one of ChartPalette based on a seed string
 */
fun generateRandomColor(seed: String): Color {
    val idx = seed.hashCode().absoluteValue % ChartPalette.size
    return ChartPalette[idx]
}

/**
 * Deterministically pick one of ChartPalette based on a category ID
 */
fun generateCategoryColor(id: Long): Color =
    generateRandomColor(id.toString())
