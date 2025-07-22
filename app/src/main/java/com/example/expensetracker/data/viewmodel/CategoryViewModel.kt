// data/viewmodel/CategoryViewModel.kt
package com.example.expensetracker.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.model.Category
import com.example.expensetracker.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow // NEW: Import MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow // NEW: Import SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow // NEW: Import asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: CategoryRepository
) : ViewModel() {

    sealed class CategoryUiState {
        data object Loading : CategoryUiState() // UPDATED: Changed to data object
        data class Success(val categories: List<Category>) : CategoryUiState()
        data class Error(val message: String) : CategoryUiState()
    }

    private val _uiState = MutableStateFlow<CategoryUiState>(CategoryUiState.Loading)
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>() // NEW: For one-time messages
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow() // NEW: Expose as SharedFlow

    init { loadCategories() }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getAllCategories()
                .catch { e ->
                    // NEW: Replace with proper error logging in production
                    // Log.e("CategoryViewModel", "Error loading categories", e)
                    _uiState.value = CategoryUiState.Error(
                        "Error loading categories: ${e.message ?: "Unknown error"}"
                    )
                }
                .collect { cats ->
                    _uiState.value = CategoryUiState.Success(cats)
                }
        }
    }

    fun addCategory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            _uiState.value = CategoryUiState.Loading
            try {
                repository.insertCategory(Category(name = name.trim()))
                _userMessage.emit("Category added successfully") // NEW: Emit success message
                loadCategories()
            } catch (e: Exception) {
                // NEW: Replace with proper error logging in production
                // Log.e("CategoryViewModel", "Failed to add category", e)
                _uiState.value = CategoryUiState.Error(
                    "Failed to add category: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            _uiState.value = CategoryUiState.Loading
            try {
                repository.updateCategory(category)
                _userMessage.emit("Category updated successfully") // NEW: Emit success message
                loadCategories()
            } catch (e: Exception) {
                // NEW: Replace with proper error logging in production
                // Log.e("CategoryViewModel", "Failed to update category", e)
                _uiState.value = CategoryUiState.Error(
                    "Failed to update category: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            _uiState.value = CategoryUiState.Loading
            try {
                repository.deleteCategory(category)
                _userMessage.emit("Category deleted successfully") // NEW: Emit success message
                loadCategories()
            } catch (e: Exception) {
                // NEW: Replace with proper error logging in production
                // Log.e("CategoryViewModel", "Failed to delete category", e)
                _uiState.value = CategoryUiState.Error(
                    "Failed to delete category: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
}