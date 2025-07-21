// ui/budget/BudgetDialog.kt
package com.example.expensetracker.ui.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.expensetracker.R
import com.example.expensetracker.data.util.CurrencyFormatter
import com.example.expensetracker.ui.theme.Dimens
import com.example.expensetracker.ui.theme.Shapes

@Composable
fun BudgetDialog(
    currentBudget: Double?,
    currencyCode: String,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var budgetAmount by remember { mutableStateOf(currentBudget?.toString() ?: "") }
    val isValid = budgetAmount.toDoubleOrNull() != null
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = Shapes.extraLarge,
        title = {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        if (currentBudget == null) R.string.set_budget
                        else R.string.edit_budget
                    ),
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close)
                    )
                }
            }
        },
        text = {
            Column(Modifier.padding(Dimens.medium)) {
                OutlinedTextField(
                    value = budgetAmount,
                    onValueChange = {
                        if (it.isEmpty() || it.toDoubleOrNull() != null) {
                            budgetAmount = it
                        }
                    },
                    label = { Text(stringResource(R.string.budget_amount)) },
                    leadingIcon = {
                        Text(
                            text = CurrencyFormatter.getSymbol(currencyCode),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    isError = budgetAmount.isNotEmpty() && !isValid,
                    supportingText = {
                        if (budgetAmount.isNotEmpty() && !isValid) {
                            Text(stringResource(R.string.invalid_amount))
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )

                Spacer(Modifier.height(Dimens.large))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = Dimens.small)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(
                        onClick = {
                            budgetAmount.toDoubleOrNull()?.let(onSave)
                            onDismiss()
                        },
                        enabled = isValid
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        },
        confirmButton = {},  // handled in text slot
        dismissButton = {}   // handled in text slot
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
