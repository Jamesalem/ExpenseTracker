package com.example.expensetracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.expensetracker.data.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    // *** IMPORTANT FIX: Corrected return type to Long ***
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long

    // *** IMPORTANT FIX: Corrected return type to Int ***
    @Update
    suspend fun update(category: Category): Int

    // *** IMPORTANT FIX: Corrected return type to Int ***
    @Delete
    suspend fun delete(category: Category): Int

    // *** IMPORTANT FIX: Changed id type back to Long for consistency with Expense and autoGenerate ***
    @Query("SELECT * FROM categories WHERE id = :id")
    fun getCategory(id: Long): Flow<Category> // Changed from Int to Long

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun countCategories(): Int
}
