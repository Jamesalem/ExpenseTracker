// data/repository/ExpenseRepositoryImpl.kt
package com.example.expensetracker.data.repository

import com.example.expensetracker.data.dao.ExpenseDao
import com.example.expensetracker.data.model.Expense
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val dao: ExpenseDao
) : ExpenseRepository(dao) {

    override fun observeAllExpenses(): Flow<List<Expense>> =
        dao.observeAll()

    override fun observeExpensesBetweenDates(start: LocalDate, end: LocalDate): Flow<List<Expense>> =
        dao.observeBetweenDates(start, end)

    override fun observeExpenseById(id: Long): Flow<Expense> =
        dao.observeById(id)

    override suspend fun getExpenseById(id: Long): Expense? =
        dao.getById(id)

    override suspend fun insertExpense(expense: Expense): Long =
        dao.insert(expense)

    override suspend fun updateExpense(expense: Expense) {
        dao.update(expense)
    }

    override suspend fun deleteExpense(expense: Expense) {
        dao.delete(expense)
    }

    override suspend fun deleteExpenseById(id: Long) {
        dao.deleteById(id)
    }

    override suspend fun getBetweenDates(start: LocalDate, end: LocalDate): List<Expense> =
        dao.getBetweenDates(start, end)

    override suspend fun replaceAllExpenses(expenses: List<Expense>) =
        dao.replaceAllExpenses(expenses)

    override suspend fun deleteAllExpenses() {
        dao.deleteAll()
    }
}
