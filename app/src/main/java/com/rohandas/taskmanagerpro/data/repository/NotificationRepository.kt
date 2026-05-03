package com.rohandas.taskmanagerpro.data.repository

import com.rohandas.taskmanagerpro.ui.notification.NotificationModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId: String? get() = auth.currentUser?.uid

    suspend fun addNotification(title: String, message: String, type: String = "success"): Result<Unit> = try {
        val uid = userId ?: throw Exception("No authenticated user")
        val notificationRef = firestore.collection("notifications").document()
        val notification = NotificationModel(
            id = notificationRef.id,
            title = title,
            message = message,
            type = type,
            timestamp = System.currentTimeMillis(),
            userId = uid
        )
        notificationRef.set(notification).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
