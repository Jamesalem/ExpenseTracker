package com.example.expensetracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.expensetracker.data.model.Expense
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ExpenseDao {

    // Observation
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun observeAll(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun observeBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>>

    @Query("SELECT * FROM expenses ORDER BY date DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<Expense>>

    // Single item operations
    @Query("SELECT * FROM expenses WHERE id = :id")
    fun observeById(id: Long): Flow<Expense>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): Expense?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Delete
    suspend fun delete(expense: Expense)

    // Batch operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<Expense>)

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getBetweenDates(startDate: LocalDate, endDate: LocalDate): List<Expense>

    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalSpentBetween(startDate: LocalDate, endDate: LocalDate): Double?

    // Aggregate Observation
    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'INCOME'")
    fun observeTotalIncome(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'EXPENSE'")
    fun observeTotalExpense(): Flow<Double?>

    @Query("DELETE FROM expenses")
    suspend fun deleteAll()

    // Multi-Currency Math Conversion
    @Query("UPDATE expenses SET amount = amount * :rate, currencyCode = :newCurrencyCode")
    suspend fun convertCurrencyAmounts(newCurrencyCode: String, rate: Double)

    // Transactions
    @Transaction
    suspend fun replaceAllExpenses(expenses: List<Expense>) {
        deleteAll()
        insertAll(expenses)
    }

    // Query to count income entries
    @Query("SELECT COUNT(*) FROM expenses WHERE type = 'INCOME'")
    suspend fun countIncomeEntries(): Int
}
