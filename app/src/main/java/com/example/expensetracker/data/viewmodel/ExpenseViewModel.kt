package com.example.expensetracker.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow // NEW: Import MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow // NEW: Import SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow // NEW: Import asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepo: ExpenseRepository,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    // Expose raw expenses stream for lists
    val expenses: Flow<List<Expense>> = expenseRepo.observeAllExpenses()
    fun getExpenseById(id: Long): Flow<Expense?> = expenseRepo.observeExpenseById(id)

    // Expose UI state
    sealed class ExpenseUiState {
        data object Loading : ExpenseUiState() // UPDATED: Changed to data object
        data class Success(
            val expenses: List<Expense>,
            val settings: AppSettings
        ) : ExpenseUiState()
        data class Error(val message: String) : ExpenseUiState()
    }

    private val _uiState = MutableStateFlow<ExpenseUiState>(ExpenseUiState.Loading)
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>() // NEW: For one-time messages
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow() // NEW: Expose as SharedFlow

    // Form state
    data class ExpenseFormState(
        val id: Long = 0,
        val amount: Double = 0.0,
        val currencyCode: String = "USD",
        val category: String = "",
        val note: String = "",
        val type: Expense.ExpenseType = Expense.ExpenseType.EXPENSE,
        val date: Date = Date()
    )

    private val _formState = MutableStateFlow(ExpenseFormState())
    val formState: StateFlow<ExpenseFormState> = _formState.asStateFlow()

    @Suppress("Unused") // ADDED: Suppress warning as it's used internally by submitExpense()
    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    init {
        loadData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadData() {
        viewModelScope.launch {
            settingsRepo.appSettings
                .flatMapLatest { settings ->
                    expenseRepo.observeAllExpenses()
                        .map { expenses -> ExpenseUiState.Success(expenses, settings) }
                }
                .catch { e ->
                    // NEW: Replace with proper error logging in production
                    // Log.e("ExpenseViewModel", "Error loading expenses", e)
                    _uiState.value = ExpenseUiState.Error(
                        "Error loading expenses: ${e.message ?: "Unknown error"}"
                    )
                }
                .collect { state ->
                    _uiState.value = state

                    // initialize form currency on first load
                    if (_formState.value.currencyCode == "USD") {
                        _formState.value = _formState.value.copy(
                            currencyCode = state.settings.defaultCurrency
                        )
                    }
                }
        }
    }

    fun initForm() {
        _isEditing.value = false
        val defaultCurrency =
            (uiState.value as? ExpenseUiState.Success)?.settings?.defaultCurrency ?: "USD"
        _formState.value = ExpenseFormState(currencyCode = defaultCurrency)
    }

    fun initForm(expense: Expense) {
        _isEditing.value = true
        _formState.value = ExpenseFormState(
            id = expense.id,
            amount = expense.amount,
            currencyCode = expense.currencyCode,
            category = expense.category,
            note = expense.note.orEmpty(),
            type = expense.type,
            // convert LocalDate → Date for the form
            date = Date.from(expense.date.atStartOfDay(ZoneId.systemDefault()).toInstant())
        )
    }

    fun updateFormAmount(amount: Double) =
        _formState.update { it.copy(amount = amount) }

    fun updateFormCurrencyCode(currencyCode: String) =
        _formState.update { it.copy(currencyCode = currencyCode) }

    fun updateFormDate(date: Date) =
        _formState.update { it.copy(date = date) }

    fun updateFormCategory(category: String) =
        _formState.update { it.copy(category = category) }

    fun updateFormNote(note: String) =
        _formState.update { it.copy(note = note) }

    fun updateFormType(type: Expense.ExpenseType) =
        _formState.update { it.copy(type = type) }

    fun submitExpense() {
        val form = _formState.value
        val localDate = form.date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val expense = Expense(
            id = if (_isEditing.value) form.id else 0L,
            title = form.category, // Assuming title is derived from category
            amount = form.amount,
            date = localDate,
            category = form.category,
            note = form.note.ifBlank { null },
            type = form.type,
            currencyCode = form.currencyCode
        )

        viewModelScope.launch {
            try {
                if (_isEditing.value) {
                    expenseRepo.updateExpense(expense)
                    _userMessage.emit("Expense updated successfully!") // NEW: Emit success message
                } else {
                    expenseRepo.insertExpense(expense)
                    _userMessage.emit("Expense added successfully!") // NEW: Emit success message
                }
            } catch (e: Exception) { // UPDATED: Catch and handle specific exceptions
                // NEW: Replace with proper error logging in production
                // Log.e("ExpenseViewModel", "Error submitting expense", e)
                _userMessage.emit("Failed to save expense: ${e.message ?: "Unknown error"}") // NEW: Emit error message
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                expenseRepo.deleteExpense(expense)
                _userMessage.emit("Expense deleted successfully!") // NEW: Emit success message
            } catch (e: Exception) { // UPDATED: Catch and handle specific exceptions
                // NEW: Replace with proper error logging in production
                // Log.e("ExpenseViewModel", "Error deleting expense", e)
                _userMessage.emit("Failed to delete expense: ${e.message ?: "Unknown error"}") // NEW: Emit error message
            }
        }
    }
}