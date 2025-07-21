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

// No-op migrations to keep version continuity
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
