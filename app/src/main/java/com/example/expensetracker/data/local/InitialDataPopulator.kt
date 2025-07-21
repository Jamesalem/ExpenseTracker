// data/local/InitialDataPopulator.kt
package com.example.expensetracker.data.local

import androidx.annotation.WorkerThread
import com.example.expensetracker.data.dao.CategoryDao
import com.example.expensetracker.data.dao.SettingsDao
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.data.model.Category
import javax.inject.Inject

class InitialDataPopulator @Inject constructor(
    private val categoryDao: CategoryDao,
    private val settingsDao: SettingsDao
) {
    @WorkerThread
    suspend fun populateInitialData() {
        // Only insert default categories if the category table is empty
        // This prevents re-inserting on subsequent app launches after initial setup
        if (categoryDao.countCategories() == 0) { // NEW: Check if categories exist
            val defaultCategories = listOf(
                Category(name = "Food", isCustom = false),
                Category(name = "Transport", isCustom = false),
                Category(name = "Housing", isCustom = false),
                Category(name = "Utilities", isCustom = false),
                Category(name = "Healthcare", isCustom = false),
                Category(name = "Entertainment", isCustom = false),
                Category(name = "Shopping", isCustom = false),
                Category(name = "Education", isCustom = false)
            )
            defaultCategories.forEach { categoryDao.insert(it) }
        }

        // Default settings if none exist
        if (settingsDao.getSettings() == null) {
            settingsDao.saveSettings(AppSettings.getDefault())
        }
    }
}
