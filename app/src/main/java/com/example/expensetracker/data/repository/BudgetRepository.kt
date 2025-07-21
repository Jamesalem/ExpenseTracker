// data/repository/BudgetRepository.kt
package com.example.expensetracker.data.repository

import com.example.expensetracker.data.dao.BudgetDao
import com.example.expensetracker.data.model.Budget
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class BudgetRepository @Inject constructor(
    protected val budgetDao: BudgetDao
) {
    open fun observeAllBudgets(): Flow<List<Budget>> =
        budgetDao.observeAll()

    open suspend fun getBudgetByPeriod(periodKey: String): Budget? =
        budgetDao.getByPeriodKey(periodKey)

    open suspend fun insertBudget(budget: Budget): Long =
        budgetDao.insert(budget)

    open suspend fun upsertBudget(budget: Budget) =
        budgetDao.upsert(budget)

    open suspend fun deleteBudget(id: Long) =
        budgetDao.delete(id)
}
