package com.example.expensetracker.ui.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator // NEW: Import CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost // NEW: Import SnackbarHost
import androidx.compose.material3.SnackbarHostState // NEW: Import SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect // NEW: Import LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expensetracker.R
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.util.CurrencyFormatter
import com.example.expensetracker.data.viewmodel.ExpenseViewModel
import com.example.expensetracker.ui.theme.Dimens
import com.example.expensetracker.ui.theme.Shapes
import com.example.expensetracker.ui.theme.generateCategoryColor
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    navController: NavController,
    expenseVm: ExpenseViewModel = hiltViewModel()
) {
    // UPDATED: Collect combined uiState from ExpenseViewModel
    val uiState by expenseVm.uiState.collectAsState()
    val expenses = (uiState as? ExpenseViewModel.ExpenseUiState.Success)?.expenses.orEmpty()
    val settings = (uiState as? ExpenseViewModel.ExpenseUiState.Success)?.settings ?: AppSettings()

    var toDelete by remember { mutableStateOf<Expense?>(null) }
    val dateFmt = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val snackbarHostState = remember { SnackbarHostState() } // NEW: Create SnackbarHostState

    // NEW: Observe user messages for SnackBar
    LaunchedEffect(expenseVm.userMessage) {
        expenseVm.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.expenses)) },
                actions = {
                    IconButton(onClick = { navController.navigate("dashboard") }) {
                        Icon(
                            Icons.Filled.PieChart,
                            contentDescription = stringResource(R.string.dashboard)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add") },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(R.string.add_expense),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) } // NEW: Provide SnackbarHost
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (uiState) { // UPDATED: Handle different UI states
                is ExpenseViewModel.ExpenseUiState.Loading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(Dimens.extraSmall))
                        Text(
                            stringResource(R.string.loading), // Assuming you have a "loading" string resource
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                is ExpenseViewModel.ExpenseUiState.Success -> {
                    if (expenses.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.Inbox,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(Dimens.extraSmall))
                            Text(
                                stringResource(R.string.no_expenses_title),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(Dimens.small))
                            Text(
                                stringResource(R.string.no_expenses_message),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(Dimens.medium),
                            verticalArrangement = Arrangement.spacedBy(Dimens.small)
                        ) {
                            items(
                                items = expenses,
                                key = { it.id }
                            ) { exp ->
                                ExpenseItem(
                                    expense = exp,
                                    decimalPlaces = settings.decimalPlaces,
                                    useGrouping = settings.useGroupingSeparator,
                                    dateFormatter = dateFmt,
                                    onDelete = { toDelete = exp },
                                    onClick = { navController.navigate("detail/${exp.id}") }
                                )
                            }
                        }
                    }
                }
                is ExpenseViewModel.ExpenseUiState.Error -> {
                    Column( // UPDATED: Show actual error message
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.Inbox, // Using Inbox as a generic error icon for now
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(Dimens.extraSmall))
                        Text(
                            (uiState as ExpenseViewModel.ExpenseUiState.Error).message, // Display actual error message
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error // Use error color
                        )
                        Spacer(Modifier.height(Dimens.small))
                        Text(
                            stringResource(R.string.try_again_message), // Assuming you have a "try again" message
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }


            toDelete?.let { exp ->
                AlertDialog(
                    onDismissRequest = { toDelete = null },
                    title = { Text(stringResource(R.string.delete_expense_title)) },
                    text = { Text(stringResource(R.string.delete_expense_message)) },
                    confirmButton = {
                        TextButton(onClick = {
                            expenseVm.deleteExpense(exp)
                            toDelete = null
                        }) {
                            Text(
                                stringResource(R.string.delete),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { toDelete = null }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ExpenseItem(
    expense: Expense,
    decimalPlaces: Int,
    useGrouping: Boolean,
    dateFormatter: SimpleDateFormat,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    // convert LocalDate → Date
    val date = Date.from(expense.date.atStartOfDay(ZoneId.systemDefault()).toInstant())
    val color = generateCategoryColor(expense.category.hashCode().toLong())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = Shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationS)
    ) {
        Row(
            modifier = Modifier.padding(Dimens.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(Dimens.iconL)
                    .clip(Shapes.extraSmall)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    expense.category.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(Modifier.width(Dimens.medium))

            Column(modifier = Modifier.weight(1f)) {
                Text(expense.category, style = MaterialTheme.typography.bodyLarge)
                Text(
                    dateFormatter.format(date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                expense.note?.takeIf(String::isNotBlank)?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(Dimens.extraSmall)
            ) {
                Text(
                    CurrencyFormatter.format(
                        expense.amount,
                        expense.currencyCode,
                        decimalPlaces,
                        useGrouping
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (expense.type == Expense.ExpenseType.INCOME)
                        MaterialTheme.colorScheme.tertiary
                    else
                        MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = onDelete, modifier = Modifier.size(Dimens.iconM)) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}