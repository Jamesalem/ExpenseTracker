// ui/theme/ProgressIndicator.kt
package com.example.expensetracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Picks a color from our ExtendedColors based on progress [0f..1f].
 */
@Composable
fun progressColor(progress: Float): Color {
    val colors = MaterialTheme.extendedColors
    return when {
        progress < 0.5f -> colors.incomeColor
        progress < 0.8f -> colors.budgetColor
        else            -> colors.expenseColor
    }
}
