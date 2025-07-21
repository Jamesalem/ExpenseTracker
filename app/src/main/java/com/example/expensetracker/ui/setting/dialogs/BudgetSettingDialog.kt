// ui/setting/dialogs/BudgetSettingDialog.kt
package com.example.expensetracker.ui.setting.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R
import com.example.expensetracker.data.model.BudgetPeriod

@Composable
fun BudgetSettingDialog(
    currentAmount: Double,
    currentPeriod: BudgetPeriod,
    onConfirm: (Double, BudgetPeriod) -> Unit,
    onDismiss: () -> Unit
) {
    // Pre‑fetch strings
    val titleText     = stringResource(R.string.budget_settings)
    val amountLabel   = stringResource(R.string.budget_amount)
    val periodLabel   = stringResource(R.string.budget_period)
    val invalidAmtMsg = stringResource(R.string.invalid_budget_amount)
    val saveText      = stringResource(R.string.save)
    val cancelText    = stringResource(R.string.cancel)

    var amountText by remember { mutableStateOf(currentAmount.toString()) }
    var period     by remember { mutableStateOf(currentPeriod) }
    var errorText  by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text(titleText) },
        text    = {
            Column {
                OutlinedTextField(
                    value           = amountText,
                    onValueChange   = { amountText = it; errorText = null },
                    label           = { Text(amountLabel) },
                    isError         = errorText != null,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { /* hide keyboard */ }),
                    modifier        = Modifier.fillMaxWidth()
                )
                errorText?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text  = periodLabel,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(BudgetPeriod.entries) { p ->
                        FilterChip(
                            selected = period == p,
                            onClick  = { period = p },
                            label    = { Text(p.displayName) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor     = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amountText.toDoubleOrNull()
                if (amt == null || amt <= 0.0) {
                    errorText = invalidAmtMsg
                } else {
                    onConfirm(amt, period)
                    onDismiss()
                }
            }) {
                Text(saveText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(cancelText)
            }
        }
    )
}
