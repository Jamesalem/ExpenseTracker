// ui/setting/dialogs/CurrencyPickerDialog.kt
package com.example.expensetracker.ui.setting.dialogs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R
import com.example.expensetracker.data.util.CurrencyFormatter
import com.example.expensetracker.data.util.CurrencyHelper

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CurrencyPickerDialog(
    currentCurrency: String,
    onCurrencySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val all = remember { CurrencyHelper.allCurrencies }
    val filtered = remember(query) {
        if (query.isBlank()) all
        else all.filter {
            it.code.contains(query, true) ||
                    CurrencyFormatter.getDisplayName(it.code).contains(query, true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text(stringResource(R.string.select_currency)) },
        text    = {
            Column(Modifier.fillMaxWidth().padding(8.dp)) {
                OutlinedTextField(
                    value            = query,
                    onValueChange    = { query = it },
                    label            = { Text(stringResource(R.string.search_currency)) },
                    keyboardOptions  = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = true,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions  = KeyboardActions(onDone = { /* hide keyboard */ }),
                    modifier         = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                LazyColumn(Modifier.height(300.dp)) {
                    stickyHeader {
                        Text(
                            stringResource(R.string.popular_currencies),
                            style    = MaterialTheme.typography.labelMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(8.dp)
                        )
                    }
                    items(CurrencyHelper.popularCurrencies) { c ->
                        CurrencyListItem(c, c.code == currentCurrency, onCurrencySelected, onDismiss)
                    }

                    stickyHeader {
                        Text(
                            stringResource(R.string.all_currencies),
                            style    = MaterialTheme.typography.labelMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(8.dp)
                        )
                    }
                    items(filtered) { c ->
                        CurrencyListItem(c, c.code == currentCurrency, onCurrencySelected, onDismiss)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun CurrencyListItem(
    currency: CurrencyHelper.CurrencyInfo,
    isSelected: Boolean,
    pick: (String) -> Unit,
    dismiss: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable {
                pick(currency.code)
                dismiss()
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(currency.code, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        if (isSelected) {
            Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}
