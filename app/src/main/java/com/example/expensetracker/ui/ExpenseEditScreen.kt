package com.example.expensetracker.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.expensetracker.data.util.CurrencyFormatter
import com.example.expensetracker.data.util.DateFormatter
import com.example.expensetracker.data.viewmodel.ExpenseViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEditScreen(
    navController: NavController,
    expenseId: Long,
    vm: ExpenseViewModel = viewModel()
) {
    val context = LocalContext.current

    // Observe the original expense
    val original by vm
        .getExpenseById(expenseId)
        .collectAsState(initial = null)

    // Editable state
    var amountText by rememberSaveable { mutableStateOf("") }
    var currencyCode by rememberSaveable { mutableStateOf("USD") }
    var category by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf(Date()) }

    // Initialize when the expense loads
    LaunchedEffect(original) {
        original?.let {
            amountText = it.amount.toString()
            currencyCode = it.currencyCode
            category = it.category
            note = it.note.orEmpty()
            date = Date(it.date)
        }
    }

    // Date picker dialog
    val calendar = Calendar.getInstance().apply { time = date }
    val datePicker = DatePickerDialog(
        context,
        { _, y, m, d ->
            calendar.set(y, m, d)
            date = calendar.time
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Currency dropdown state
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Editing Expense #$expenseId",
            style = MaterialTheme.typography.titleMedium
        )

        TextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Currency picker
        Box {
            TextField(
                value = currencyCode,
                onValueChange = { /* read-only */ },
                label = { Text("Currency") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                CurrencyFormatter.currencyCodes.forEach { code ->
                    DropdownMenuItem(
                        text = { Text(code) },
                        onClick = {
                            currencyCode = code
                            expanded = false
                        }
                    )
                }
            }
        }

        // Date button
        Button(onClick = { datePicker.show() }) {
            Text(DateFormatter.toIso(date))
        }

        TextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note (optional)") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                original?.let { e ->
                    val updated = e.copy(
                        amount = amountText.toDoubleOrNull() ?: e.amount,
                        currencyCode = currencyCode,
                        date = date.time,
                        category = category,
                        note = note.ifBlank { null }
                    )
                    vm.updateExpense(updated)
                    navController.popBackStack("detail/$expenseId", inclusive = false)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Changes")
        }
    }
}
