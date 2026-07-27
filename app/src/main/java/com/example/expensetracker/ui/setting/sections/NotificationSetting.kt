package com.example.expensetracker.ui.setting.sections

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.expensetracker.R
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.ui.setting.components.SettingItem
import com.example.expensetracker.ui.setting.components.SettingsCard

@Composable
fun NotificationSetting(
    appSettings: AppSettings,
    onNotificationToggle: (Boolean) -> Unit,
    onNotificationSettingsClick: () -> Unit,
    onTestNotificationClick: () -> Unit,
    onSoundPickerClick: () -> Unit,
    onLoggingReminderToggle: (Boolean) -> Unit,
    onLoggingReminderSettingsClick: () -> Unit,
    onPomodoroSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onNotificationToggle(true)
        }
    }

    SettingsCard {
        // 1. General Notifications (Budget/Bills)
        SettingItem(
            title  = "Budget & Bill Alerts",
            action = {
                Switch(
                    checked         = appSettings.enableNotifications,
                    onCheckedChange = { checked ->
                        if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                            
                            if (hasPermission) {
                                onNotificationToggle(true)
                            } else {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            onNotificationToggle(checked)
                        }
                    }
                )
            }
        )

        if (appSettings.enableNotifications) {
            SettingItem(
                title    = "Alert Schedule",
                subtitle = "Summary at ${appSettings.notificationTime}, Weekly on ${appSettings.weeklyReminderDay.name}",
                icon     = Icons.Filled.Notifications,
                onClick  = onNotificationSettingsClick
            )
        }

        // 2. Logging Reminder
        SettingItem(
            title = "Daily Logging Nudge",
            action = {
                Switch(
                    checked = appSettings.loggingReminderEnabled,
                    onCheckedChange = { checked ->
                        if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) onLoggingReminderToggle(true)
                            else permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onLoggingReminderToggle(checked)
                        }
                    }
                )
            }
        )

        if (appSettings.loggingReminderEnabled) {
            SettingItem(
                title = "Reminder Time",
                subtitle = "Notify if no logs by ${appSettings.loggingReminderTime}",
                icon = Icons.Filled.Alarm,
                onClick = onLoggingReminderSettingsClick
            )
        }

        // 3. Timer Settings
        SettingItem(
            title = "Timer & Focus Settings",
            subtitle = "Pomodoro: ${appSettings.pomodoroDurationMinutes}m • ${if (appSettings.timerSoundUri != null) "Custom sound" else "Default sound"}",
            icon = Icons.Filled.Timer,
            onClick = onPomodoroSettingsClick
        )

        SettingItem(
            title = "Pick Timer Sound",
            icon = Icons.Filled.MusicNote,
            onClick = onSoundPickerClick
        )

        OutlinedButton(
            onClick = onTestNotificationClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Send Test Notification Now")
        }
    }
}
