// ui/components/pickers/CurrencyPicker.kt
package com.example.expensetracker.ui.components.pickers

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R
import com.example.expensetracker.data.util.CurrencyFormatter
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
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

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
                    text = CurrencyFormatter.getSymbol(currencyCode),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(Modifier.width(Dimens.small))

            Text(text = currencyCode, style = MaterialTheme.typography.bodyLarge)
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            shape = Shapes.extraLarge
        ) {
            CurrencyPickerBottomSheet(
                currentCurrency = currencyCode,
                onCurrencySelected = {
                    onCurrencySelected(it)
                    showSheet = false
                },
                focusRequester = focusRequester,
                keyboardController = keyboardController
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CurrencyPickerBottomSheet(
    currentCurrency: String,
    onCurrencySelected: (String) -> Unit,
    focusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?
) {
    var query by rememberSaveable { mutableStateOf("") }
    val all = remember { CurrencyHelper.allCurrencies }
    val filtered = remember(query) {
        if (query.isBlank()) all else all.filter {
            it.code.contains(query, true) ||
                    CurrencyFormatter.getDisplayName(it.code).contains(query, true)
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
                capitalization = KeyboardCapitalization.None,  // or your choice
                autoCorrectEnabled = true,                        // explicitly opt in/out
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }
            ),
        )

        Spacer(Modifier.height(Dimens.medium))

        Text(
            text = stringResource(R.string.popular_currencies),
            style = MaterialTheme.typography.labelMedium
        )

        LazyColumn(Modifier.fillMaxWidth().height(200.dp)) {
            stickyHeader {
                // header for popular
                Text(
                    stringResource(R.string.popular_currencies),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(Dimens.small)
                )
            }
            items(CurrencyHelper.popularCurrencies) { c ->
                CurrencyItem(c, c.code == currentCurrency, onClick = { onCurrencySelected(c.code) })
            }
            stickyHeader {
                Text(
                    stringResource(R.string.all_currencies),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(Dimens.small)
                )
            }
            items(filtered) { c ->
                CurrencyItem(c, c.code == currentCurrency, onClick = { onCurrencySelected(c.code) })
            }
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
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
            Text(currency.code, style = MaterialTheme.typography.bodyLarge)
        },
        supportingContent = {
            Text(CurrencyFormatter.getDisplayName(currency.code), style = MaterialTheme.typography.bodyMedium)
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
