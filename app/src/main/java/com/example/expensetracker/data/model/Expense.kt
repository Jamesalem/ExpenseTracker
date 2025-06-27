package com.example.expensetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * amount is always stored in the smallest unit (e.g. decimals allowed),
 * currencyCode is the ISO 4217 code (USD, EUR, NGN, etc.)
 */
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val currencyCode: String = "USD",
    val date: Long,         // epoch millis
    val category: String,
    val note: String? = null,
    val receiptUri: String? = null
)
