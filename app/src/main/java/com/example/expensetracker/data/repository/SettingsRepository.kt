package com.example.expensetracker.data.repository

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val DEFAULT_CURRENCY_KEY = stringPreferencesKey("default_currency")
    }

    /** Flow of the user’s chosen default currency, or null if none set */
    val defaultCurrency: Flow<String?> = dataStore.data
        .map { prefs -> prefs[DEFAULT_CURRENCY_KEY] }

    /** Update the default currency */
    suspend fun setDefaultCurrency(code: String) {
        dataStore.edit { prefs ->
            prefs[DEFAULT_CURRENCY_KEY] = code
        }
    }
}
