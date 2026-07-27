package com.example.expensetracker.data.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.R
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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
    private val settingsRepo: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Expose raw expenses stream for lists
    @OptIn(ExperimentalCoroutinesApi::class)
    val expenses: Flow<List<Expense>> = searchQuery
        .flatMapLatest { query ->
            expenseRepo.observeAllExpenses().map { list ->
                if (query.isBlank()) list
                else list.filter { 
                    it.category.contains(query, ignoreCase = true) || 
                    (it.note?.contains(query, ignoreCase = true) ?: false)
                }
            }
        }

    // Targeted flows for the Dashboard
    val recentExpenses: Flow<List<Expense>> = expenseRepo.observeRecentExpenses(4)
    val totalIncome: Flow<Double> = expenseRepo.observeTotalIncome()
    val totalExpense: Flow<Double> = expenseRepo.observeTotalExpense()

    fun getExpenseById(id: Long): Flow<Expense?> = expenseRepo.observeExpenseById(id)

    // Expose UI state
    sealed class ExpenseUiState {
        data object Loading : ExpenseUiState()
        data class Success(
            val expenses: List<Expense>,
            val settings: AppSettings
        ) : ExpenseUiState()
        data class Error(val message: String) : ExpenseUiState()
    }

    private val _uiState = MutableStateFlow<ExpenseUiState>(ExpenseUiState.Loading)
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

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

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    init {
        loadData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadData() {
        viewModelScope.launch {
            combine(settingsRepo.appSettings, searchQuery) { settings, query ->
                settings to query
            }.flatMapLatest { (settings, query) ->
                expenseRepo.observeAllExpenses().map { list ->
                    val filtered = if (query.isBlank()) list
                    else list.filter { 
                        it.category.contains(query, ignoreCase = true) || 
                        (it.note?.contains(query, ignoreCase = true) ?: false)
                    }
                    ExpenseUiState.Success(filtered, settings)
                }
            }
            .catch { e ->
                _uiState.value = ExpenseUiState.Error("Error loading expenses: ${e.message}")
            }
            .collect { state ->
                _uiState.value = state
                if (_formState.value.currencyCode == "USD") {
                    _formState.value = _formState.value.copy(
                        currencyCode = (state as? ExpenseUiState.Success)?.settings?.defaultCurrency ?: "USD"
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
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
            title = form.category,
            amount = form.amount,
            date = localDate,
            category = form.category,
            note = form.note.ifBlank { null },
            type = form.type,
            currencyCode = form.currencyCode
        )

        viewModelScope.launch {
            try {
                if (expense.type == Expense.ExpenseType.EXPENSE && !_isEditing.value) {
                    val hasIncome = expenseRepo.hasIncome()
                    if (!hasIncome) {
                        _userMessage.emit(context.getString(R.string.add_income_first))
                        return@launch
                    }
                }

                if (_isEditing.value) {
                    expenseRepo.updateExpense(expense)
                    val messageRes = if (expense.type == Expense.ExpenseType.INCOME) R.string.income_updated_successfully else R.string.expense_updated_successfully
                    _userMessage.emit(context.getString(messageRes))
                } else {
                    expenseRepo.insertExpense(expense)
                    val messageRes = if (expense.type == Expense.ExpenseType.INCOME) R.string.income_added_successfully else R.string.expense_added_successfully
                    _userMessage.emit(context.getString(messageRes))
                }
            } catch (e: Exception) {
                _userMessage.emit(context.getString(R.string.failed_to_save_expense, e.message ?: "Unknown error"))
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                expenseRepo.deleteExpense(expense)
                val messageRes = if (expense.type == Expense.ExpenseType.INCOME) R.string.income_deleted_successfully else R.string.expense_deleted_successfully
                _userMessage.emit(context.getString(messageRes))
            } catch (e: Exception) {
                _userMessage.emit(context.getString(R.string.failed_to_delete_expense, e.message ?: "Unknown error"))
            }
        }
    }
}
