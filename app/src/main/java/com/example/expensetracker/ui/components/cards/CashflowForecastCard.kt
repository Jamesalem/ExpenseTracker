package com.example.expensetracker.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.math.CashflowForecaster
import com.example.expensetracker.data.util.CurrencyFormatter

@Composable
fun CashflowForecastCard(
    forecast: CashflowForecaster.CashflowForecastResult,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    val runwayColor = when {
        forecast.estimatedRunwayDays >= 180 -> Color(0xFF10B981) // 6+ months
        forecast.estimatedRunwayDays >= 90 -> Color(0xFF3B82F6)  // 3+ months
        forecast.estimatedRunwayDays >= 30 -> Color(0xFFF59E0B)  // 1-3 months
        else -> Color(0xFFEF4444)                               // < 30 days
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoGraph,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "30-Day Cash Flow Forecast",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                if (forecast.isRunwayCritical) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Low Runway", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFEF4444), fontWeight = FontWeight.Bold))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // EOM Projected Balance
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            "End-of-Month Est.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            CurrencyFormatter.format(forecast.forecastedEndOfMonthBalance, currencyCode),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = if (forecast.forecastedEndOfMonthBalance >= 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Estimated Runway
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            "Financial Runway",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (forecast.estimatedRunwayDays >= 365) "12+ Months" else "${forecast.estimatedRunwayDays} Days",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = runwayColor
                            )
                        )
                    }
                }
            }

            Text(
                "Holt-Winters statistical model trained on your past net flow trends. Projected with 95% confidence intervals.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
