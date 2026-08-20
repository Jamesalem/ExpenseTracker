package com.example.expensetracker.ui.setting.sections

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.runtime.Composable
import com.example.expensetracker.ui.setting.components.SettingItem
import com.example.expensetracker.ui.setting.components.SettingsCard

@Composable
fun AboutSetting(
    onPrivacyPolicyClick: () -> Unit,
    onTermsOfServiceClick: () -> Unit = {}
) {
    SettingsCard {
        SettingItem(
            title = "Privacy Policy",
            subtitle = "100% offline & zero-data collection guarantee",
            icon = Icons.Default.PrivacyTip,
            onClick = onPrivacyPolicyClick
        )

        SettingItem(
            title = "Terms of Service",
            subtitle = "Usage terms & financial calculation disclaimers",
            icon = Icons.Default.Description,
            onClick = onTermsOfServiceClick
        )
        
        SettingItem(
            title = "Version",
            subtitle = "1.0.0 (Production Release)",
            icon = Icons.Default.Info,
            onClick = { /* Could show a toast or nothing */ }
        )
    }
}
