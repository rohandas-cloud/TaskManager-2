package com.example.taskmanagerpro.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val deadline: Long = 0L,
    val isCompleted: Boolean = false,
    @ServerTimestamp
    val createdAt: Date? = null
)
