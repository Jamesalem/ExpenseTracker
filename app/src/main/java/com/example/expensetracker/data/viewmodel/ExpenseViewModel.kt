package com.example.expensetracker.data.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.R
import com.example.expensetracker.data.math.NaiveBayesCategorizer
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

    private val categorizer = NaiveBayesCategorizer()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Expose raw expenses stream for lists
    @OptIn(ExperimentalCoroutinesApi::class)
    val expenses: Flow<List<Expense>> = searchQuery
        .flatMapLatest { query ->
            expenseRepo.observeAllExpenses().map { list ->
                if (query.isBlank()) list
                else list.filter { 
                    it.title.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true) || 
                    it.account.contains(query, ignoreCase = true) ||
                    it.tags.contains(query, ignoreCase = true) ||
                    (it.note?.contains(query, ignoreCase = true) ?: false)
                }
            }
        }

    // Targeted flows for the Dashboard
    val recentExpenses: Flow<List<Expense>> = expenseRepo.observeRecentExpenses(6)
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
        val title: String = "",
        val amount: Double = 0.0,
        val currencyCode: String = "USD",
        val category: String = "",
        val note: String = "",
        val type: Expense.ExpenseType = Expense.ExpenseType.EXPENSE,
        val account: String = "Cash",
        val tags: String = "",
        val date: Date = Date()
    )

    private val _formState = MutableStateFlow(ExpenseFormState())
    val formState: StateFlow<ExpenseFormState> = _formState.asStateFlow()

    private val _categoryPredictions = MutableStateFlow<List<Pair<String, Double>>>(emptyList())
    val categoryPredictions: StateFlow<List<Pair<String, Double>>> = _categoryPredictions.asStateFlow()

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
                    // Train Bayesian Categorizer on historical transactions
                    categorizer.train(list)

                    val filtered = if (query.isBlank()) list
                    else list.filter { 
                        it.title.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true) || 
                        it.account.contains(query, ignoreCase = true) ||
                        it.tags.contains(query, ignoreCase = true) ||
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
        _categoryPredictions.value = emptyList()
    }

    fun initForm(expense: Expense) {
        _isEditing.value = true
        _formState.value = ExpenseFormState(
            id = expense.id,
            title = expense.title,
            amount = expense.amount,
            currencyCode = expense.currencyCode,
            category = expense.category,
            note = expense.note.orEmpty(),
            type = expense.type,
            account = expense.account,
            tags = expense.tags,
            date = Date.from(expense.date.atStartOfDay(ZoneId.systemDefault()).toInstant())
        )
        _categoryPredictions.value = emptyList()
    }

    fun updateFormTitle(title: String) {
        _formState.update { it.copy(title = title) }
        updatePredictions(title, _formState.value.note)
    }

    fun updateFormAmount(amount: Double) =
        _formState.update { it.copy(amount = amount) }

    fun updateFormCurrencyCode(currencyCode: String) =
        _formState.update { it.copy(currencyCode = currencyCode) }

    fun updateFormDate(date: Date) =
        _formState.update { it.copy(date = date) }

    fun updateFormCategory(category: String) =
        _formState.update { it.copy(category = category) }

    fun updateFormAccount(account: String) =
        _formState.update { it.copy(account = account) }

    fun updateFormTags(tags: String) =
        _formState.update { it.copy(tags = tags) }

    fun updateFormNote(note: String) {
        _formState.update { it.copy(note = note) }
        updatePredictions(_formState.value.title, note)
    }

    private fun updatePredictions(title: String, note: String) {
        val query = "$title $note".trim()
        if (query.length >= 2) {
            val preds = categorizer.predict(query).take(3)
            _categoryPredictions.value = preds
        } else {
            _categoryPredictions.value = emptyList()
        }
    }

    fun updateFormType(type: Expense.ExpenseType) =
        _formState.update { it.copy(type = type) }

    fun submitExpense() {
        val form = _formState.value
        val localDate = form.date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val finalTitle = form.title.ifBlank { form.category }

        val expense = Expense(
            id = if (_isEditing.value) form.id else 0L,
            title = finalTitle,
            amount = form.amount,
            date = localDate,
            category = form.category,
            note = form.note.ifBlank { null },
            type = form.type,
            currencyCode = form.currencyCode,
            account = form.account,
            tags = form.tags
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
                    categorizer.addDocument("${expense.title} ${expense.note.orEmpty()}", expense.category)
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
