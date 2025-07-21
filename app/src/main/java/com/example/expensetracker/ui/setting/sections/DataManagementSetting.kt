// ui/setting/sections/DataManagementSetting.kt
package com.example.expensetracker.ui.setting.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.ui.setting.components.SettingItem
import com.example.expensetracker.ui.setting.components.SettingsCard
import com.example.expensetracker.ui.theme.Shapes

@Composable
fun DataManagementSetting(
    appSettings: AppSettings,
    onBackupClick: () -> Unit,
    onBackupNow: () -> Unit,
    onRestore: () -> Unit
) {
    SettingsCard {
        SettingItem(
            title    = stringResource(R.string.auto_backup),
            subtitle = if (appSettings.autoBackupEnabled)
                appSettings.autoBackupFrequency.displayName
            else
                stringResource(R.string.disabled),
            onClick = onBackupClick
        )

        Row(
            modifier            = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick   = onBackupNow,
                modifier  = Modifier.weight(1f),
                shape     = Shapes.medium
            ) {
                Text(stringResource(R.string.backup_now))
            }

            Button(
                onClick   = onRestore,
                modifier  = Modifier.weight(1f),
                shape     = Shapes.medium
            ) {
                Text(stringResource(R.string.restore))
            }
        }
    }
}
