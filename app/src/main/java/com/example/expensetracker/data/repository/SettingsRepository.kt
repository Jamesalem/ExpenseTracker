// data/repository/SettingsRepository.kt
package com.example.expensetracker.data.repository

import com.example.expensetracker.data.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    /** Emits the latest merged settings (Room + DataStore) **/
    val appSettings: Flow<AppSettings>

    /** Persist full settings object **/
    suspend fun updateSettings(settings: AppSettings)

    /** Shortcut for currency-only update **/
    suspend fun updateCurrency(currencyCode: String)
}
