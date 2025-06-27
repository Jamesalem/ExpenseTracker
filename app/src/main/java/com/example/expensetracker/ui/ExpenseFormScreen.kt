@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.expensetracker.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.expensetracker.data.util.CurrencyFormatter
import com.example.expensetracker.data.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpenseFormScreen(
    navController: NavController,
    vm: ExpenseViewModel = viewModel()
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    var amountText by remember { mutableStateOf("") }
    var currencyCode by remember { mutableStateOf("USD") }
    var category by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(calendar.time) }

    var expanded by remember { mutableStateOf(false) }

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

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Box {
            TextField(
                value = currencyCode,
                onValueChange = { },
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

        Button(onClick = { datePicker.show() }) {
            Text(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date))
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
                val amt = amountText.toDoubleOrNull() ?: 0.0
                vm.addExpense(
                    amount = amt,
                    currencyCode = currencyCode,
                    date = date,
                    category = category.ifBlank { "Misc" },
                    note = note.ifBlank { null },
                    receiptUri = null
                )
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Expense")
        }
    }
}
