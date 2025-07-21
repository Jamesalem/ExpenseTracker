// ui/dashboard/DashboardScreen.kt
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
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expensetracker.R
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.util.CurrencyFormatter
import com.example.expensetracker.data.viewmodel.BudgetViewModel
import com.example.expensetracker.data.viewmodel.ExpenseViewModel
import com.example.expensetracker.data.viewmodel.SettingsViewModel
import com.example.expensetracker.ui.budget.BudgetDialog
import com.example.expensetracker.ui.components.cards.BudgetCard
import com.example.expensetracker.ui.components.pickers.MonthPicker
import com.example.expensetracker.ui.theme.PieChartColors
import com.example.expensetracker.ui.theme.progressColor
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import java.time.YearMonth
import java.time.ZoneId
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    expenseViewModel: ExpenseViewModel = hiltViewModel(),
    budgetViewModel: BudgetViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val expenses by expenseViewModel.expenses.collectAsState(emptyList())
    val budgetsState by budgetViewModel.uiState.collectAsState()
    val settings by settingsViewModel.appSettings.collectAsState(initial = AppSettings())

    // Extract the list of budgets when in Success state
    val budgets = remember(budgetsState) {
        (budgetsState as? BudgetViewModel.BudgetUiState.Success)?.budgets.orEmpty()
    }

    // Month selector state
    var selectedYearMonth by remember { mutableStateOf(YearMonth.now()) }
    val periodKey = remember(selectedYearMonth) {
        "${selectedYearMonth.year}-${selectedYearMonth.monthValue}"
    }

    // Filter this month's expenses
    val monthExpenses = remember(expenses, selectedYearMonth) {
        expenses.filter { exp ->
            // Convert LocalDate to epoch millis, then to java.util.Date
            val millis = exp.date
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            val cal = Calendar.getInstance().apply { time = Date(millis) }
            cal.get(Calendar.YEAR) == selectedYearMonth.year &&
                    (cal.get(Calendar.MONTH) + 1) == selectedYearMonth.monthValue
        }
    }

    // Current budget & total spent
    val currentBudget = remember(budgets, periodKey) {
        budgets.firstOrNull { it.periodKey == periodKey }?.amount
    }
    val totalSpent = remember(monthExpenses) { monthExpenses.sumOf { it.amount } }

    // Dialog states
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showCategoryInsights by remember { mutableStateOf(false) }

    // Category breakdown
    val spentByCategory = remember(monthExpenses) {
        monthExpenses
            .groupBy { it.category }
            .mapValues { it.value.sumOf(Expense::amount) }
            .toList()
            .sortedByDescending { it.second }
    }

    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.dashboard_title),
                        style = typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate("expenses") }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = stringResource(R.string.view_all_expenses)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Month picker
            MonthPicker(
                yearMonth = selectedYearMonth,
                onYearMonthChange = { selectedYearMonth = it },
                modifier = Modifier.fillMaxWidth()
            )

            // Budget summary card
            BudgetCard(
                budget = currentBudget,
                totalSpent = totalSpent,
                currencyCode = settings.defaultCurrency,
                onEditClick = { showBudgetDialog = true },
                modifier = Modifier.fillMaxWidth()
            )

            // Spending summary row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryCard(
                    title = stringResource(R.string.total_spent),
                    value = CurrencyFormatter.format(totalSpent, settings.defaultCurrency),
                    modifier = Modifier.weight(1f),
                    backgroundColor = colorScheme.surfaceVariant
                )

                SummaryCard(
                    title = stringResource(R.string.remaining),
                    value = CurrencyFormatter.format(
                        (currentBudget ?: 0.0) - totalSpent,
                        settings.defaultCurrency
                    ),
                    modifier = Modifier.weight(1f),
                    backgroundColor = colorScheme.surfaceVariant,
                    valueColor = if ((currentBudget ?: 0.0) - totalSpent >= 0)
                        colorScheme.primary else colorScheme.error
                )
            }

            // Top categories breakdown
            CategoryBreakdownCard(
                spentByCategory = spentByCategory,
                currencyCode = settings.defaultCurrency,
                totalSpent = totalSpent,
                onSeeMoreClick = { showCategoryInsights = true },
                modifier = Modifier.fillMaxWidth()
            )

            // Pie chart section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.spending_by_category),
                        style = typography.titleMedium,
                        color = colorScheme.onSurface
                    )

                    if (spentByCategory.isNotEmpty()) {
                        AndroidView(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 8.dp),
                            factory = { ctx ->
                                createPieChart(
                                    context = ctx,
                                    entries = createPieEntries(spentByCategory),
                                    colorScheme = colorScheme
                                )
                            },
                            update = { chart ->
                                chart.data = createPieData(
                                    entries = createPieEntries(spentByCategory),
                                    colorScheme = colorScheme
                                )
                                chart.invalidate()
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_expenses_this_month),
                                style = typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Budget editing dialog
    if (showBudgetDialog) {
        BudgetDialog(
            currentBudget = currentBudget,
            currencyCode = settings.defaultCurrency,
            onDismiss = { showBudgetDialog = false },
            onSave = { amount -> budgetViewModel.saveBudget(periodKey, amount) }
        )
    }

    // Category insights dialog
    if (showCategoryInsights) {
        CategoryInsightsDialog(
            spentByCategory = spentByCategory,
            currencyCode = settings.defaultCurrency,
            totalSpent = totalSpent,
            onDismiss = { showCategoryInsights = false }
        )
    }
}

