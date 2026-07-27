package com.example.expensetracker.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "subscriptions",
    indices = [Index(value = ["nextDueDateString"])]
)
@Serializable
data class Subscription(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val title: String,
    val amount: Double,
    val category: String = "Subscriptions",
    val billingCycle: String = "MONTHLY",
    val nextDueDateString: String,
    val note: String? = null,
    val iconName: String = "Subscriptions",
    val isActive: Boolean = true
)
