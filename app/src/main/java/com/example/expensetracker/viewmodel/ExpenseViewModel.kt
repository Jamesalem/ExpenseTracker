package com.example.expensetracker.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.AppDatabase
import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class ExpenseViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.getInstance(app).expenseDao()
    private val repo = ExpenseRepository(dao)

    /** All expenses as StateFlow */
    val expenses: StateFlow<List<Expense>> = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Single expense by ID */
    fun getExpenseById(id: Long): StateFlow<Expense?> =
        repo.getById(id)
            .stateIn(viewModelScope,
                SharingStarted.Lazily,
                initialValue = null)

    /** Add a new expense (with currency) */
    fun addExpense(
        amount: Double,
        currencyCode: String,
        date: Date,
        category: String,
        note: String? = null,
        receiptUri: String? = null
    ) {
        val e = Expense(
            amount = amount,
            currencyCode = currencyCode,
            date = date.time,
            category = category,
            note = note,
            receiptUri = receiptUri
        )
        viewModelScope.launch { repo.add(e) }
    }

    /** Update an existing expense */
    fun updateExpense(expense: Expense) {
        viewModelScope.launch { repo.update(expense) }
    }

    /** Delete */
    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repo.delete(expense) }
    }
}
