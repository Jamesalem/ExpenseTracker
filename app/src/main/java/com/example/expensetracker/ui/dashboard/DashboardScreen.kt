package com.example.expensetracker.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expensetracker.R
import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.util.CurrencyFormatter
import com.example.expensetracker.data.viewmodel.DashboardViewModel
import com.example.expensetracker.ui.budget.BudgetDialog
import com.example.expensetracker.ui.components.cards.BudgetCard
import com.example.expensetracker.ui.components.pickers.MonthPicker
import com.example.expensetracker.ui.navigation.Routes
import com.example.expensetracker.ui.theme.PieChartColors
import com.example.expensetracker.ui.theme.Shapes
import com.example.expensetracker.ui.theme.Typography
import com.example.expensetracker.ui.theme.progressColor
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by dashboardViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(dashboardViewModel.userMessage) {
        dashboardViewModel.userMessage.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.dashboard_title),
                        style = Typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.EXPENSE_LIST) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = stringResource(R.string.view_all_expenses)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            when (val state = uiState) {
                is DashboardViewModel.DashboardUiState.Loading -> {
                    Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is DashboardViewModel.DashboardUiState.Error -> {
                    Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    }
                }
                is DashboardViewModel.DashboardUiState.Success -> {
                    val expenses = state.expenses
                    val settings = state.settings
                    val month = state.selectedMonth
                    val periodKey = "${month.year}-${month.monthValue}"
                    
                    val totalSpent = expenses.filter { it.type == Expense.ExpenseType.EXPENSE }.sumOf { it.amount }
                    val currentBudget = state.budgets.firstOrNull { it.periodKey == periodKey }?.amount

                    MonthPicker(
                        yearMonth = month,
                        onYearMonthChange = { 
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            dashboardViewModel.updateSelectedMonth(it) 
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    BudgetCard(
                        budget = currentBudget,
                        totalSpent = totalSpent,
                        currencyCode = settings.defaultCurrency,
                        onEditClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            // In production, navigate to budget settings or show dialog
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    val spentByCategory = expenses
                        .filter { it.type == Expense.ExpenseType.EXPENSE }
                        .groupBy { it.category }
                        .mapValues { it.value.sumOf(Expense::amount) }
                        .toList()
                        .sortedByDescending { it.second }

                    SummaryRow(totalSpent, currentBudget, settings)
                    
                    CategoryBreakdownCard(
                        spentByCategory = spentByCategory,
                        currencyCode = settings.defaultCurrency,
                        totalSpent = totalSpent,
                        onSeeMoreClick = { /* show category insights */ }
                    )

                    SpendingChartCard(spentByCategory)
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(totalSpent: Double, budget: Double?, settings: com.example.expensetracker.data.model.AppSettings) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SummaryCard(
            title = stringResource(R.string.total_spent),
            value = CurrencyFormatter.format(totalSpent, settings.defaultCurrency),
            modifier = Modifier.weight(1f),
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        )
        val remaining = (budget ?: 0.0) - totalSpent
        SummaryCard(
            title = stringResource(R.string.remaining),
            value = CurrencyFormatter.format(remaining, settings.defaultCurrency),
            modifier = Modifier.weight(1f),
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            valueColor = if (remaining >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun SpendingChartCard(spentByCategory: List<Pair<String, Double>>) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth().height(320.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.monthly_distribution),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (spentByCategory.isNotEmpty()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize().padding(top = 8.dp),
                    factory = { ctx -> createPieChart(ctx, createPieEntries(spentByCategory), colorScheme) },
                    update = { chart ->
                        chart.data = createPieData(createPieEntries(spentByCategory), colorScheme)
                        chart.invalidate()
                    }
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.no_data_for_month),
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = Shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CategoryBreakdownCard(
    spentByCategory: List<Pair<String, Double>>,
    currencyCode: String,
    totalSpent: Double,
    onSeeMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleCategories = remember(spentByCategory) { spentByCategory.take(3) }
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.top_spending_categories),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (spentByCategory.size > 3) {
                    TextButton(onClick = onSeeMoreClick) {
                        Text(stringResource(R.string.details))
                    }
                }
            }
            if (spentByCategory.isEmpty()) {
                Text(
                    text = stringResource(R.string.start_logging_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                visibleCategories.forEach { (category, amount) ->
                    val progress = (amount / totalSpent.coerceAtLeast(1.0)).toFloat()
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(category, style = MaterialTheme.typography.bodyMedium)
                            Text("${"%.1f".format(java.util.Locale.getDefault(), progress * 100)}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = progressColor(progress),
                            trackColor = MaterialTheme.colorScheme.surface,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

private fun createPieEntries(spentByCategory: List<Pair<String, Double>>): List<PieEntry> =
    spentByCategory.map { (category, amount) -> PieEntry(amount.toFloat(), category) }

private fun createPieChart(
    context: android.content.Context,
    entries: List<PieEntry>,
    colorScheme: ColorScheme
): PieChart = PieChart(context).apply {
    setHoleColor(colorScheme.surface.toArgb())
    setTransparentCircleColor(colorScheme.surfaceVariant.toArgb())
    setEntryLabelColor(colorScheme.onSurface.toArgb())
    legend.apply {
        verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        orientation = Legend.LegendOrientation.HORIZONTAL
        textColor = colorScheme.onSurface.toArgb()
        isWordWrapEnabled = true
    }
    description.isEnabled = false
    setUsePercentValues(true)
    setDrawEntryLabels(false)
    animateY(800)
    data = createPieData(entries, colorScheme)
}

private fun createPieData(entries: List<PieEntry>, colorScheme: ColorScheme): PieData {
    val dataSet = PieDataSet(entries, "").apply {
        colors = PieChartColors.map { it.toArgb() }
        sliceSpace = 2f
        yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        valueTextColor = colorScheme.onSurface.toArgb()
    }
    return PieData(dataSet).apply {
        setValueFormatter(PercentFormatter())
        setValueTextSize(10f)
    }
}
