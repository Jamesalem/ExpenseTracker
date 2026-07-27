// data/model/Expense.kt
package com.example.expensetracker.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["date"]),
        Index(value = ["category"])
    ]
)
@Serializable
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val title: String,
    val amount: Double,

    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,

    val category: String,
    val note: String? = null,
    val type: ExpenseType = ExpenseType.EXPENSE,
    val currencyCode: String
) {
    @Serializable
    enum class ExpenseType { INCOME, EXPENSE }
}
