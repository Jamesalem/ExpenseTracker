// data/repository/BudgetRepositoryImpl.kt
package com.example.expensetracker.data.repository

import com.example.expensetracker.data.dao.BudgetDao
import com.example.expensetracker.data.model.Budget
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val dao: BudgetDao
) : BudgetRepository(dao) {

    override fun observeAllBudgets(): Flow<List<Budget>> =
        dao.observeAll()

    override suspend fun getBudgetByPeriod(periodKey: String): Budget? =
        dao.getByPeriodKey(periodKey)

    override suspend fun insertBudget(budget: Budget): Long =
        dao.insert(budget)

    override suspend fun upsertBudget(budget: Budget) =
        dao.upsert(budget)

    override suspend fun deleteBudget(id: Long) =
        dao.delete(id)
}
