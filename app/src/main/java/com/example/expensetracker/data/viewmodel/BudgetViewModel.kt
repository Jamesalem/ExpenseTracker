// data/viewmodel/BudgetViewModel.kt
package com.example.expensetracker.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.model.Budget
import com.example.expensetracker.data.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {

    sealed class BudgetUiState {
        object Loading : BudgetUiState()
        data class Success(val budgets: List<Budget>) : BudgetUiState()
        data class Error(val message: String) : BudgetUiState()
    }

    private val _uiState = MutableStateFlow<BudgetUiState>(BudgetUiState.Loading)
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage

    init {
        loadBudgets()
    }

    private fun loadBudgets() {
        viewModelScope.launch {
            repository.observeAllBudgets()
                .catch { e ->
                    _uiState.value = BudgetUiState.Error(
                        "Error loading budgets: ${e.message ?: "Unknown error"}"
                    )
                }
                .collect { budgets ->
                    _uiState.value = BudgetUiState.Success(budgets)
                }
        }
    }

    /**
     * Return the budget for the given period key, or null if none.
     */
    fun getBudgetForPeriod(periodKey: String): Budget? =
        (uiState.value as? BudgetUiState.Success)
            ?.budgets
            ?.firstOrNull { it.periodKey == periodKey }

    /**
     * Create or update a budget for the given period.
     */
    fun saveBudget(periodKey: String, amount: Double) {
        viewModelScope.launch {
            try {
                repository.upsertBudget(Budget(periodKey = periodKey, amount = amount))
                _userMessage.emit("Budget saved successfully")
            } catch (e: Exception) {
                _userMessage.emit("Failed to save budget: ${e.message ?: "Unknown error"}")
            }
        }
    }

    /**
     * Delete a budget by its ID, then reload the list.
     */
    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteBudget(id)
                _userMessage.emit("Budget deleted successfully")
                loadBudgets()
            } catch (e: Exception) {
                _userMessage.emit("Failed to delete budget: ${e.message ?: "Unknown error"}")
            }
        }
    }
}
