// ui/expense/ExpenseFormScreen.kt
package com.example.expensetracker.ui.expense

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expensetracker.R
import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.util.CurrencyHelper
import com.example.expensetracker.data.viewmodel.ExpenseViewModel
import com.example.expensetracker.ui.components.pickers.CurrencyPicker
import com.example.expensetracker.ui.navigation.Routes
import com.example.expensetracker.ui.theme.Dimens
import com.example.expensetracker.ui.theme.Shapes
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseFormScreen(
    navController: NavController,
    expenseVm: ExpenseViewModel = hiltViewModel()
) {
    val formState by expenseVm.formState.collectAsState()
    val isEditing by expenseVm.isEditing.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    var showDatePicker by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }

    val currencySymbol = remember(formState.currencyCode) {
        CurrencyHelper.allCurrencies.firstOrNull { it.code == formState.currencyCode }?.symbol
            ?: formState.currencyCode
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault()) }
    val localDate = remember(formState.date) {
        formState.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }

    LaunchedEffect(Unit) {
        if (!isEditing) expenseVm.initForm()
    }

    LaunchedEffect(expenseVm.userMessage) {
        expenseVm.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
            if (message.contains("successfully")) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                navController.popBackStack()
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = formState.date.time
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        expenseVm.updateFormDate(Date(it))
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isEditing) stringResource(R.string.edit_expense) 
                        else stringResource(R.string.new_expense_title),
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = { navController.navigate(Routes.SETTINGS) }) {
                            Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.medium),
            verticalArrangement = Arrangement.spacedBy(Dimens.medium)
        ) {
            // Type Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.small)
            ) {
                FilterChip(
                    selected = formState.type == Expense.ExpenseType.EXPENSE,
                    onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        expenseVm.updateFormType(Expense.ExpenseType.EXPENSE) 
                    },
                    label = { Text(stringResource(R.string.expense), modifier = Modifier.padding(horizontal = 8.dp)) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
                FilterChip(
                    selected = formState.type == Expense.ExpenseType.INCOME,
                    onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        expenseVm.updateFormType(Expense.ExpenseType.INCOME) 
                    },
                    label = { Text(stringResource(R.string.income), modifier = Modifier.padding(horizontal = 8.dp)) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                )
            }

            Spacer(Modifier.height(Dimens.small))

            // Amount Input
            var amountInputText by remember(formState.amount) {
                mutableStateOf(if (formState.amount > 0) String.format(Locale.getDefault(), "%.2f", formState.amount) else "")
            }

            OutlinedTextField(
                value = amountInputText,
                onValueChange = { input ->
                    if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        amountInputText = input
                        expenseVm.updateFormAmount(input.toDoubleOrNull() ?: 0.0)
                        amountError = if (input.isEmpty() || (input.toDoubleOrNull() ?: 0.0) <= 0.0) context.getString(R.string.invalid_amount_error) else null
                    }
                },
                label = { Text(stringResource(R.string.amount)) },
                placeholder = { Text("0.00") },
                isError = amountError != null,
                supportingText = { amountError?.let { Text(it) } },
                leadingIcon = {
                    Text(currencySymbol, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                },
                trailingIcon = {
                    if (amountInputText.isNotEmpty()) {
                        IconButton(onClick = { 
                            amountInputText = ""
                            expenseVm.updateFormAmount(0.0)
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.backspace))
                        }
                    } else {
                        CurrencyPicker(
                            currencyCode = formState.currencyCode,
                            onCurrencySelected = { expenseVm.updateFormCurrencyCode(it) }
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = Shapes.medium
            )

            // Category Field
            OutlinedTextField(
                value = formState.category,
                onValueChange = { 
                    expenseVm.updateFormCategory(it)
                    categoryError = if (it.isBlank()) context.getString(R.string.category_required) else null
                },
                label = { Text(stringResource(R.string.category)) },
                isError = categoryError != null,
                supportingText = { categoryError?.let { Text(it) } },
                leadingIcon = { Icon(Icons.Default.Category, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = Shapes.medium,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            // Preset Categories
            val presetCategories = remember(formState.type) {
                if (formState.type == Expense.ExpenseType.INCOME) {
                    listOf("Salary", "Freelance", "Investment", "Gift", "Business")
                } else {
                    listOf("Food", "Rent", "Transport", "Bills", "Health", "Shopping")
                }
            }

            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Dimens.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(presetCategories.size) { idx ->
                    val cat = presetCategories[idx]
                    FilterChip(
                        selected = formState.category.equals(cat, ignoreCase = true),
                        onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            expenseVm.updateFormCategory(cat)
                            categoryError = null
                        },
                        label = { Text(cat) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.alpha(0.5f))

            // Date Picker Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Shapes.medium)
                    .clickable { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showDatePicker = true 
                    }
                    .padding(vertical = Dimens.small)
            ) {
                Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(Dimens.medium))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.date), style = MaterialTheme.typography.labelMedium)
                    Text(localDate.format(dateFormatter), style = MaterialTheme.typography.bodyLarge)
                }
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            }

            // Notes Field
            OutlinedTextField(
                value = formState.note,
                onValueChange = { expenseVm.updateFormNote(it) },
                label = { Text(stringResource(R.string.note_optional)) },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = Shapes.medium,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            Spacer(Modifier.height(Dimens.large))

            Button(
                onClick = {
                    if (formState.amount <= 0.0) {
                        amountError = context.getString(R.string.invalid_amount_error)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    } else if (formState.category.isBlank()) {
                        categoryError = context.getString(R.string.category_required)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    } else {
                        focusManager.clearFocus()
                        expenseVm.submitExpense()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.buttonHeight),
                shape = Shapes.medium
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(Modifier.width(Dimens.small))
                Text(
                    if (isEditing) stringResource(R.string.update_record) 
                    else stringResource(R.string.save_transaction), 
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
