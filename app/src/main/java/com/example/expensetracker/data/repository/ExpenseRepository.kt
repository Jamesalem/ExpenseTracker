package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.ExpenseDao
import com.example.expensetracker.data.model.Expense
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val dao: ExpenseDao
) {
    fun getAll(): Flow<List<Expense>> = dao.getAllExpenses()

    /** New: fetch a single expense by its ID */
    fun getById(id: Long): Flow<Expense> = dao.getExpenseById(id)

    suspend fun add(expense: Expense): Long = dao.insert(expense)
    suspend fun update(expense: Expense) = dao.update(expense)
    suspend fun delete(expense: Expense) = dao.delete(expense)
}
