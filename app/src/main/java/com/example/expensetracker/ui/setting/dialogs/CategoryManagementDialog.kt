// ui/setting/dialogs/CategoryManagementDialog.kt
package com.example.expensetracker.ui.setting.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R

@Composable
fun CategoryManagementDialog(
    categories: List<String>,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var newCat by remember { mutableStateOf("") }
    val currentCats = remember { mutableStateListOf<String>().apply { addAll(categories) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text(stringResource(R.string.manage_categories)) },
        text    = {
            Column {
                LazyColumn(Modifier.height(200.dp)) {
                    items(currentCats) { cat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier           = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                cat,
                                style    = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { currentCats.remove(cat) }) {
                                Icon(
                                    imageVector    = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.remove_category),
                                    tint           = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value         = newCat,
                        onValueChange = { newCat = it },
                        label         = { Text(stringResource(R.string.new_category)) },
                        modifier      = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        if (newCat.isNotBlank() && newCat !in currentCats) {
                            currentCats.add(newCat)
                            newCat = ""
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_category),
                            tint        = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(currentCats.toList())
                onDismiss()
            }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
