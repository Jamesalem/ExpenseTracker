// ui/expense/ExpenseEditScreen.kt
package com.example.expensetracker.ui.expense

import android.app.DatePickerDialog
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expensetracker.R
import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.util.DateFormatter
import com.example.expensetracker.data.viewmodel.ExpenseViewModel
import com.example.expensetracker.ui.components.pickers.CurrencyPicker
import com.example.expensetracker.ui.theme.Dimens
import com.example.expensetracker.ui.theme.Shapes
import com.example.expensetracker.ui.theme.Typography
import com.example.expensetracker.ui.theme.extendedColors
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEditScreen(
    navController: NavController,
    expenseId: Long,
    vm: ExpenseViewModel = hiltViewModel()
) {
    val expense by vm.getExpenseById(expenseId).collectAsState(initial = null)
    val form by vm.formState.collectAsState()
    val context = LocalContext.current
    var showDelete by remember { mutableStateOf(false) }
    val ext = MaterialTheme.extendedColors

    // initialize
    LaunchedEffect(expense) {
        expense?.let { vm.initForm(it) }
    }

    // date picker
    val calendar = remember { Calendar.getInstance().apply { time = form.date } }
    val picker = remember {
        DatePickerDialog(
            context,
            { _, y, m, d ->
                calendar.set(y, m, d)
                vm.updateFormDate(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.edit_expense),
                        style = Typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    vm.submitExpense()
                    navController.popBackStack()
                },
                icon = { Icon(Icons.Default.Save, contentDescription = stringResource(R.string.save)) },
                text = { Text(stringResource(R.string.save_changes), style = Typography.labelLarge) },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { inner ->
        if (expense == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(inner)
                    .padding(Dimens.large),
                verticalArrangement = Arrangement.spacedBy(Dimens.large)
            ) {
                // Amount
                OutlinedTextField(
                    value = form.amount.toString(),
                    onValueChange = { vm.updateFormAmount(it.toDoubleOrNull() ?: 0.0) },
                    label = { Text(stringResource(R.string.amount), style = Typography.labelLarge) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_money),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.large,
                    textStyle = Typography.bodyLarge
                )

                // Currency
                CurrencyPicker(
                    currencyCode = form.currencyCode,
                    onCurrencySelected = { vm.updateFormCurrencyCode(it) },
                    modifier = Modifier.fillMaxWidth()
                )

                // Date
                OutlinedTextField(
                    value = DateFormatter.toDisplay(form.date),
                    onValueChange = {},
                    label = { Text(stringResource(R.string.date), style = Typography.labelLarge) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_calendar),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { picker.show() }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null)
                        }
                    },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.large,
                    textStyle = Typography.bodyLarge
                )

                // Category
                OutlinedTextField(
                    value = form.category,
                    onValueChange = { vm.updateFormCategory(it) },
                    label = { Text(stringResource(R.string.category), style = Typography.labelLarge) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_category),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.large,
                    textStyle = Typography.bodyLarge
                )

                // Type chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.small)
                ) {
                    Expense.ExpenseType.entries.forEach { type ->
                        val sel = form.type == type
                        FilterChip(
                            selected = sel,
                            onClick = { vm.updateFormType(type) },
                            label = { Text(type.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (type == Expense.ExpenseType.INCOME) ext.incomeColor.copy(alpha = 0.2f) else ext.expenseColor.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = sel,
                                borderColor = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                borderWidth = if (sel) 2.dp else 1.dp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Notes
                OutlinedTextField(
                    value = form.note,
                    onValueChange = { vm.updateFormNote(it) },
                    label = { Text(stringResource(R.string.note_optional), style = Typography.labelLarge) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_notes),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = Shapes.large,
                    textStyle = Typography.bodyLarge
                )

                // Delete
                Button(
                    onClick = { showDelete = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = Shapes.medium
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(Dimens.iconM))
                    Spacer(Modifier.width(Dimens.small))
                    Text(stringResource(R.string.delete_expense), style = Typography.labelLarge)
                }
            }
        }
    }

    if (showDelete && expense != null) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            icon = {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            },
            title = { Text(stringResource(R.string.confirm_deletion), style = Typography.titleLarge) },
            text = { Text(stringResource(R.string.delete_expense_message), style = Typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteExpense(expense!!)
                    navController.popBackStack()
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            shape = Shapes.large
        )
    }
}
