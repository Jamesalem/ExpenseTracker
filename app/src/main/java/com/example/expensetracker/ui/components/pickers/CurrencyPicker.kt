package com.example.expensetracker.ui.components.pickers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R
import com.example.expensetracker.data.util.CurrencyHelper
import com.example.expensetracker.ui.theme.Dimens
import com.example.expensetracker.ui.theme.Shapes
import com.example.expensetracker.ui.theme.generateCategoryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyPicker(
    currencyCode: String,
    onCurrencySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val currencySymbol = remember(currencyCode) {
        CurrencyHelper.allCurrencies.firstOrNull { it.code == currencyCode }?.symbol
            ?: currencyCode
    }

    Surface(
        modifier = modifier
            .clip(Shapes.medium)
            .clickable { showSheet = true }
            .padding(Dimens.extraSmall),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            Modifier.padding(horizontal = Dimens.medium, vertical = Dimens.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(Dimens.iconM)
                    .clip(CircleShape)
                    .background(generateCategoryColor(currencyCode.hashCode().toLong()))
                    .padding(Dimens.extraSmall),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currencySymbol,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(Modifier.width(Dimens.small))

            Text(text = currencyCode, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            shape = Shapes.extraLarge
        ) {
            CurrencyPickerContent(
                currentCurrency = currencyCode,
                onCurrencySelected = {
                    onCurrencySelected(it)
                    showSheet = false
                }
            )
        }
    }
}

@Composable
private fun CurrencyPickerContent(
    currentCurrency: String,
    onCurrencySelected: (String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val all = remember { CurrencyHelper.allCurrencies }
    val filtered = remember(query) {
        if (query.isBlank()) all else all.filter {
            it.code.contains(query, true) ||
                    it.name.contains(query, true) ||
                    it.symbol.contains(query, true)
        }
    }

    Column(Modifier.padding(Dimens.medium)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text(stringResource(R.string.search_currencies)) },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            },
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

        Spacer(Modifier.height(Dimens.medium))

        LazyColumn(Modifier.fillMaxWidth().height(300.dp)) {
            if (query.isBlank()) {
                item {
                    Text(
                        stringResource(R.string.popular_currencies),
                        style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.padding(vertical = Dimens.small)
                    )
                }
                items(CurrencyHelper.popularCurrencies) { c ->
                    CurrencyItem(c, c.code == currentCurrency, onClick = { onCurrencySelected(c.code) })
                }
                item {
                    Spacer(Modifier.height(Dimens.small))
                    Text(
                        stringResource(R.string.all_currencies),
                        style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.padding(vertical = Dimens.small)
                    )
                }
            }
            items(filtered) { c ->
                CurrencyItem(c, c.code == currentCurrency, onClick = { onCurrencySelected(c.code) })
            }
        }
    }
}

@Composable
private fun CurrencyItem(
    currency: CurrencyHelper.CurrencyInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface

    ListItem(
        headlineContent = {
            Text("${currency.symbol}  ${currency.code}", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
        },
        supportingContent = {
            Text(currency.name, style = MaterialTheme.typography.bodyMedium)
        },
        trailingContent = {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(bg)
    )
}