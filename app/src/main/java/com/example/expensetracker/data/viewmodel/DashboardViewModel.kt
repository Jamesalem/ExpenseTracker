package com.example.expensetracker.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.data.model.Budget
import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.repository.BudgetRepository
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val expenseRepo: ExpenseRepository,
    private val budgetRepo: BudgetRepository,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    sealed class DashboardUiState {
        data object Loading : DashboardUiState()
        data class Success(
            val expenses: List<Expense>,
            val budgets: List<Budget>,
            val settings: AppSettings,
            val selectedMonth: YearMonth,
            val totalSpent: Double,
            val spentByCategory: List<Pair<String, Double>>
        ) : DashboardUiState()
        data class Error(val message: String) : DashboardUiState()
    }

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    init {
        loadDashboardData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadDashboardData() {
        viewModelScope.launch {
            _selectedMonth.flatMapLatest { month ->
                val start = month.atDay(1)
                val end = month.atEndOfMonth()
                
                combine(
                    expenseRepo.observeExpensesBetweenDates(start, end),
                    budgetRepo.observeAllBudgets(),
                    settingsRepo.appSettings
                ) { expenses, budgets, settings ->
                    val totalSpent = expenses
                        .filter { it.type == Expense.ExpenseType.EXPENSE }
                        .sumOf { it.amount }

                    val spentByCategory = expenses
                        .filter { it.type == Expense.ExpenseType.EXPENSE }
                        .groupBy { it.category }
                        .mapValues { it.value.sumOf(Expense::amount) }
                        .toList()
                        .sortedByDescending { it.second }

                    DashboardUiState.Success(
                        expenses = expenses,
                        budgets = budgets,
                        settings = settings,
                        selectedMonth = month,
                        totalSpent = totalSpent,
                        spentByCategory = spentByCategory
                    )
                }
            }
            .catch { e ->
                _uiState.value = DashboardUiState.Error("Error loading dashboard data: ${e.message}")
            }
            .collect { state ->
                _uiState.value = state
            }
        }
    }

    fun updateSelectedMonth(month: YearMonth) {
        _selectedMonth.value = month
    }

    fun saveBudget(periodKey: String, amount: Double) {
        viewModelScope.launch {
            try {
                budgetRepo.upsertBudget(Budget(periodKey = periodKey, amount = amount))
                _userMessage.emit("Budget saved successfully!")
            } catch (e: Exception) {
                _userMessage.emit("Failed to save budget: ${e.message}")
            }
        }
    }
}
