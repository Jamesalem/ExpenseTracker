package com.example.expensetracker.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expensetracker.data.util.CurrencyFormatter
import com.example.expensetracker.data.util.DateFormatter
import com.example.expensetracker.data.viewmodel.ExpenseViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ExpenseDetailScreen(
    navController: NavController,
    expenseId: Long,
    vm: ExpenseViewModel = hiltViewModel()
) {
    // Remember the flow *once* for this expenseId
    val expenseFlow = remember(expenseId) { vm.getExpenseById(expenseId) }
    val expense by expenseFlow.collectAsState(initial = null)

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Loading spinner only until we get a non-null expense
    if (expense == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Safe to unwrap now
    val e = expense!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        modifier = Modifier.systemBarsPadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Category: ${e.category}", style = MaterialTheme.typography.titleMedium)
            Text("Amount: ${CurrencyFormatter.format(e.amount, e.currencyCode)}")
            Text("Date: ${DateFormatter.toDisplay(Date(e.date))}")
            e.note?.let { Text("Note: $it") }

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { navController.navigate("edit/${e.id}") }) {
                    Text("Edit")
                }
                Button(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, "Delete")
                    Spacer(Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Expense") },
                text = {
                    Text(
                        "Are you sure you want to delete “${e.category}” of " +
                                "${CurrencyFormatter.format(e.amount, e.currencyCode)}?"
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        vm.deleteExpense(e)
                        showDeleteDialog = false
                        navController.popBackStack()
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
