package com.example.expensetracker.ui.expense

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expensetracker.R
import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.util.CurrencyFormatter
import com.example.expensetracker.data.util.DateFormatter
import com.example.expensetracker.data.viewmodel.ExpenseViewModel
import com.example.expensetracker.ui.theme.Dimens
import com.example.expensetracker.ui.theme.Shapes
import com.example.expensetracker.ui.theme.Typography
import com.example.expensetracker.ui.theme.extendedColors
import com.example.expensetracker.ui.theme.generateCategoryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    navController: NavController,
    expenseId: Long,
    vm: ExpenseViewModel = hiltViewModel()
) {
    val expense by vm.getExpenseById(expenseId).collectAsState(initial = null)
    var showDeleteDialog by remember { mutableStateOf(false) }
    val ext = MaterialTheme.extendedColors
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(vm.userMessage) {
        vm.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
            if (message.contains("successfully")) {
                navController.popBackStack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.expense_details),
                        style = Typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    expense?.let {
                        IconButton(onClick = { navController.navigate("edit/$expenseId") }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { inner ->
        if (expense == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val e = expense!!
            // REMOVED: Unused 'date' variable
            // val date: Date = Date.from(e.date.atStartOfDay(ZoneId.systemDefault()).toInstant())

            // UPDATED: Use category hash code for color generation consistency
            val catColor = remember(e.category) { generateCategoryColor(e.category.hashCode().toLong()) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(inner)
                    .padding(Dimens.large),
                verticalArrangement = Arrangement.spacedBy(Dimens.large)
            ) {
                // Category card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = catColor.copy(alpha = 0.1f)),
                    shape = Shapes.large
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.large),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            e.category,
                            style = Typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(Dimens.small))
                        Text(
                            // UPDATED: Use the new CurrencyFormatter.format overload
                            CurrencyFormatter.format(e.amount, e.currencyCode),
                            style = Typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (e.type == Expense.ExpenseType.INCOME)
                                ext.incomeColor else ext.expenseColor
                        )
                    }
                }

                // Details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.small),
                    verticalArrangement = Arrangement.spacedBy(Dimens.medium)
                ) {
                    DetailItem(
                        label = stringResource(R.string.date),
                        value = DateFormatter.formatDate(e.date), // Correctly uses LocalDate
                        icon = R.drawable.ic_calendar_detail
                    )
                    DetailItem(
                        label = stringResource(R.string.currency),
                        value = e.currencyCode,
                        icon = R.drawable.ic_currency
                    )
                    DetailItem(
                        label = stringResource(R.string.type),
                        value = e.type.name,
                        icon = R.drawable.ic_type
                    )
                    e.note?.takeIf(String::isNotBlank)?.let {
                        DetailItem(
                            label = stringResource(R.string.notes),
                            value = it,
                            icon = R.drawable.ic_notes_detail
                        )
                    }
                }

                // Delete button
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = Shapes.medium
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        modifier = Modifier.size(Dimens.iconM)
                    )
                    Spacer(Modifier.width(Dimens.small))
                    Text(
                        text = stringResource(R.string.delete_expense),
                        style = Typography.labelLarge
                    )
                }
            }
        }
    }

    // confirm deletion
    if (showDeleteDialog && expense != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(stringResource(R.string.confirm_deletion), style = Typography.titleLarge)
            },
            text = {
                Text(stringResource(R.string.delete_expense_message), style = Typography.bodyMedium)
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteExpense(expense!!)
                    showDeleteDialog = false
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            shape = Shapes.large
        )
    }
}

@Composable
private fun DetailItem(label: String, value: String, icon: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Dimens.iconM)
        )
        Spacer(Modifier.width(Dimens.medium))
        Column {
            Text(label, style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}