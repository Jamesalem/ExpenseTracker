// ui/theme/Color.kt
package com.example.expensetracker.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.absoluteValue

// World-class Royal Indigo & Emerald Cash Palette
val Primary      = Color(0xFF6366F1) // Royal Indigo
val PrimaryLight = Color(0xFF818CF8) // Indigo Soft
val PrimaryDark  = Color(0xFF4F46E5) // Deep Indigo

val Secondary      = Color(0xFF10B981) // Emerald Cash
val SecondaryLight = Color(0xFF34D399) // Mint Soft
val SecondaryDark  = Color(0xFF059669) // Forest Emerald

val AccentViolet   = Color(0xFF8B5CF6) // Violet Neon
val AccentRose     = Color(0xFFF43F5E) // Rose Coral

// Status
val Success = Color(0xFF10B981)
val Warning = Color(0xFFF59E0B)
val Error   = Color(0xFFEF4444)

// Neutral & Backgrounds
val Background      = Color(0xFFF8FAFC)
val Surface         = Color(0xFFFFFFFF)
val OnSurface       = Color(0xFF0F172A)
val SurfaceVariant  = Color(0xFFF1F5F9)

val DarkBackground     = Color(0xFF0F172A) // Slate Void
val DarkSurface        = Color(0xFF1E293B) // Dark Slate Card
val DarkSurfaceVariant = Color(0xFF334155) // Slate Border/Elevated
val DarkOnSurface      = Color(0xFFF8FAFC)

// Glassmorphism Strokes
val GlassBorderLight = Color(0x336366F1)
val GlassBorderDark  = Color(0x33818CF8)

// Functional Financial Colors
val IncomeColor  = Color(0xFF10B981)
val ExpenseColor = Color(0xFFEF4444)
val BudgetColor  = Color(0xFF6366F1)

// Chart palette
val ChartPalette = listOf(
    Primary, Secondary, AccentViolet, AccentRose,
    Color(0xFF3B82F6), Color(0xFF06B6D4),
    Color(0xFFF59E0B), Color(0xFFEC4899)
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
