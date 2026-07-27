package com.example.expensetracker.ui.setting.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun LoggingReminderDialog(
    enabled: Boolean,
    reminderTime: String,
    onConfirm: (Boolean, String) -> Unit,
    onDismiss: () -> Unit
) {
    var isEnabled by remember { mutableStateOf(enabled) }
    var time by remember { mutableStateOf(reminderTime) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Daily Logging Reminder") },
        text = {
            Column {
                Text(
                    "Receive a nudge if you haven't logged any expenses by a specific time.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enable Reminder")
                    Switch(checked = isEnabled, onCheckedChange = { isEnabled = it })
                }

                if (isEnabled) {
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it; error = null },
                        label = { Text("Reminder Time (HH:mm)") },
                        isError = error != null,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (isEnabled && !time.matches(Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$"))) {
                    error = "Use 24h format (e.g. 21:00)"
                } else {
                    onConfirm(isEnabled, time)
                    onDismiss()
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
