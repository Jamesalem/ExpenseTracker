package com.example.expensetracker.data.repository

import com.example.expensetracker.data.dao.BudgetDao
import com.example.expensetracker.data.local.ExpenseDao
import com.example.expensetracker.data.model.Budget
import com.example.expensetracker.data.model.Expense
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao
) {
    // ——— Expense APIs ——————————————————————————
    fun getAll(): Flow<List<Expense>> = expenseDao.getAllExpenses()
    fun getById(id: Long): Flow<Expense>     = expenseDao.getExpenseById(id)
    suspend fun add(expense: Expense): Long   = expenseDao.insert(expense)
    suspend fun update(expense: Expense)      = expenseDao.update(expense)
    suspend fun delete(expense: Expense)      = expenseDao.delete(expense)

    // ——— Budget APIs ——————————————————————————
    fun observeBudgets(): Flow<List<Budget>> = budgetDao.observeAll()
    suspend fun upsertBudget(budget: Budget) = budgetDao.upsert(budget)
}
