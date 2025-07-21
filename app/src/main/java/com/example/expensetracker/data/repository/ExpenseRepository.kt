// data/repository/ExpenseRepository.kt
package com.example.expensetracker.data.repository

import com.example.expensetracker.data.dao.ExpenseDao
import com.example.expensetracker.data.model.Expense
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class ExpenseRepository @Inject constructor(
    protected val expenseDao: ExpenseDao
) {
    /** Observers **/
    open fun observeAllExpenses(): Flow<List<Expense>> =
        expenseDao.observeAll()

    open fun observeExpensesBetweenDates(start: LocalDate, end: LocalDate): Flow<List<Expense>> =
        expenseDao.observeBetweenDates(start, end)

    open fun observeExpenseById(id: Long): Flow<Expense> =
        expenseDao.observeById(id)

    /** CRUD **/
    open suspend fun getExpenseById(id: Long): Expense? =
        expenseDao.getById(id)

    open suspend fun insertExpense(expense: Expense): Long =
        expenseDao.insert(expense)

    open suspend fun updateExpense(expense: Expense) {
        expenseDao.update(expense)
    }

    open suspend fun deleteExpense(expense: Expense) {
        expenseDao.delete(expense)
    }

    open suspend fun deleteExpenseById(id: Long) {
        expenseDao.deleteById(id)
    }

    /** Batch **/
    open suspend fun getBetweenDates(start: LocalDate, end: LocalDate): List<Expense> =
        expenseDao.getBetweenDates(start, end)

    open suspend fun replaceAllExpenses(expenses: List<Expense>) =
        expenseDao.replaceAllExpenses(expenses)

    open suspend fun deleteAllExpenses() =
        expenseDao.deleteAll()
}
