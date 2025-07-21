// data/repository/CategoryRepositoryImpl.kt
package com.example.expensetracker.data.repository

import com.example.expensetracker.data.dao.CategoryDao
import com.example.expensetracker.data.model.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> =
        dao.getAllCategories()

    override suspend fun getCategoryById(id: Long): Category? =
        dao.getCategoryById(id)

    override suspend fun insertCategory(category: Category): Long =
        dao.insert(category)

    override suspend fun updateCategory(category: Category): Int =
        dao.update(category)

    override suspend fun deleteCategory(category: Category): Int =
        dao.delete(category)
}
