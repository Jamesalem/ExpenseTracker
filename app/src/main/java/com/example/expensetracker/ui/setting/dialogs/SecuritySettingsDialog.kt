// ui/setting/dialogs/SecuritySettingsDialog.kt
package com.example.expensetracker.ui.setting.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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

@Composable
fun SecuritySettingsDialog(
    useAppLock: Boolean,
    useBiometrics: Boolean,
    onConfirm: (Boolean, Boolean) -> Unit,
    onDismiss: () -> Unit,
    onSetupPin: () -> Unit
) {
    var lockEnabled   by remember { mutableStateOf(useAppLock) }
    var biometricEnabled by remember { mutableStateOf(useBiometrics) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text(stringResource(R.string.security_settings)) },
        text    = {
            Column {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment   = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.enable_app_lock))
                    Switch(checked = lockEnabled, onCheckedChange = { lockEnabled = it })
                }

                if (lockEnabled) {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment   = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.use_biometrics))
                        Switch(checked = biometricEnabled, onCheckedChange = { biometricEnabled = it })
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onSetupPin, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.setup_pin))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(lockEnabled, biometricEnabled)
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
