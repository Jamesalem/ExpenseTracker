// data/local/AppDatabase.kt
package com.example.expensetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.expensetracker.data.dao.BudgetDao
import com.example.expensetracker.data.dao.CategoryDao
import com.example.expensetracker.data.dao.ExpenseDao
import com.example.expensetracker.data.dao.SettingsDao
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.data.model.Budget
import com.example.expensetracker.data.model.Category
import com.example.expensetracker.data.model.Expense

@Database(
    entities = [
        Expense::class,
        Budget::class,
        Category::class,
        AppSettings::class
    ],
    // *** IMPORTANT CHANGE: Increment database version ***
    version = 7,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryDao(): CategoryDao
    abstract fun settingsDao(): SettingsDao
}