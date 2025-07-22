// data/viewmodel/DashboardViewModel.kt
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val expenseRepo: ExpenseRepository,
    private val budgetRepo: BudgetRepository,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    // Centralized UI State for the Dashboard
    sealed class DashboardUiState {
        data object Loading : DashboardUiState()
        data class Success(
            val expenses: List<Expense>,
            val budgets: List<Budget>,
            val settings: AppSettings
        ) : DashboardUiState()
        data class Error(val message: String) : DashboardUiState()
    }

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>() // NEW: For one-time messages
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow() // NEW: Expose as SharedFlow

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            combine(
                expenseRepo.observeAllExpenses(),
                budgetRepo.observeAllBudgets(),
                settingsRepo.appSettings
            ) { expenses, budgets, settings ->
                DashboardUiState.Success(expenses, budgets, settings)
            }
                .catch { e ->
                    // NEW: Replace with proper error logging in production
                    // Log.e("DashboardViewModel", "Error loading dashboard data", e)
                    _uiState.value = DashboardUiState.Error(
                        "Error loading dashboard data: ${e.message ?: "Unknown error"}"
                    )
                }
                .collect { dashboardState ->
                    _uiState.value = dashboardState
                }
        }
    }

    // Delegate function for saving budget
    fun saveBudget(periodKey: String, amount: Double) {
        viewModelScope.launch {
            try {
                // UPDATED: Changed from saveBudget to upsertBudget
                budgetRepo.upsertBudget(Budget(periodKey = periodKey, amount = amount))
                _userMessage.emit("Budget saved successfully!") // NEW: Emit success message
            } catch (e: Exception) {
                // NEW: Replace with proper error logging in production
                // Log.e("DashboardViewModel", "Failed to save budget", e)
                _userMessage.emit("Failed to save budget: ${e.message ?: "Unknown error"}") // NEW: Emit error message
            }
        }
    }

    // You can add other delegated functions here if dashboard triggers them
}