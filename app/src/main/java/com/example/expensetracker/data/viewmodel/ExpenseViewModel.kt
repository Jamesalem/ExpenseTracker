package com.example.expensetracker.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repo: ExpenseRepository
) : ViewModel() {
    val expenses: StateFlow<List<Expense>> =
        repo.getAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun getExpenseById(id: Long): StateFlow<Expense?> =
        repo.getById(id)
            .map { it }
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    /** Now takes `dateMillis: Long` */
    fun addExpense(
        amount: Double,
        currencyCode: String,
        dateMillis: Long,
        category: String,
        note: String? = null,
        receiptUri: String? = null
    ) {
        val e = Expense(
            amount = amount,
            currencyCode = currencyCode,
            date = dateMillis,
            category = category,
            note = note,
            receiptUri = receiptUri
        )
        viewModelScope.launch { repo.add(e) }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch { repo.update(expense) }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repo.delete(expense) }
    }
}
