// ui/theme/Theme.kt
package com.example.expensetracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary            = Primary,
    onPrimary          = Color.White,
    primaryContainer   = PrimaryLight,
    onPrimaryContainer = PrimaryDark,
    secondary          = Secondary,
    onSecondary        = Color.White,
    secondaryContainer = SecondaryLight,
    onSecondaryContainer = SecondaryDark,
    background         = Background,
    onBackground       = OnSurface,
    surface            = Surface,
    onSurface          = OnSurface,
    surfaceVariant     = Color(0xFFE9ECEF),
    onSurfaceVariant   = Color(0xFF6C757D),
    error              = Error,
    onError            = Color.White,
    outline            = Color(0xFFCED4DA)
)

private val DarkColors = darkColorScheme(
    primary            = PrimaryLight,
    onPrimary          = Color.Black,
    primaryContainer   = PrimaryDark,
    onPrimaryContainer = PrimaryLight,
    secondary          = SecondaryLight,
    onSecondary        = Color.Black,
    secondaryContainer = SecondaryDark,
    onSecondaryContainer = SecondaryLight,
    background         = DarkBackground,
    onBackground       = DarkOnSurface,
    surface            = DarkSurface,
    onSurface          = DarkOnSurface,
    surfaceVariant     = Color(0xFF2D3748),
    onSurfaceVariant   = Color(0xFFADB5BD),
    error              = Color(0xFFFF6B6B),
    onError            = Color.Black,
    outline            = Color(0xFF495057)
)

@Immutable
data class ExtendedColors(
    val incomeColor: Color,
    val expenseColor: Color,
    val budgetColor: Color,
    val chartColors: List<Color>
)

private val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        incomeColor  = IncomeColor,
        expenseColor = ExpenseColor,
        budgetColor  = BudgetColor,
        chartColors  = ChartPalette
    )
}

@Composable
fun ExpenseTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val ctx = LocalContext.current
    val useDynamic = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colors = when {
        useDynamic && darkTheme -> dynamicDarkColorScheme(ctx)
        useDynamic              -> dynamicLightColorScheme(ctx)
        darkTheme               -> DarkColors
        else                    -> LightColors
    }

    // lighten functional colors in dark mode
    val extended = ExtendedColors(
        incomeColor  = IncomeColor.adjustForTheme(darkTheme),
        expenseColor = ExpenseColor.adjustForTheme(darkTheme),
        budgetColor  = BudgetColor.adjustForTheme(darkTheme),
        chartColors  = ChartPalette
    )

    CompositionLocalProvider(LocalExtendedColors provides extended) {
        MaterialTheme(
            colorScheme = colors,
            typography  = Typography,
            shapes      = Shapes,
            content     = content
        )
    }
}

// Expose our extras
val MaterialTheme.extendedColors: ExtendedColors
    @Composable get() = LocalExtendedColors.current

val MaterialTheme.iconColor: Color
    @Composable get() = if (isSystemInDarkTheme()) Color.White else Color.Black

val MaterialTheme.secondaryIconColor: Color
    @Composable get() = colorScheme.onSurfaceVariant

private fun Color.adjustForTheme(dark: Boolean): Color {
    return if (!dark) this
    else copy(
        red   = (red   * 1.2f).coerceAtMost(1f),
        green = (green * 1.2f).coerceAtMost(1f),
        blue  = (blue  * 1.2f).coerceAtMost(1f)
    )
}
