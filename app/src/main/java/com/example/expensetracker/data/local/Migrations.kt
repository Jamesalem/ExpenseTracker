// data/local/Migrations.kt
package com.example.expensetracker.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.expensetracker.data.model.AppSettings

val Migration1to2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `categories` (
              `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
              `name` TEXT NOT NULL,
              `isCustom` INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent())
    }
}

val Migration2to3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `budgets` (
              `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
              `periodKey` TEXT NOT NULL,
              `amount` REAL NOT NULL
            )
        """.trimIndent())
    }
}

val Migration3to4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) { /* no schema change */ }
}
val Migration4to5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) { /* no schema change */ }
}

val Migration5to6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val s = AppSettings.getDefault()
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `app_settings` (
              `id` INTEGER PRIMARY KEY NOT NULL,
              `defaultCurrency` TEXT NOT NULL,
              `decimalPlaces` INTEGER NOT NULL,
              `useGroupingSeparator` INTEGER NOT NULL,
              `budgetPeriod` TEXT NOT NULL,
              `budgetAmount` REAL NOT NULL,
              `customCategories` TEXT NOT NULL,
              `themeMode` TEXT NOT NULL,
              `enableNotifications` INTEGER NOT NULL,
              `notificationTime` TEXT NOT NULL,
              `weeklyReminderDay` TEXT NOT NULL,
              `useAppLock` INTEGER NOT NULL,
              `appLockPin` TEXT,
              `useBiometrics` INTEGER NOT NULL,
              `lastBackupTimestamp` INTEGER,
              `autoBackupEnabled` INTEGER NOT NULL,
              `autoBackupFrequency` TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            INSERT INTO `app_settings` (
              id, defaultCurrency, decimalPlaces, useGroupingSeparator,
              budgetPeriod, budgetAmount, customCategories, themeMode,
              enableNotifications, notificationTime, weeklyReminderDay,
              useAppLock, appLockPin, useBiometrics, lastBackupTimestamp,
              autoBackupEnabled, autoBackupFrequency
            ) VALUES (
              ${s.id},
              '${s.defaultCurrency}', ${s.decimalPlaces}, ${if (s.useGroupingSeparator) 1 else 0},
              '${s.budgetPeriod.name}', ${s.budgetAmount}, '${s.customCategories.joinToString(",")}',
              '${s.themeMode.name}', ${if (s.enableNotifications) 1 else 0},
              '${s.notificationTime}', '${s.weeklyReminderDay.name}',
              ${if (s.useAppLock) 1 else 0}, ${s.appLockPin?.let{"'$it'"} ?: "NULL"},
              ${if (s.useBiometrics) 1 else 0}, ${s.lastBackupTimestamp ?: "NULL"},
              ${if (s.autoBackupEnabled) 1 else 0}, '${s.autoBackupFrequency.name}'
            )
        """.trimIndent())
    }
}

val Migration6to7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) { /* no schema change */ }
}

val Migration7to8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `time_entries` (
              `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
              `title` TEXT NOT NULL,
              `description` TEXT,
              `category` TEXT NOT NULL DEFAULT 'General',
              `startTimeMillis` INTEGER NOT NULL,
              `endTimeMillis` INTEGER,
              `durationSeconds` INTEGER NOT NULL DEFAULT 0,
              `isRunning` INTEGER NOT NULL DEFAULT 0,
              `isBillable` INTEGER NOT NULL DEFAULT 0,
              `hourlyRate` REAL,
              `associatedExpenseId` INTEGER,
              `dateString` TEXT NOT NULL
            )
        """.trimIndent())
    }
}

val Migration8to9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `subscriptions` (
              `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
              `title` TEXT NOT NULL,
              `amount` REAL NOT NULL,
              `category` TEXT NOT NULL DEFAULT 'Subscriptions',
              `billingCycle" TEXT NOT NULL DEFAULT 'MONTHLY',
              `nextDueDateString` TEXT NOT NULL,
              `note` TEXT,
              `iconName` TEXT NOT NULL DEFAULT 'Subscriptions',
              `isActive` INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent())
    }
}

val Migration9to10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `categories` ADD COLUMN `type` TEXT NOT NULL DEFAULT 'EXPENSE'")
    }
}

val Migration10to11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `app_settings` ADD COLUMN `timerSoundUri` TEXT")
    }
}

val Migration11to12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `app_settings` ADD COLUMN `pomodoroDurationMinutes` INTEGER NOT NULL DEFAULT 25")
    }
}

val Migration12to13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `app_settings` ADD COLUMN `loggingReminderEnabled` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `app_settings` ADD COLUMN `loggingReminderTime` TEXT NOT NULL DEFAULT '21:00'")
    }
}

// NEW: Migration 13 to 14 adds indices for performance boost
val Migration13to14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Indices for Expense table
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_date` ON `expenses` (`date`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_category` ON `expenses` (`category`)")
        
        // Index for Subscription table
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_subscriptions_nextDueDateString` ON `subscriptions` (`nextDueDateString`)")
        
        // Unique Index for Budget table
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_budgets_periodKey` ON `budgets` (`periodKey`)")
        
        // Index for TimeEntry table
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_time_entries_dateString` ON `time_entries` (`dateString`)")
        
        // Unique index for Categories name
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_categories_name` ON `categories` (`name`)")
    }
}
