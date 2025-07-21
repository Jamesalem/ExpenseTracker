// ui/home/HomeScreen.kt
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expensetracker.R
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.util.CurrencyFormatter
import com.example.expensetracker.data.util.DateFormatter
import com.example.expensetracker.data.viewmodel.ExpenseViewModel
import com.example.expensetracker.data.viewmodel.SettingsViewModel
import com.example.expensetracker.ui.navigation.Routes
import com.example.expensetracker.ui.theme.Dimens
import java.util.Locale

@Composable
fun HomeScreen(
    navController: NavController,
    expenseViewModel: ExpenseViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val expenses by expenseViewModel.expenses.collectAsState(initial = emptyList())
    val settings by settingsViewModel.appSettings.collectAsState(initial = AppSettings())

    // Compute recent transactions once per expenses change
    val recentTransactions by remember(expenses) {
        mutableStateOf(getRecentTransactions(expenses))
    }

    Scaffold(
        topBar = { HomeAppBar() },
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
            item { BalanceSummary(expenses, settings) }
            item { QuickActionsRow(navController) }
            item { SpendingChartSection() }
            item { RecentTransactionsHeader(navController) }
            items(recentTransactions) { tx ->
                TransactionItem(tx, settings)
                HorizontalDivider()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                stringResource(R.string.dashboard_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        },
        actions = {
            IconButton(onClick = { /* TODO */ }) {
                BadgedBox(badge = { Badge { Text("3") } }) {
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

@Composable
fun BalanceSummary(expenses: List<Expense>, settings: AppSettings) {
    val income = remember(expenses) {
        expenses.filter { it.type == Expense.ExpenseType.INCOME }.sumOf { it.amount }
    }
    val spent = remember(expenses) {
        expenses.filter { it.type == Expense.ExpenseType.EXPENSE }.sumOf { it.amount }
    }
    val balance = income - spent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.medium),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.current_balance),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Dimens.small))
            Text(
                CurrencyFormatter.format(balance, settings.defaultCurrency),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(Dimens.medium))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BalanceMetric(
                    title = stringResource(R.string.income),
                    value = CurrencyFormatter.format(income, settings.defaultCurrency)
                )
                BalanceMetric(
                    title = stringResource(R.string.expenses),
                    value = CurrencyFormatter.format(spent, settings.defaultCurrency)
                )
            }
        }
    }
}

@Composable
fun BalanceMetric(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Dimens.extraSmall))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
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
            icon = Icons.Filled.ShoppingCart,
            label = stringResource(R.string.expenses),
            onClick = { navController.navigate(Routes.EXPENSE_LIST) }
        )
        ActionButton(
            icon = Icons.Filled.PieChart,
            label = stringResource(R.string.reports),
            onClick = { navController.navigate(Routes.DASHBOARD) }
        )
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
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(Dimens.small))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun SpendingChartSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.medium),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(Dimens.medium)) {
            Text(
                stringResource(R.string.spending_overview),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(Modifier.height(Dimens.medium))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.chart_visualization),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Spacer(Modifier.height(Dimens.small))
            Text(
                stringResource(R.string.spending_insight),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(Dimens.medium), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(transaction.category.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    transaction.category.icon,
                    contentDescription = transaction.category.name,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(Dimens.small))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    transaction.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
                Spacer(Modifier.height(Dimens.extraSmall))
                Text(
                    "${transaction.category.name} • ${transaction.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                CurrencyFormatter.format(transaction.amount, settings.defaultCurrency),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = if (transaction.isExpense) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )
        }
    }
}

// --- Data classes & helper functions ---

data class Transaction(
    val id: String,
    val title: String,
    val amount: Double,
    val date: String,
    val category: Category,
    val isExpense: Boolean
)

data class Category(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

fun getRecentTransactions(expenses: List<Expense>): List<Transaction> {
    // sort descending by epoch millis then take top 4
    return expenses
        .sortedByDescending { it.date }
        .take(4)
        .map { exp ->
            Transaction(
                id = exp.id.toString(),
                title = exp.title,
                amount = if (exp.type == Expense.ExpenseType.EXPENSE) -exp.amount else exp.amount,
                date = DateFormatter.formatShortDate(exp.date),
                category = when (exp.category.lowercase(Locale.getDefault())) {
                    "food"      -> Category("Food", Icons.Filled.ShoppingCart, Color(0xFFFF6B6B))
                    "income"    -> Category("Income", Icons.Filled.AccountBalance, Color(0xFF4CAF50))
                    "utilities" -> Category("Utilities", Icons.Filled.Lightbulb, Color(0xFFFF9800))
                    "dining"    -> Category("Dining", Icons.Filled.Coffee, Color(0xFF9C27B0))
                    else        -> Category("Other", Icons.Filled.Wallet, Color(0xFF607D8B))
                },
                isExpense = exp.type == Expense.ExpenseType.EXPENSE
            )
        }
}
