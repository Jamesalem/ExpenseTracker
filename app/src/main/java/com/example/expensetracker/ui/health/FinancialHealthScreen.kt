package com.example.expensetracker.ui.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expensetracker.data.math.FinancialHealthEngine
import com.example.expensetracker.data.viewmodel.DashboardViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialHealthScreen(
    navController: NavController,
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by dashboardViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Financial Health Hub", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is DashboardViewModel.DashboardUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is DashboardViewModel.DashboardUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is DashboardViewModel.DashboardUiState.Success -> {
                val health = state.healthScore
                val ratingColor = Color(health.rating.colorHex)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Hero Health Gauge
                    item {
                        HealthHeroCard(health = health, ratingColor = ratingColor)
                    }

                    // 4 Pillars Header
                    item {
                        Text(
                            "Core Financial Pillars",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Pillar 1: Savings Rate
                    item {
                        PillarDetailCard(
                            title = "Savings Rate Index",
                            score = health.savingsRateScore,
                            subtitle = "${String.format(Locale.getDefault(), "%.1f", health.savingsRatePercent)}% of income saved (Benchmark: ≥20%)",
                            icon = Icons.Default.Savings,
                            color = Color(0xFF10B981)
                        )
                    }

                    // Pillar 2: Budget Adherence
                    item {
                        PillarDetailCard(
                            title = "Budget Velocity & Control",
                            score = health.budgetAdherenceScore,
                            subtitle = "Pacing multiplier: ${String.format(Locale.getDefault(), "%.2f", state.safeSpendResult.burnVelocity)}x of target limit",
                            icon = Icons.Default.Speed,
                            color = Color(0xFF3B82F6)
                        )
                    }

                    // Pillar 3: Emergency Runway
                    item {
                        PillarDetailCard(
                            title = "Emergency Runway Buffer",
                            score = health.runwayScore,
                            subtitle = "${String.format(Locale.getDefault(), "%.1f", health.runwayMonths)} Months of liquid runway (Target: 3-6 Months)",
                            icon = Icons.Default.Timelapse,
                            color = Color(0xFFF59E0B)
                        )
                    }

                    // Pillar 4: Fixed Costs & Debt
                    item {
                        PillarDetailCard(
                            title = "Fixed Commitments & Bills",
                            score = health.fixedCostScore,
                            subtitle = "${String.format(Locale.getDefault(), "%.1f", health.fixedCostPercent)}% of income consumed by recurring subscriptions",
                            icon = Icons.Default.Wallet,
                            color = Color(0xFF8B5CF6)
                        )
                    }

                    // Actionable Recommendations Header
                    item {
                        Text(
                            "Actionable Intelligence",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(health.actionableTips) { tip ->
                        TipItemCard(tip)
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthHeroCard(
    health: FinancialHealthEngine.HealthScoreBreakdown,
    ratingColor: Color
) {
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val heroGradient = remember(ratingColor, surfaceVariant) {
        Brush.linearGradient(
            listOf(
                ratingColor.copy(alpha = 0.25f),
                surfaceVariant.copy(alpha = 0.6f)
            )
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(heroGradient)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                    CircularProgressIndicator(
                        progress = { health.totalScore / 100f },
                        modifier = Modifier.size(120.dp),
                        color = ratingColor,
                        trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        strokeWidth = 10.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${health.totalScore}",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 42.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "/ 100",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(ratingColor.copy(alpha = 0.2f))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        "${health.rating.label} Financial Standing",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = ratingColor
                        )
                    )
                }

                Text(
                    health.primaryRecommendation,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PillarDetailCard(
    title: String,
    score: Int,
    subtitle: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }
                Text(
                    "$score / 100",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold, color = color)
                )
            }

            LinearProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = color,
                trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                strokeCap = StrokeCap.Round
            )

            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TipItemCard(tip: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                tip,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
