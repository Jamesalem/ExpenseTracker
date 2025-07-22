package com.example.expensetracker.ui.expense

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost // NEW: Import SnackbarHost
import androidx.compose.material3.SnackbarHostState // NEW: Import SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect // NEW: Import LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expensetracker.R
import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.util.CurrencyFormatter
import com.example.expensetracker.data.viewmodel.ExpenseViewModel
import com.example.expensetracker.ui.components.pickers.CurrencyPicker
import com.example.expensetracker.ui.theme.Dimens
import com.example.expensetracker.ui.theme.Shapes
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseFormScreen(
    navController: NavController,
    expenseVm: ExpenseViewModel = hiltViewModel()
) {
    val formState by expenseVm.formState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() } // NEW: Create SnackbarHostState

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val dateState = remember { mutableStateOf(Date()) }

    val datePicker = remember {
        DatePickerDialog(
            context,
            { _, y, m, d ->
                calendar.set(y, m, d)
                dateState.value = calendar.time
                expenseVm.updateFormDate(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    // NEW: Initialize form on first composition
    LaunchedEffect(Unit) {
        expenseVm.initForm()
    }

    // NEW: Observe user messages for SnackBar
    LaunchedEffect(expenseVm.userMessage) {
        expenseVm.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
            // Navigate back only after a successful save message is shown
            // Assuming successful messages implies it's safe to pop back
            if (message.contains("successfully")) { // Simple check, refine if needed
                navController.popBackStack()
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_expense)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    expenseVm.submitExpense()
                    // REMOVED: navController.popBackStack() is now handled in LaunchedEffect(expenseVm.userMessage)
                },
                icon = { Icon(Icons.Default.Add, stringResource(R.string.save_expense)) },
                text = { Text(stringResource(R.string.save_expense)) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) } // NEW: Provide SnackbarHost
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(Dimens.large),
            verticalArrangement = Arrangement.spacedBy(Dimens.medium)
        ) {
            // Amount
            OutlinedTextField(
                value = if (formState.amount > 0) formState.amount.toString() else "",
                onValueChange = {
                    expenseVm.updateFormAmount(it.toDoubleOrNull() ?: 0.0)
                },
                label = { Text(stringResource(R.string.amount)) },
                leadingIcon = {
                    Text(
                        CurrencyFormatter.getSymbol(formState.currencyCode),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = Shapes.large
            )

            // Currency
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.currency),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(Dimens.small))
                CurrencyPicker(
                    currencyCode = formState.currencyCode,
                    onCurrencySelected = { expenseVm.updateFormCurrencyCode(it) },
                    modifier = Modifier.weight(1.5f)
                )
            }

            HorizontalDivider()

            // Date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.date),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(Dimens.small))
                OutlinedButton(
                    onClick = { datePicker.show() },
                    modifier = Modifier.weight(1.5f),
                    shape = Shapes.large
                ) {
                    Text(
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            .format(dateState.value)
                    )
                }
            }

            HorizontalDivider()

            // Category
            OutlinedTextField(
                value = formState.category,
                onValueChange = { expenseVm.updateFormCategory(it) },
                label = { Text(stringResource(R.string.category)) },
                leadingIcon = { Icon(Icons.Default.Category, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = Shapes.large
            )

            HorizontalDivider()

            // Note
            OutlinedTextField(
                value = formState.note,
                onValueChange = { expenseVm.updateFormNote(it) },
                label = { Text(stringResource(R.string.note_optional)) },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = Shapes.large
            )

            // Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = formState.type == Expense.ExpenseType.EXPENSE,
                    onClick = { expenseVm.updateFormType(Expense.ExpenseType.EXPENSE) },
                    label = { Text(stringResource(R.string.expense)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
                FilterChip(
                    selected = formState.type == Expense.ExpenseType.INCOME,
                    onClick = { expenseVm.updateFormType(Expense.ExpenseType.INCOME) },
                    label = { Text(stringResource(R.string.income)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                )
            }
        }
    }
}