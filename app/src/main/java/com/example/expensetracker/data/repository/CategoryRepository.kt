package com.example.expensetracker.data.repository

import com.example.expensetracker.data.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    // *** IMPORTANT FIX: Changed id type back to Long ***
    fun getCategory(id: Long): Flow<Category>
    suspend fun insertCategory(category: Category): Long
    suspend fun updateCategory(category: Category): Int
    suspend fun deleteCategory(category: Category): Int
}
