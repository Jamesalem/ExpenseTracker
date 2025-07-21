// data/repository/CategoryRepository.kt
package com.example.expensetracker.data.repository

import com.example.expensetracker.data.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: Long): Category?
    suspend fun insertCategory(category: Category): Long
    suspend fun updateCategory(category: Category): Int
    suspend fun deleteCategory(category: Category): Int
}
