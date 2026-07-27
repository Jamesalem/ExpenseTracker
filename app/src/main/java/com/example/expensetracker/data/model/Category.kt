// Category.kt
package com.example.expensetracker.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)]
)
@Serializable
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val iconRes: String? = null,
    val isCustom: Boolean = true,
    val type: CategoryType = CategoryType.BOTH
) {
    @Serializable
    enum class CategoryType { INCOME, EXPENSE, BOTH }
}
