// data/util/SettingsDataStore.kt
package com.example.expensetracker.data.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.expensetracker.data.dao.SettingsDao
import com.example.expensetracker.data.model.AppSettings

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

object PreferenceKeys {
    // Theme
    val THEME_MODE            = stringPreferencesKey("theme_mode")

    // Notifications
    val ENABLE_NOTIFICATIONS  = booleanPreferencesKey("enable_notifications")
    val NOTIFICATION_TIME     = stringPreferencesKey("notification_time")
    val WEEKLY_REMINDER_DAY   = stringPreferencesKey("weekly_reminder_day")

    // Security
    val USE_APP_LOCK          = booleanPreferencesKey("use_app_lock")
    val APP_LOCK_PIN          = stringPreferencesKey("app_lock_pin")
    val USE_BIOMETRICS        = booleanPreferencesKey("use_biometrics")

    // Legacy Currency (for migration only)
    val DEFAULT_CURRENCY      = stringPreferencesKey("default_currency")
}

object SettingsMigrations {
    /**
     * Migrates any old prefs into Room, then removes them.
     */
    suspend fun applyMigrations(
        dataStore: DataStore<Preferences>,
        settingsDao: SettingsDao
    ) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.DEFAULT_CURRENCY]?.let { currency ->
                val current = settingsDao.getSettings() ?: AppSettings.getDefault()
                // saveSettings replaces via REPLACE strategy
                settingsDao.saveSettings(current.copy(defaultCurrency = currency))
                prefs.remove(PreferenceKeys.DEFAULT_CURRENCY)
            }
            if (!prefs.contains(PreferenceKeys.THEME_MODE)) {
                prefs[PreferenceKeys.THEME_MODE] = AppSettings.getDefault().themeMode.name
            }
        }
    }
}

suspend fun DataStore<Preferences>.runDataStoreMigrations(settingsDao: SettingsDao) {
    SettingsMigrations.applyMigrations(this, settingsDao)
}
