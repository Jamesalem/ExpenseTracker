package com.example.expensetracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.expensetracker.data.util.CurrencyFormatter
import com.example.expensetracker.data.util.DateFormatter
import com.example.expensetracker.data.viewmodel.ExpenseViewModel
import java.util.*

@Composable
fun ExpenseDetailScreen(
    navController: NavController,
    expenseId: Long,
    vm: ExpenseViewModel = viewModel()
) {
    // Collect the expense by ID, initialValue = null
    val expense by vm
        .getExpenseById(expenseId)
        .collectAsState(initial = null)

    expense?.let { e ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Category: ${e.category}",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Amount: ${CurrencyFormatter.format(e.amount, e.currencyCode)}",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "Date: ${DateFormatter.toDisplay(Date(e.date))}",
                style = MaterialTheme.typography.bodyLarge
            )

            e.note?.let {
                Text(
                    text = "Note: $it",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { navController.navigate("edit/${e.id}") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit")
            }
        }
    }
}
