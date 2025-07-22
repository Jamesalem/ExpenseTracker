// data/util/CurrencyFormatter.kt
package com.example.expensetracker.data.util

import com.example.expensetracker.data.model.AppSettings
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyFormatter {

    fun formatCurrency(amount: Double, settings: AppSettings): String {
        return try {
            val currencyInstance = Currency.getInstance(settings.defaultCurrency)
            val format = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
                currency = currencyInstance
                minimumFractionDigits = settings.decimalPlaces
                maximumFractionDigits = settings.decimalPlaces
                isGroupingUsed = settings.useGroupingSeparator
            }
            format.format(amount)
        } catch (e: Exception) {
            // Fallback in case of invalid currency code or other formatting issues
            String.format(Locale.getDefault(), "%.${settings.decimalPlaces}f", amount)
        }
    }

    // NEW: Overload for direct formatting with specific parameters
    fun format(
        amount: Double,
        currencyCode: String,
        decimalPlaces: Int = 2, // Default to 2 if not provided
        useGrouping: Boolean = true // Default to true if not provided
    ): String {
        return try {
            val currencyInstance = Currency.getInstance(currencyCode)
            val format = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
                currency = currencyInstance
                minimumFractionDigits = decimalPlaces
                maximumFractionDigits = decimalPlaces
                isGroupingUsed = useGrouping
            }
            format.format(amount)
        } catch (e: Exception) {
            String.format(Locale.getDefault(), "%.${decimalPlaces}f", amount)
        }
    }
}