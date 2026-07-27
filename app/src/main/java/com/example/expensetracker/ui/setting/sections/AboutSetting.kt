package com.example.expensetracker.ui.setting.sections

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.expensetracker.R
import com.example.expensetracker.ui.setting.components.SettingItem
import com.example.expensetracker.ui.setting.components.SettingsCard

@Composable
fun AboutSetting(
    onPrivacyPolicyClick: () -> Unit
) {
    SettingsCard {
        SettingItem(
            title = "Privacy Policy",
            subtitle = "How we handle your data",
            icon = Icons.Default.PrivacyTip,
            onClick = onPrivacyPolicyClick
        )
        
        SettingItem(
            title = "Version",
            subtitle = "1.0.0 (Production)",
            icon = Icons.Default.Info,
            onClick = { /* Could show a toast or nothing */ }
        )
    }
}
