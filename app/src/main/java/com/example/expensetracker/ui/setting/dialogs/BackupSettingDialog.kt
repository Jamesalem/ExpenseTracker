// ui/setting/dialogs/BackupSettingDialog.kt
package com.example.expensetracker.ui.setting.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R
import com.example.expensetracker.data.model.BackupFrequency

@Composable
fun BackupSettingDialog(
    enabled: Boolean,
    frequency: BackupFrequency,
    onConfirm: (Boolean, BackupFrequency) -> Unit,
    onDismiss: () -> Unit
) {
    val titleText  = stringResource(R.string.auto_backup_settings)
    val enableLabel= stringResource(R.string.enable_auto_backup)
    val freqLabel  = stringResource(R.string.backup_frequency)
    val saveText   = stringResource(R.string.save)
    val cancelText = stringResource(R.string.cancel)

    var autoBackupEnabled by remember { mutableStateOf(enabled) }
    var backupFrequency   by remember { mutableStateOf(frequency) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text(titleText) },
        text    = {
            Column {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(enableLabel)
                    Switch(
                        checked         = autoBackupEnabled,
                        onCheckedChange = { autoBackupEnabled = it }
                    )
                }

                if (autoBackupEnabled) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text     = freqLabel,
                        style    = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyRow(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(BackupFrequency.entries) { freq ->
                            FilterChip(
                                selected = backupFrequency == freq,
                                onClick  = { backupFrequency = freq },
                                label    = { Text(freq.displayName) },
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
                onConfirm(autoBackupEnabled, backupFrequency)
                onDismiss()
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
