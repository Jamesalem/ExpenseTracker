// ui/setting/sections/CurrencySetting.kt
package com.example.expensetracker.ui.setting.sections

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
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
fun CurrencySetting(
    appSettings: AppSettings,
    onCurrencyClick: () -> Unit,
    onDecimalClick: () -> Unit,
    onGroupingSeparatorChange: (Boolean) -> Unit
) {
    SettingsCard {
        SettingItem(
            title    = stringResource(R.string.default_currency),
            subtitle = appSettings.defaultCurrency,
            icon     = Icons.Filled.CurrencyExchange,
            onClick  = onCurrencyClick
        )

        SettingItem(
            title   = stringResource(R.string.decimal_places),
            subtitle= appSettings.decimalPlaces.toString(),
            onClick = onDecimalClick
        )

        SettingItem(
            title = stringResource(R.string.use_grouping_separators),
            action = {
                Switch(
                    checked         = appSettings.useGroupingSeparator,
                    onCheckedChange = onGroupingSeparatorChange,
                    colors          = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        )
    }
}
