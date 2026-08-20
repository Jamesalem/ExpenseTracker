package com.example.expensetracker.ui.components.cards

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.data.math.SafeSpendEngine
import com.example.expensetracker.data.util.CurrencyFormatter
import java.util.Locale

@Composable
fun SafeSpendPulseCard(
    result: SafeSpendEngine.SafeSpendResult,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    val statusColor = when (result.paceStatus) {
        SafeSpendEngine.PaceStatus.SAFE -> Color(0xFF10B981) // Emerald
        SafeSpendEngine.PaceStatus.ON_TRACK -> Color(0xFF3B82F6) // Blue
        SafeSpendEngine.PaceStatus.CAUTION -> Color(0xFFF59E0B) // Amber
        SafeSpendEngine.PaceStatus.OVER_BUDGET -> Color(0xFFEF4444) // Red
    }

    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val cardGradient = remember(statusColor, surfaceVariant) {
        Brush.linearGradient(
            listOf(
                statusColor.copy(alpha = 0.15f),
                surfaceVariant.copy(alpha = 0.6f)
            )
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardGradient)
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Header row
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
                                .background(statusColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Safe-to-Spend Today",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Status chip
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            result.paceStatus.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = statusColor
                            )
                        )
                    }
                }

                // Daily allowance hero value
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            CurrencyFormatter.format(result.dailyAllowance, currencyCode),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 32.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "${result.daysRemainingInMonth} days remaining in month",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (result.burnVelocity <= 1.0) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${String.format(Locale.getDefault(), "%.2f", result.burnVelocity)}x Velocity",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = statusColor
                            )
                        }
                        Text(
                            result.paceStatus.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                // Velocity progress bar
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val progress = (result.burnVelocity / 2.0).toFloat().coerceIn(0.05f, 1.0f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = statusColor,
                        trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        strokeCap = StrokeCap.Round
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Pace: 0.0x (Saver)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        Text("1.0x (Target)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        Text("2.0x+ (High)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}
