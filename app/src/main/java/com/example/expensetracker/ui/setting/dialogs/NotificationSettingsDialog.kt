// ui/setting/dialogs/NotificationSettingsDialog.kt
package com.example.expensetracker.ui.setting.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R
import java.time.DayOfWeek

// ui/setting/dialogs/NotificationSettingsDialog.kt
@Composable
fun NotificationSettingsDialog(
    enableNotifications: Boolean,
    notificationTime: String,
    weeklyReminderDay: DayOfWeek,
    onConfirm: (Boolean, String, DayOfWeek) -> Unit,
    onDismiss: () -> Unit
) {
    // Lifted strings
    val titleText        = stringResource(R.string.notification_settings)
    val enableLabel      = stringResource(R.string.enable_notifications)
    val timeLabel        = stringResource(R.string.notification_time)
    val dayLabel         = stringResource(R.string.weekly_reminder_day)
    val invalidTimeMsg   = stringResource(R.string.invalid_time_format)
    val saveText         = stringResource(R.string.save)
    val cancelText       = stringResource(R.string.cancel)

    var enabled by remember { mutableStateOf(enableNotifications) }
    var time    by remember { mutableStateOf(notificationTime) }
    var day     by remember { mutableStateOf(weeklyReminderDay) }
    var error   by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text(titleText) },
        text    = {
            Column {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment   = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(enableLabel)
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }

                if (enabled) {
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value            = time,
                        onValueChange    = { time = it; error = null },
                        label            = { Text(timeLabel) },
                        isError          = error != null,
                        keyboardOptions  = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        modifier         = Modifier.fillMaxWidth()
                    )
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                    Spacer(Modifier.height(16.dp))

                    Text(dayLabel, style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(DayOfWeek.entries) { weekday ->  // use .entries
                            FilterChip(
                                selected = day == weekday,
                                onClick  = { day = weekday },
                                label    = { Text(weekday.name) },
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor     = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (enabled && !time.matches(Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$"))) {
                    error = invalidTimeMsg
                } else {
                    onConfirm(enabled, time, day)
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

