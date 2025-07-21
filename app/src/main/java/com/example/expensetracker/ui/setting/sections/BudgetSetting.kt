// ui/setting/sections/BudgetSetting.kt
package com.example.expensetracker.ui.setting.sections

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Savings
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.expensetracker.R
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.ui.setting.components.SettingItem
import com.example.expensetracker.ui.setting.components.SettingsCard

@Composable
fun BudgetSetting(
    appSettings: AppSettings,
    onBudgetClick: () -> Unit
) {
    SettingsCard {
        SettingItem(
            title    = stringResource(R.string.budget_settings),
            subtitle = "${appSettings.budgetAmount} ${appSettings.budgetPeriod.displayName}",
            icon     = Icons.Filled.Savings,
            onClick  = onBudgetClick
        )
    }
}
