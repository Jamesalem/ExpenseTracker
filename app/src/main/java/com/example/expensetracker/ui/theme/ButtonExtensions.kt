// ui/theme/ButtonExtensions.kt
package com.example.expensetracker.ui.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MaterialTheme.primaryButtonColors(): ButtonColors =
    ButtonDefaults.buttonColors(
        containerColor = colorScheme.primary,
        contentColor = colorScheme.onPrimary
    )

@Composable
fun MaterialTheme.secondaryButtonColors(): ButtonColors =
    ButtonDefaults.buttonColors(
        containerColor = colorScheme.surfaceVariant,
        contentColor = colorScheme.onSurfaceVariant
    )

@Composable
fun MaterialTheme.customButtonColors(
    containerColor: Color,
    contentColor: Color
): ButtonColors =
    ButtonDefaults.buttonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = containerColor.copy(alpha = 0.38f),
        disabledContentColor = contentColor.copy(alpha = 0.38f)
    )
