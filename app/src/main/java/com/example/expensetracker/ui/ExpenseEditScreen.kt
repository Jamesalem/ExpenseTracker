package com.example.expensetracker.ui

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expensetracker.data.viewmodel.ExpenseViewModel
import com.example.expensetracker.data.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

@SuppressLint("SimpleDateFormat")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEditScreen(
    navController: NavController,
    expenseId: Long
) {
    val expenseVm: ExpenseViewModel = hiltViewModel()
    val settingsVm: SettingsViewModel = hiltViewModel()

    // Load expense
    val expenseState by expenseVm.getExpenseById(expenseId).collectAsState(initial = null)

    // Form state from ViewModel
    val amount by expenseVm.formAmount
    val currencyCode by expenseVm.formCurrencyCode
    val category by expenseVm.formCategory
    val note by expenseVm.formNote

    // Date state
    val dateState = remember { mutableStateOf(Date()) }
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    // Initialize form when expense loads
    LaunchedEffect(expenseState) {
        expenseState?.let { expense ->
            expenseVm.initForm(expense)
            dateState.value = Date(expense.date)
            calendar.time = dateState.value
        }
    }

    // Show loading if expense not loaded
    if (expenseState == null) {
        Box(
            Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Date picker dialog
    val datePicker = remember {
        DatePickerDialog(
            context,
            { _, y, m, d ->
                calendar.set(y, m, d)
                dateState.value = calendar.time
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Expense") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Delete button
                FloatingActionButton(
                    onClick = {
                        expenseState?.let { expenseVm.deleteExpense(it) }
                        navController.popBackStack()
                    },
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Icon(Icons.Default.Delete, "Delete Expense")
                }

                // Save button
                FloatingActionButton(
                    onClick = {
                        expenseVm.updateExpense(expenseId)
                        navController.popBackStack()
                    }
                ) {
                    Icon(Icons.Default.Save, "Save Changes")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .systemBarsPadding()
                .imePadding()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Editing Expense #${expenseState?.id}", style = MaterialTheme.typography.titleMedium)

            // Amount field
            TextField(
                value = amount.toString(),
                onValueChange = { expenseVm.updateFormAmount(it.toDoubleOrNull() ?: 0.0) },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Currency picker
            CurrencyPicker(
                currencyCode = currencyCode,
                onCurrencySelected = { expenseVm.updateFormCurrencyCode(it) },
                modifier = Modifier.fillMaxWidth()
            )

            // Date picker button
            Button(onClick = { datePicker.show() }) {
                Text(SimpleDateFormat("yyyy-MM-dd").format(dateState.value))
            }

            // Category
            TextField(
                value = category,
                onValueChange = { expenseVm.updateFormCategory(it) },
                label = { Text("Category") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Note
            TextField(
                value = note,
                onValueChange = { expenseVm.updateFormNote(it) },
                label = { Text("Note (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            Spacer(Modifier.weight(1f))
        }
    }
}