// AppSettings.kt
package com.example.expensetracker.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek

@Entity(tableName = "app_settings")
@Immutable
data class AppSettings(
    @PrimaryKey val id: Long = 1L,

    val hasCompletedOnboarding: Boolean = false,

    // Currency Settings
    var defaultCurrency: String = "USD",
    var decimalPlaces: Int = 2,
    val useGroupingSeparator: Boolean = true,

    // Budget Settings
    val budgetPeriod: BudgetPeriod = BudgetPeriod.MONTHLY,
    val budgetAmount: Double = 0.0,

    // Categories
    val customCategories: List<String> = emptyList(),

    // Theme
    val themeMode: ThemeMode = ThemeMode.SYSTEM,

    // Notifications
    val enableNotifications: Boolean = true,
    val notificationTime: String = "20:00",  // HH:mm for Budget Summary
    val weeklyReminderDay: DayOfWeek = DayOfWeek.MONDAY,
    
    // Daily Logging Reminder
    val loggingReminderEnabled: Boolean = false,
    val loggingReminderTime: String = "21:00", // HH:mm
    
    // Timer Settings
    val timerSoundUri: String? = null,
    val pomodoroDurationMinutes: Int = 25,

    // Security
    val useAppLock: Boolean = false,
    val appLockPin: String? = null,
    val useBiometrics: Boolean = false,

    // Data Management
    val lastBackupTimestamp: Long? = null,
    val autoBackupEnabled: Boolean = false,
    val autoBackupFrequency: BackupFrequency = BackupFrequency.WEEKLY
) {
    init {
        require(notificationTime.matches(Regex("^([01]?\\d|2[0-3]):[0-5]\\d\$"))) {
            "Invalid notification time format. Use HH:mm 24‑hour"
        }
        require(loggingReminderTime.matches(Regex("^([01]?\\d|2[0-3]):[0-5]\\d\$"))) {
            "Invalid logging reminder time format. Use HH:mm 24‑hour"
        }
    }

    companion object {
        fun getDefault() = AppSettings()
    }
}

enum class BackupFrequency(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}

enum class ThemeMode(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System Default")
}

enum class BudgetPeriod(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}
