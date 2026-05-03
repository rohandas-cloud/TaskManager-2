package com.rohandas.taskmanagerpro.ui.notification

data class NotificationModel(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "info",   // "info" | "warning" | "success" | "error"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val userId: String = ""
)
