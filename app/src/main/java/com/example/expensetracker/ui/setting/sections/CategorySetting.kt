// ui/setting/sections/CategorySetting.kt
package com.example.expensetracker.ui.setting.sections

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.expensetracker.R
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.ui.setting.components.SettingItem
import com.example.expensetracker.ui.setting.components.SettingsCard

@Composable
fun CategorySetting(
    appSettings: AppSettings,
    onCategoryClick: () -> Unit
) {
    SettingsCard {
        SettingItem(
            title    = stringResource(R.string.manage_categories),
            subtitle = stringResource(
                R.string.category_count,
                appSettings.customCategories.size
            ),
            icon     = Icons.Filled.Category,
            onClick  = onCategoryClick
        )
    }
}
