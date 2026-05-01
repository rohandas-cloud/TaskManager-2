package com.example.taskmanagerpro.data.remote

import com.example.taskmanagerpro.data.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * TaskRepository - Handles all Firestore task operations.
 *
 * FIXES:
 *  1. Tasks are now stored under users/{uid}/tasks so each user only
 *     sees their own data (previously all tasks were in a single global
 *     "tasks" collection visible to everyone).
 *  2. FirebaseCrashlytics.recordException() added to every catch block.
 */
class TaskRepository {

    private val db = FirebaseFirestore.getInstance()
    private val crashlytics = FirebaseCrashlytics.getInstance()

    /**
     * Returns the Firestore collection scoped to the current user.
     * Throws IllegalStateException if called while no user is signed in.
     */
    private fun userTasksCollection() =
        FirebaseAuth.getInstance().currentUser?.uid
            ?.let { uid -> db.collection("users").document(uid).collection("tasks") }
            ?: error("No authenticated user — cannot access tasks collection")

    suspend fun addTask(task: Task): Result<String> {
        return try {
            val docRef = userTasksCollection().add(task).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            crashlytics.recordException(e)           // <-- Crashlytics fix
            Result.failure(e)
        }
    }

    fun getTasksStream(): Flow<List<Task>> = callbackFlow {
        val registration = try {
            userTasksCollection()
                .orderBy("deadline", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        crashlytics.recordException(error) // <-- Crashlytics fix
                        close(error)
                        return@addSnapshotListener
                    }
                    val tasks = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Task::class.java)?.copy(id = doc.id)
                    } ?: emptyList()
                    trySend(tasks)
                }
        } catch (e: Exception) {
            crashlytics.recordException(e)
            close(e)
            return@callbackFlow
        }
        awaitClose { registration.remove() }
    }

    suspend fun updateTaskStatus(taskId: String, isCompleted: Boolean): Result<Unit> {
        return try {
            userTasksCollection().document(taskId)
                .update("isCompleted", isCompleted)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)           // <-- Crashlytics fix
            Result.failure(e)
        }
    }

    suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            userTasksCollection().document(taskId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }
}
