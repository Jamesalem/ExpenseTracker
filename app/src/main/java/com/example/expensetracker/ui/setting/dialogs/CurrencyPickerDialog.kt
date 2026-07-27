package com.example.expensetracker.ui.setting.dialogs

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R
import com.example.expensetracker.data.util.CurrencyHelper

@Composable
fun CurrencyPickerDialog(
    currentCurrency: String,
    onCurrencySelected: (String) -> Unit,
    onConvertHistorical: (String, Double) -> Unit = { _, _ -> },
    onDismiss: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedNewCurrency by remember { mutableStateOf<String?>(null) }
    var exchangeRateText by remember { mutableStateOf("1.0") }
    var convertHistoricalOption by remember { mutableStateOf(false) }

    val all = remember { CurrencyHelper.allCurrencies }
    val filtered = remember(query) {
        if (query.isBlank()) all
        else all.filter {
            it.code.contains(query, true) ||
                    it.name.contains(query, true) ||
                    it.symbol.contains(query, true)
        }
    }

    if (selectedNewCurrency != null) {
        val targetCode = selectedNewCurrency!!
        AlertDialog(
            onDismissRequest = { selectedNewCurrency = null },
            title = { Text("Currency Switch Mode", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "You are switching default currency from $currentCurrency to $targetCode.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { convertHistoricalOption = false }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !convertHistoricalOption,
                            onClick = { convertHistoricalOption = false }
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text("Update Symbol Only", fontWeight = FontWeight.Bold)
                            Text(
                                "Keep numerical amounts unchanged with new $targetCode symbol.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { convertHistoricalOption = true }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = convertHistoricalOption,
                            onClick = { convertHistoricalOption = true }
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text("Convert Historical Balances", fontWeight = FontWeight.Bold)
                            Text(
                                "Multiply existing amounts and budget by exchange rate.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (convertHistoricalOption) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = exchangeRateText,
                            onValueChange = { exchangeRateText = it },
                            label = { Text("Exchange Rate Multiplier (1 $currentCurrency = ? $targetCode)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rate = exchangeRateText.toDoubleOrNull() ?: 1.0
                        if (convertHistoricalOption) {
                            onConvertHistorical(targetCode, rate)
                        } else {
                            onCurrencySelected(targetCode)
                        }
                        selectedNewCurrency = null
                        onDismiss()
                    }
                ) {
                    Text("APPLY CURRENCY")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedNewCurrency = null }) {
                    Text("BACK")
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.select_currency), fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text(stringResource(R.string.search_currency)) },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = true,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { }),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(Modifier.height(16.dp))

                    LazyColumn(Modifier.fillMaxWidth().height(320.dp)) {
                        if (query.isBlank()) {
                            item {
                                Text(
                                    stringResource(R.string.popular_currencies),
                                    style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                )
                            }
                            items(CurrencyHelper.popularCurrencies) { c ->
                                CurrencyListItem(c, c.code == currentCurrency) { selectedCode ->
                                    if (selectedCode != currentCurrency) {
                                        selectedNewCurrency = selectedCode
                                    } else {
                                        onDismiss()
                                    }
                                }
                            }
                            item {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    stringResource(R.string.all_currencies),
                                    style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                )
                            }
                        }
                        items(filtered) { c ->
                            CurrencyListItem(c, c.code == currentCurrency) { selectedCode ->
                                if (selectedCode != currentCurrency) {
                                    selectedNewCurrency = selectedCode
                                } else {
                                    onDismiss()
                                }
                            }
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
}

@Composable
private fun CurrencyListItem(
    currency: CurrencyHelper.CurrencyInfo,
    isSelected: Boolean,
    onSelect: (String) -> Unit
) {
    val bg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface

    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onSelect(currency.code) }
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${currency.symbol} ",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Column(Modifier.weight(1f)) {
            Text(currency.code, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(currency.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (isSelected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}