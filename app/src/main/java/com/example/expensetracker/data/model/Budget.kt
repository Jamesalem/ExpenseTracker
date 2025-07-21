// Budget.kt
package com.example.expensetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val periodKey: String,  // e.g. "2025-07" or "project-foo"
    val amount: Double
)
