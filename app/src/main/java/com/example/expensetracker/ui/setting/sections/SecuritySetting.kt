// ui/setting/sections/SecuritySetting.kt
package com.example.expensetracker.ui.setting.sections

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.expensetracker.R
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.ui.setting.components.SettingItem
import com.example.expensetracker.ui.setting.components.SettingsCard

@Composable
fun SecuritySetting(
    appSettings: AppSettings,
    onAppLockToggle: (Boolean) -> Unit,
    onBiometricsToggle: (Boolean) -> Unit,
    onSecuritySettingsClick: () -> Unit
) {
    SettingsCard {
        SettingItem(
            title = stringResource(R.string.enable_app_lock),
            action = {
                Switch(
                    checked         = appSettings.useAppLock,
                    onCheckedChange = onAppLockToggle,
                    colors          = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        )

        if (appSettings.useAppLock) {
            SettingItem(
                title = stringResource(R.string.use_biometrics),
                action = {
                    Switch(
                        checked         = appSettings.useBiometrics,
                        onCheckedChange = onBiometricsToggle,
                        colors          = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            )

            SettingItem(
                title    = stringResource(R.string.security_settings),
                subtitle = if (appSettings.appLockPin != null)
                    stringResource(R.string.pin_set)
                else
                    stringResource(R.string.no_pin_set),
                icon     = Icons.Filled.Security,
                onClick  = onSecuritySettingsClick
            )
        }
    }
}
