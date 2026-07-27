// data/local/InitialDataPopulator.kt
package com.example.expensetracker.data.local

import androidx.annotation.WorkerThread
import com.example.expensetracker.data.dao.CategoryDao
import com.example.expensetracker.data.dao.SettingsDao
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.data.model.Category
import com.example.expensetracker.data.model.Category.CategoryType
import javax.inject.Inject

class InitialDataPopulator @Inject constructor(
    private val categoryDao: CategoryDao,
    private val settingsDao: SettingsDao
) {
    @WorkerThread
    suspend fun populateInitialData() {
        if (categoryDao.countCategories() == 0) {
            val defaultCategories = listOf(
                // Preset Income Categories
                Category(name = "Salary & Wages", isCustom = false, type = CategoryType.INCOME),
                Category(name = "Freelance & Consulting", isCustom = false, type = CategoryType.INCOME),
                Category(name = "Investments & Dividends", isCustom = false, type = CategoryType.INCOME),
                Category(name = "Business Profits", isCustom = false, type = CategoryType.INCOME),
                Category(name = "Gifts & Grants", isCustom = false, type = CategoryType.INCOME),
                Category(name = "Rental Income", isCustom = false, type = CategoryType.INCOME),
                Category(name = "Refunds & Claims", isCustom = false, type = CategoryType.INCOME),
                Category(name = "Other Income", isCustom = false, type = CategoryType.INCOME),

                // Preset Expense Categories
                Category(name = "Food & Dining", isCustom = false, type = CategoryType.EXPENSE),
                Category(name = "Transportation", isCustom = false, type = CategoryType.EXPENSE),
                Category(name = "Housing & Rent", isCustom = false, type = CategoryType.EXPENSE),
                Category(name = "Utilities & Bills", isCustom = false, type = CategoryType.EXPENSE),
                Category(name = "Healthcare & Medical", isCustom = false, type = CategoryType.EXPENSE),
                Category(name = "Entertainment & Subs", isCustom = false, type = CategoryType.EXPENSE),
                Category(name = "Shopping & Apparel", isCustom = false, type = CategoryType.EXPENSE),
                Category(name = "Education & Courses", isCustom = false, type = CategoryType.EXPENSE),
                Category(name = "Travel & Vacation", isCustom = false, type = CategoryType.EXPENSE),
                Category(name = "Other Expense", isCustom = false, type = CategoryType.EXPENSE)
            )
            defaultCategories.forEach { categoryDao.insert(it) }
        }

        if (settingsDao.getSettings() == null) {
            settingsDao.saveSettings(AppSettings.getDefault())
        }
    }
}
