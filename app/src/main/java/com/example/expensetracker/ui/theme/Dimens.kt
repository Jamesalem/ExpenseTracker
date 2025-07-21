// ui/theme/Dimens.kt
package com.example.expensetracker.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp

@Immutable
object Dimens {
    // Spacing
    val none      = 0.dp
    val extraSmall = 4.dp
    val small      = 8.dp
    val medium     = 16.dp
    val large      = 24.dp
    val extraLarge = 32.dp

    // Icon sizes
    val iconXS = 8.dp
    val iconS  = 16.dp
    val iconM  = 24.dp
    val iconL  = 32.dp
    val iconXL = 48.dp

    // Component heights
    val buttonHeight     = 48.dp
    val inputFieldHeight = 56.dp
    val smallIconButton  = 40.dp

    // Borders & elevation
    val borderThin   = 1.dp
    val borderMedium = 2.dp
    val elevationS   = 2.dp
    val elevationM   = 4.dp
    val elevationL   = 8.dp

    // Corners
    val cornerS = 4.dp
    val cornerM = 8.dp
    val cornerL = 16.dp
}
