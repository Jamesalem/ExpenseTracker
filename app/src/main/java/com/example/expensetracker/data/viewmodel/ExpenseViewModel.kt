package com.example.expensetracker.data.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.model.Budget
import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repo: ExpenseRepository
) : ViewModel() {

    // ——— Existing Expenses ————————————————————————
    val expenses: StateFlow<List<Expense>> =
        repo.getAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Changed to return Flow instead of StateFlow
    fun getExpenseById(id: Long): Flow<Expense?> = repo.getById(id)

    // Form state - properly exposed as State objects
    private val _formAmount = mutableStateOf(0.0)
    val formAmount: State<Double> = _formAmount

    private val _formCurrencyCode = mutableStateOf("USD")
    val formCurrencyCode: State<String> = _formCurrencyCode

    private val _formCategory = mutableStateOf("")
    val formCategory: State<String> = _formCategory

    private val _formNote = mutableStateOf("")
    val formNote: State<String> = _formNote

    private val _formReceiptUri = mutableStateOf<String?>(null)
    val formReceiptUri: State<String?> = _formReceiptUri

    // Initialize form with default values
    fun initForm() {
        _formAmount.value = 0.0
        _formCurrencyCode.value = "USD"
        _formCategory.value = ""
        _formNote.value = ""
        _formReceiptUri.value = null
    }

    // Initialize form with existing expense values
    fun initForm(expense: Expense) {
        _formAmount.value = expense.amount
        _formCurrencyCode.value = expense.currencyCode
        _formCategory.value = expense.category
        _formNote.value = expense.note ?: ""
        _formReceiptUri.value = expense.receiptUri
    }

    fun updateFormAmount(amount: Double) {
        _formAmount.value = amount
    }

    fun updateFormCurrencyCode(currencyCode: String) {
        _formCurrencyCode.value = currencyCode
    }

    fun updateFormCategory(category: String) {
        _formCategory.value = category
    }

    fun updateFormNote(note: String) {
        _formNote.value = note
    }

    fun updateFormReceiptUri(uri: String?) {
        _formReceiptUri.value = uri
    }

    // Add expense using form state
    fun addExpense(dateMillis: Long) {
        val e = Expense(
            amount = _formAmount.value,
            currencyCode = _formCurrencyCode.value,
            date = dateMillis,
            category = _formCategory.value,
            note = _formNote.value.takeIf { it.isNotBlank() },
            receiptUri = _formReceiptUri.value
        )
        viewModelScope.launch { repo.add(e) }
    }

    // Update expense using form state
    fun updateExpense(id: Long) {
        viewModelScope.launch {
            repo.getById(id).collect { expense ->
                expense.let {
                    val updated = it.copy(
                        amount = _formAmount.value,
                        currencyCode = _formCurrencyCode.value,
                        category = _formCategory.value,
                        note = _formNote.value.takeIf { text -> text.isNotBlank() },
                        receiptUri = _formReceiptUri.value
                    )
                    repo.update(updated)
                }
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repo.delete(expense) }
    }

    // ——— Budgets ———————————————————————————
    private val _budgets: Flow<List<Budget>> = repo.observeBudgets()
    val budgets: StateFlow<List<Budget>> = _budgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun upsertBudget(periodKey: String, amount: Double) {
        viewModelScope.launch {
            repo.upsertBudget(Budget(periodKey = periodKey, amount = amount))
        }
    }
}