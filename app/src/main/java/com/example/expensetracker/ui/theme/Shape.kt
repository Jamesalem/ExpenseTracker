// ui/theme/Shape.kt
package com.example.expensetracker.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(12.dp),
    large      = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

// Custom
val BottomSheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
val DialogShape      = RoundedCornerShape(16.dp)
val ChipShape        = RoundedCornerShape(50.dp)  // fixed to use dp
