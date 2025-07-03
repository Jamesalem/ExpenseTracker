package com.example.expensetracker.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseFormScreen(
    navController: NavController
) {
    val expenseVm: ExpenseViewModel = hiltViewModel()
    val settingsVm: SettingsViewModel = hiltViewModel()
    val defaultCurrency by settingsVm.defaultCurrency.collectAsState()

    // Initialize form state
    LaunchedEffect(Unit) {
        expenseVm.initForm()
        expenseVm.updateFormCurrencyCode(defaultCurrency)
    }

    // Get form state from ViewModel
    val amountState = expenseVm.formAmount
    val currencyCodeState = expenseVm.formCurrencyCode
    val categoryState = expenseVm.formCategory
    val noteState = expenseVm.formNote

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val dateState = remember { mutableStateOf(Date()) }

    // Date picker dialog
    val datePicker = remember {
        DatePickerDialog(
            context, { _, y, m, d ->
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
                title = { Text("New Expense") },
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
            FloatingActionButton(onClick = {
                expenseVm.addExpense(dateState.value.time)
                navController.popBackStack()
            }) {
                Icon(Icons.Default.Add, "Save Expense")
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
            // Amount field - fixed to use value from state
            TextField(
                value = amountState.value.toString(),
                onValueChange = { newValue ->
                    expenseVm.updateFormAmount(newValue.toDoubleOrNull() ?: 0.0)
                },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Currency picker - fixed to use state value
            CurrencyPicker(
                currencyCode = currencyCodeState.value,
                onCurrencySelected = { expenseVm.updateFormCurrencyCode(it) },
                modifier = Modifier.fillMaxWidth()
            )

            // Date picker button
            Button(onClick = { datePicker.show() }) {
                Text(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dateState.value))
            }

            // Category - fixed to use state value
            TextField(
                value = categoryState.value,
                onValueChange = { expenseVm.updateFormCategory(it) },
                label = { Text("Category") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Note - fixed to use state value
            TextField(
                value = noteState.value,
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