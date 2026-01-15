package com.example.deadlinetrackapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val customer: String = "",
    val statusCode: Int,
    val priorityCode: Int,
    val paymentStatusCode: Int,
    val deadlineMillis: Long?
)

