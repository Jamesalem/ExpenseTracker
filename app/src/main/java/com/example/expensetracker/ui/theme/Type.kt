// ui/theme/Type.kt
package com.example.expensetracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.expensetracker.R

val Inter = FontFamily(
    Font(R.font.inter_light, FontWeight.Light),
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = Inter,
        fontWeight  = FontWeight.Light,
        fontSize    = 57.sp,
        lineHeight  = 64.sp
    ),
    displayMedium = TextStyle(
        fontFamily = Inter,
        fontWeight  = FontWeight.Light,
        fontSize    = 45.sp,
        lineHeight  = 52.sp
    ),
    displaySmall = TextStyle(
        fontFamily = Inter,
        fontWeight  = FontWeight.Normal,
        fontSize    = 36.sp,
        lineHeight  = 44.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Inter,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 32.sp,
        lineHeight  = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Inter,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 28.sp,
        lineHeight  = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Inter,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 24.sp,
        lineHeight  = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Inter,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 22.sp,
        lineHeight  = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Inter,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 18.sp,
        lineHeight  = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Inter,
        fontWeight  = FontWeight.Medium,
        fontSize    = 14.sp,
        lineHeight  = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight  = FontWeight.Normal,
        fontSize    = 16.sp,
        lineHeight  = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight  = FontWeight.Normal,
        fontSize    = 14.sp,
        lineHeight  = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight  = FontWeight.Normal,
        fontSize    = 12.sp,
        lineHeight  = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Inter,
        fontWeight  = FontWeight.Medium,
        fontSize    = 14.sp,
        lineHeight  = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Inter,
        fontWeight  = FontWeight.Medium,
        fontSize    = 12.sp,
        lineHeight  = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Inter,
        fontWeight  = FontWeight.Medium,
        fontSize    = 10.sp,
        lineHeight  = 16.sp
    )
)

// Utility text styles for common patterns
val Typography.cardTitle: TextStyle
    get() = TextStyle(
        fontFamily = Inter,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 18.sp,
        lineHeight  = 24.sp
    )

val Typography.cardSubtitle: TextStyle
    get() = TextStyle(
        fontFamily = Inter,
        fontWeight  = FontWeight.Medium,
        fontSize    = 14.sp,
        lineHeight  = 20.sp
    )

val Typography.amountLarge: TextStyle
    get() = TextStyle(
        fontFamily = Inter,
        fontWeight  = FontWeight.Bold,
        fontSize    = 20.sp,
        lineHeight  = 28.sp
    )