// --- Helpers & UI components below ---

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
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
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
    val visibleCategories = remember(spentByCategory) {
        spentByCategory.take(3)
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.top_categories),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (spentByCategory.size > 3) {
                    TextButton(onClick = onSeeMoreClick) {
                        Text(stringResource(R.string.see_more))
                    }
                }
            }

            if (spentByCategory.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_expenses_this_month),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                visibleCategories.forEach { (category, amount) ->
                    val percentage = if (totalSpent > 0) (amount / totalSpent) * 100 else 0.0
                    val progress = amount / totalSpent.coerceAtLeast(1.0)

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${"%.1f".format(percentage)}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = progressColor(progress.toFloat()),
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                        Text(
                            text = CurrencyFormatter.format(amount, currencyCode),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryInsightsDialog(
    spentByCategory: List<Pair<String, Double>>,
    currencyCode: String,
    totalSpent: Double,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.category_breakdown),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                spentByCategory.forEach { (category, amount) ->
                    val percentage = if (totalSpent > 0) (amount / totalSpent) * 100 else 0.0
                    val progress = amount / totalSpent.coerceAtLeast(1.0)

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${"%.1f".format(percentage)}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = progressColor(progress.toFloat()),
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.spent),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyFormatter.format(amount, currencyCode),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
        shape = MaterialTheme.shapes.extraLarge
    )
}

private fun createPieEntries(spentByCategory: List<Pair<String, Double>>): List<PieEntry> =
    spentByCategory.map { (category, amount) ->
        PieEntry(amount.toFloat(), category)
    }

private fun createPieChart(
    context: android.content.Context,
    entries: List<PieEntry>,
    colorScheme: ColorScheme
): PieChart = PieChart(context).apply {
    setHoleColor(colorScheme.surface.toArgb())
    setTransparentCircleColor(colorScheme.surfaceVariant.toArgb())
    setEntryLabelColor(colorScheme.onSurface.toArgb())
    setEntryLabelTextSize(12f)
    legend.apply {
        verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        orientation = Legend.LegendOrientation.HORIZONTAL
        textColor = colorScheme.onSurface.toArgb()
        textSize = 12f
        isWordWrapEnabled = true
        formSize = 12f
    }
    description.isEnabled = false
    isDrawHoleEnabled = true
    holeRadius = 45f
    transparentCircleRadius = 50f
    setUsePercentValues(true)
    isRotationEnabled = false
    setDrawEntryLabels(false)
    setTouchEnabled(true)
    animateY(1000)
    data = createPieData(entries, colorScheme)
    invalidate()
}

private fun createPieData(entries: List<PieEntry>, colorScheme: ColorScheme): PieData {
    val dataSet = PieDataSet(entries, "").apply {
        colors = PieChartColors.map { it.toArgb() }
        sliceSpace = 2f
        valueLinePart1Length = 0.4f
        valueLinePart2Length = 0.4f
        yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        valueTextSize = 12f
        valueTextColor = colorScheme.onSurface.toArgb()
    }
    return PieData(dataSet).apply {
        setValueFormatter(PercentFormatter())
        setValueTextSize(12f)
    }
}
