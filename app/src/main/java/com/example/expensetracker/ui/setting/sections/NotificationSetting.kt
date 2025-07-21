// ui/setting/sections/NotificationSetting.kt
package com.example.expensetracker.ui.setting.sections

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.expensetracker.R
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.ui.setting.components.SettingItem
import com.example.expensetracker.ui.setting.components.SettingsCard

@Composable
fun NotificationSetting(
    appSettings: AppSettings,
    onNotificationToggle: (Boolean) -> Unit,
    onNotificationSettingsClick: () -> Unit
) {
    SettingsCard {
        SettingItem(
            title  = stringResource(R.string.enable_notifications),
            action = {
                Switch(
                    checked         = appSettings.enableNotifications,
                    onCheckedChange = onNotificationToggle
                )
            }
        )

        if (appSettings.enableNotifications) {
            SettingItem(
                title    = stringResource(R.string.notification_settings),
                subtitle = stringResource(
                    R.string.notification_summary,
                    appSettings.notificationTime,
                    appSettings.weeklyReminderDay.name
                ),
                icon     = Icons.Filled.Notifications,
                onClick  = onNotificationSettingsClick
            )
        }
    }
}
