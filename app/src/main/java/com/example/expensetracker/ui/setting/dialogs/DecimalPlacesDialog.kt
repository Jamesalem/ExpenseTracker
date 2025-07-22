// ui/setting/dialogs/DecimalPlacesDialog.kt
package com.example.expensetracker.ui.setting.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R

@Composable
fun DecimalPlacesDialog(
    currentDecimalPlaces: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // Options for decimal places
    val decimalOptions = remember { listOf(0, 1, 2, 3, 4) }
    var selectedOption by remember { mutableIntStateOf(currentDecimalPlaces) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.select_decimal_places), // You'll need to add this string resource
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(Modifier.selectableGroup()) {
                decimalOptions.forEach { numDecimals ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .selectable(
                                selected = (selectedOption == numDecimals),
                                onClick = { selectedOption = numDecimals },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedOption == numDecimals),
                            onClick = null // null recommended for accessibility with screen readers
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(text = numDecimals.toString())
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(selectedOption)
                onDismiss()
            }) {
                Text(stringResource(R.string.confirm)) // You'll need to add this string resource
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        shape = MaterialTheme.shapes.extraLarge // Assuming Shapes is accessible
    )
}