// ui/setting/sections/AppearanceSetting.kt
package com.example.expensetracker.ui.setting.sections

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.expensetracker.R
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.ui.setting.components.SettingItem
import com.example.expensetracker.ui.setting.components.SettingsCard

@Composable
fun AppearanceSetting(
    appSettings: AppSettings,
    onThemeClick: () -> Unit
) {
    SettingsCard {
        SettingItem(
            title       = stringResource(R.string.theme),
            subtitle    = appSettings.themeMode.displayName,
            icon        = Icons.Filled.DarkMode,
            onClick     = onThemeClick
        )
    }
}
