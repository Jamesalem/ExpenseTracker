// data/repository/SettingsRepositoryImpl.kt
package com.example.expensetracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.expensetracker.data.dao.SettingsDao
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.data.model.ThemeMode
import com.example.expensetracker.data.util.PreferenceKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.DayOfWeek
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDao: SettingsDao,
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override val appSettings: Flow<AppSettings> =
        combine(
            settingsDao.observeSettings(),
            dataStore.data
        ) { dbSettings, prefs ->
            val base = dbSettings ?: AppSettings.getDefault()

            // Merge DataStore preferences over the database values
            // We prioritize DataStore for security and UI preferences
            base.copy(
                themeMode = prefs[PreferenceKeys.THEME_MODE]?.let { ThemeMode.valueOf(it) }
                    ?: base.themeMode,
                enableNotifications = prefs[PreferenceKeys.ENABLE_NOTIFICATIONS]
                    ?: base.enableNotifications,
                notificationTime = prefs[PreferenceKeys.NOTIFICATION_TIME]
                    ?: base.notificationTime,
                weeklyReminderDay = prefs[PreferenceKeys.WEEKLY_REMINDER_DAY]?.let { DayOfWeek.valueOf(it) }
                    ?: base.weeklyReminderDay,
                useAppLock = prefs[PreferenceKeys.USE_APP_LOCK] ?: base.useAppLock,
                appLockPin = prefs[PreferenceKeys.APP_LOCK_PIN], // DataStore is the source of truth for PIN
                useBiometrics = prefs[PreferenceKeys.USE_BIOMETRICS] ?: base.useBiometrics,
                defaultCurrency = base.defaultCurrency
            )
        }

    override suspend fun updateSettings(settings: AppSettings) {
        // Persist full object in Room for backup/general tracking
        settingsDao.saveSettings(settings)

        // Persist critical prefs in DataStore
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.THEME_MODE] = settings.themeMode.name
            prefs[PreferenceKeys.ENABLE_NOTIFICATIONS] = settings.enableNotifications
            prefs[PreferenceKeys.NOTIFICATION_TIME] = settings.notificationTime
            prefs[PreferenceKeys.WEEKLY_REMINDER_DAY] = settings.weeklyReminderDay.name
            prefs[PreferenceKeys.USE_APP_LOCK] = settings.useAppLock
            prefs[PreferenceKeys.USE_BIOMETRICS] = settings.useBiometrics
            
            // Fix: Explicitly handle PIN removal
            if (settings.appLockPin != null) {
                prefs[PreferenceKeys.APP_LOCK_PIN] = settings.appLockPin!!
            } else {
                prefs.remove(PreferenceKeys.APP_LOCK_PIN)
            }
        }
    }

    override suspend fun updateCurrency(currencyCode: String) {
        val current = settingsDao.getSettings() ?: AppSettings.getDefault()
        settingsDao.saveSettings(current.copy(defaultCurrency = currencyCode))
    }
}
