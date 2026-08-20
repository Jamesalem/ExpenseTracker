package com.example.expensetracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.example.expensetracker.ui.theme.PieChartColors
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.example.expensetracker.R
import com.example.expensetracker.data.home.HomeUtils
import com.example.expensetracker.data.home.Transaction
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.util.CurrencyFormatter
import com.example.expensetracker.data.viewmodel.ExpenseViewModel
import com.example.expensetracker.data.viewmodel.SettingsViewModel
import com.example.expensetracker.data.viewmodel.TimeViewModel
import com.example.expensetracker.ui.navigation.Routes
import com.example.expensetracker.ui.theme.Dimens
import com.example.expensetracker.ui.theme.Shapes
import com.example.expensetracker.ui.time.formatDuration

import androidx.compose.material.icons.filled.Shield
import com.example.expensetracker.data.viewmodel.DashboardViewModel
import com.example.expensetracker.ui.components.cards.HealthScoreCard
import com.example.expensetracker.ui.components.cards.SafeSpendPulseCard

@Composable
fun HomeScreen(
    navController: NavController,
    expenseViewModel: ExpenseViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    timeViewModel: TimeViewModel = hiltViewModel(),
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    // OPTIMIZED: Use targeted flows instead of full list filtering
    val recentExpenses by expenseViewModel.recentExpenses.collectAsState(initial = emptyList())
    val allExpenses by expenseViewModel.expenses.collectAsState(initial = emptyList())
    val totalIncome by expenseViewModel.totalIncome.collectAsState(initial = 0.0)
    val totalExpense by expenseViewModel.totalExpense.collectAsState(initial = 0.0)
    val settings by settingsViewModel.appSettings.collectAsState(initial = AppSettings())
    val todaySeconds by timeViewModel.todaySeconds.collectAsState()
    val dashboardState by dashboardViewModel.uiState.collectAsState()

    var showNotificationSheet by remember { mutableStateOf(false) }

    val recentTransactions = remember(recentExpenses) {
        HomeUtils.getRecentTransactions(recentExpenses)
    }

    val spentByCategory = remember(allExpenses) {
        allExpenses
            .filter { it.type == Expense.ExpenseType.EXPENSE }
            .groupBy { it.category }
            .mapValues { it.value.sumOf(Expense::amount) }
            .toList()
            .sortedByDescending { it.second }
    }

    Scaffold(
        topBar = { HomeAppBar(onNotificationsClick = { showNotificationSheet = true }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.EXPENSE_FORM) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, stringResource(R.string.add_expense))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(Dimens.medium)
        ) {
            item { 
                BalanceSummary(
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    settings = settings
                ) 
            }

            // Safe-to-Spend Velocity Gauge
            (dashboardState as? DashboardViewModel.DashboardUiState.Success)?.let { success ->
                item {
                    SafeSpendPulseCard(
                        result = success.safeSpendResult,
                        currencyCode = settings.defaultCurrency,
                        modifier = Modifier.padding(horizontal = Dimens.medium)
                    )
                }

                item {
                    HealthScoreCard(
                        healthScore = success.healthScore,
                        onClick = { navController.navigate(Routes.HEALTH) },
                        modifier = Modifier.padding(horizontal = Dimens.medium)
                    )
                }
            }

            item { QuickActionsRow(navController) }
            item { TodayTimeSummaryCard(todaySeconds ?: 0L, navController) }
            item { SpendingChartSection(spentByCategory = spentByCategory) }
            item { RecentTransactionsHeader(navController) }
            items(recentTransactions) { tx ->
                TransactionItem(tx, settings)
                HorizontalDivider(modifier = Modifier.padding(horizontal = Dimens.medium))
            }
        }

        if (showNotificationSheet) {
            NotificationSummaryBottomSheet(
                settings = settings,
                onDismiss = { showNotificationSheet = false },
                onOpenSettings = {
                    showNotificationSheet = false
                    navController.navigate(Routes.SETTINGS)
                },
                onSendTestNotification = {
                    settingsViewModel.sendTestNotification()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar(onNotificationsClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                stringResource(R.string.dashboard_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        actions = {
            IconButton(onClick = onNotificationsClick) {
                BadgedBox(badge = { Badge { Text("!") } }) {
                    Icon(
                        Icons.Filled.Notifications,
                        contentDescription = stringResource(R.string.notifications),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSummaryBottomSheet(
    settings: AppSettings,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
    onSendTestNotification: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Notification Center",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Current Status",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (settings.enableNotifications)
                            "Daily Alerts active at ${settings.notificationTime} (${settings.weeklyReminderDay.name})"
                        else
                            "Notifications are currently disabled",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onSendTestNotification,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Notifications, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Trigger Test Notification")
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = onOpenSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Settings, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Configure Notification Settings")
            }
        }
    }
}

@Composable
fun BalanceSummary(
    totalIncome: Double,
    totalExpense: Double,
    settings: AppSettings
) {
    val balance = totalIncome - totalExpense
    var isBalanceVisible by remember { mutableStateOf(true) }

    val heroGradient = remember {
        androidx.compose.ui.graphics.Brush.linearGradient(
            listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.medium)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(heroGradient)
            .padding(Dimens.large)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.current_balance),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(Modifier.width(6.dp))
                IconButton(
                    onClick = { isBalanceVisible = !isBalanceVisible },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        if (isBalanceVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle Balance Visibility",
                        tint = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
            Spacer(Modifier.height(Dimens.small))
            Text(
                if (isBalanceVisible) CurrencyFormatter.format(balance, settings.defaultCurrency) else "••••••••",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 38.sp
                ),
                color = Color.White
            )
            Spacer(Modifier.height(Dimens.medium))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BalanceMetric(
                    title = stringResource(R.string.income),
                    value = if (isBalanceVisible) CurrencyFormatter.format(totalIncome, settings.defaultCurrency) else "••••",
                    isIncome = true,
                    modifier = Modifier.weight(1f)
                )
                BalanceMetric(
                    title = stringResource(R.string.expenses),
                    value = if (isBalanceVisible) CurrencyFormatter.format(totalExpense, settings.defaultCurrency) else "••••",
                    isIncome = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun BalanceMetric(
    title: String,
    value: String,
    isIncome: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(Color.White.copy(alpha = 0.18f))
            .padding(vertical = 12.dp, horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isIncome) Color(0xFF6EE7B7) else Color(0xFFFCA5A5)
            )
        }
    }
}

@Composable
fun QuickActionsRow(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.medium),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ActionButton(
            icon = Icons.Filled.Add,
            label = stringResource(R.string.add),
            onClick = { navController.navigate(Routes.EXPENSE_FORM) }
        )
        ActionButton(
            icon = Icons.Filled.Timer,
            label = "Timer",
            onClick = { navController.navigate(Routes.TIME_TRACKER) }
        )
        ActionButton(
            icon = Icons.Filled.Refresh,
            label = "Bills",
            onClick = { navController.navigate(Routes.SUBSCRIPTIONS) }
        )
        ActionButton(
            icon = Icons.Filled.PieChart,
            label = stringResource(R.string.reports),
            onClick = { navController.navigate(Routes.DASHBOARD) }
        )
        ActionButton(
            icon = Icons.Filled.Shield,
            label = "Health",
            onClick = { navController.navigate(Routes.HEALTH) }
        )
    }
}

@Composable
fun TodayTimeSummaryCard(todaySeconds: Long, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.medium)
            .clickable { navController.navigate(Routes.TIME_TRACKER) },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(Dimens.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(Dimens.medium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.tracked_time_today),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    formatDuration(todaySeconds),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            TextButton(onClick = { navController.navigate(Routes.TIME_TRACKER) }) {
                Text(stringResource(R.string.open_timer))
            }
        }
    }
}

@Composable
fun ActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(Dimens.small))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun SpendingChartSection(spentByCategory: List<Pair<String, Double>>) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.medium),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(Dimens.medium)) {
            Text(
                stringResource(R.string.spending_overview),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(Dimens.small))

            if (spentByCategory.isNotEmpty()) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(top = Dimens.small),
                    factory = { ctx ->
                        PieChart(ctx).apply {
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
                            data = createHomeScreenPieData(spentByCategory, colorScheme)
                        }
                    },
                    update = { chart ->
                        chart.data = createHomeScreenPieData(spentByCategory, colorScheme)
                        chart.invalidate()
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(
                            color = colorScheme.surface.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.medium
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.no_expenses_this_month),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun createHomeScreenPieData(
    spentByCategory: List<Pair<String, Double>>,
    colorScheme: androidx.compose.material3.ColorScheme
): PieData {
    val entries = spentByCategory.map { (category, amount) -> PieEntry(amount.toFloat(), category) }
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

@Composable
fun RecentTransactionsHeader(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.recent_transactions),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = { navController.navigate(Routes.EXPENSE_LIST) }) {
            Text(stringResource(R.string.view_all))
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, settings: AppSettings) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.medium),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(Dimens.medium), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(transaction.categoryDisplay.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    transaction.categoryDisplay.icon,
                    contentDescription = null,
                    tint = transaction.categoryDisplay.color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(Dimens.medium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    transaction.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "${transaction.categoryDisplay.name} • ${transaction.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Account badge
                    Box(
                        modifier = Modifier
                            .clip(Shapes.extraSmall)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            transaction.account,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (transaction.tags.isNotBlank()) {
                        Text(
                            transaction.tags,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.primary),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Text(
                CurrencyFormatter.format(transaction.amount, settings.defaultCurrency),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = if (transaction.isExpense) MaterialTheme.colorScheme.error
                else Color(0xFF10B981)
            )
        }
    }
}
