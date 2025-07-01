package com.example.expensetracker.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository
) : ViewModel() {

    // Expose the default currency, or fallback to "USD"
    val defaultCurrency: StateFlow<String> = repo.defaultCurrency
        .map { it ?: "USD" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "USD")

    // Save a new default
    fun setDefaultCurrency(code: String) = viewModelScope.launch {
        repo.setDefaultCurrency(code)
    }
}
