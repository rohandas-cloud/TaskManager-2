package com.rohandas.taskmanagerpro.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String? = null,
    val photoUrl: String? = null,
    val fcmToken: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class Task(
    var id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val time: String = "",
    val category: String = "",
    val priority: String = "Medium", // High, Medium, Low
    val status: String = "Ongoing", // Ongoing, Done, Canceled
    val userId: String = "",
    val deadline: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class Project(
    var id: String = "",
    val name: String = "",
    val description: String = "",
    val color: String = "#4A90E2",
    val userId: String = "",
    val taskCount: Int = 0
)
