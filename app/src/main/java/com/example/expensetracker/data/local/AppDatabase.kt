// data/local/AppDatabase.kt
package com.example.expensetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.expensetracker.data.dao.BudgetDao
import com.example.expensetracker.data.dao.CategoryDao
import com.example.expensetracker.data.dao.ExpenseDao
import com.example.expensetracker.data.dao.SettingsDao
import com.example.expensetracker.data.dao.SubscriptionDao
import com.example.expensetracker.data.dao.TimeEntryDao
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.data.model.Budget
import com.example.expensetracker.data.model.Category
import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.model.Subscription
import com.example.expensetracker.data.model.TimeEntry

@Database(
    entities = [
        Expense::class,
        Budget::class,
        Category::class,
        AppSettings::class,
        TimeEntry::class,
        Subscription::class
    ],
    version = 14,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryDao(): CategoryDao
    abstract fun settingsDao(): SettingsDao
    abstract fun timeEntryDao(): TimeEntryDao
    abstract fun subscriptionDao(): SubscriptionDao
}