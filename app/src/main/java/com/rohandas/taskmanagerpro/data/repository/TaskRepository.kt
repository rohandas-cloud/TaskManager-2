package com.rohandas.taskmanagerpro.data.repository

import android.util.Log
import com.rohandas.taskmanagerpro.data.model.Project
import com.rohandas.taskmanagerpro.data.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TaskRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val notificationRepository = NotificationRepository()
    private val userId: String? get() = auth.currentUser?.uid

    // --- Tasks ---

    fun getTasks(): Flow<List<Task>> = callbackFlow {
        val uid = userId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val subscription = firestore.collection("tasks")
            .whereEqualTo("userId", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore", "Error fetching tasks: ${error.message}")
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val tasks = snapshot.toObjects(Task::class.java)
                    trySend(tasks)
                }
            }

        awaitClose { subscription.remove() }
    }

    suspend fun addTask(task: Task): Result<Task> = try {
        val uid = userId ?: throw Exception("No authenticated user")
        val taskRef = firestore.collection("tasks").document()
        val finalTask = task.copy(id = taskRef.id, userId = uid)
        taskRef.set(finalTask).await()
        
        // Add App Notification
        notificationRepository.addNotification(
            title = "Task Added",
            message = "Task '${task.title}' was successfully submitted."
        )
        
        Log.d("Firestore", "Successfully added task with ID: ${taskRef.id}")
        Result.success(finalTask)
    } catch (e: Exception) {
        Log.e("Firestore", "Error adding task: ${e.message}")
        Result.failure(e)
    }


    suspend fun updateTask(task: Task): Result<Unit> = try {
        val uid = userId ?: throw Exception("No authenticated user")
        firestore.collection("tasks").document(task.id).set(task).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteTask(taskId: String): Result<Unit> = try {
        val uid = userId ?: throw Exception("No authenticated user")
        firestore.collection("tasks").document(taskId).delete().await()
        
        // Add App Notification
        notificationRepository.addNotification(
            title = "Task Deleted",
            message = "A task was successfully removed.",
            type = "warning"
        )
        
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // --- Projects ---

    fun getProjects(): Flow<List<Project>> = callbackFlow {
        val uid = userId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val subscription = firestore.collection("projects")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore", "Error fetching projects: ${error.message}")
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val projects = snapshot.toObjects(Project::class.java)
                    trySend(projects)
                }
            }

        awaitClose { subscription.remove() }
    }

    suspend fun addProject(project: Project): Result<Unit> = try {
        val uid = userId ?: throw Exception("No authenticated user")
        val projectRef = firestore.collection("projects").document()
        val finalProject = project.copy(id = projectRef.id, userId = uid)
        projectRef.set(finalProject).await()
        
        // Add App Notification
        notificationRepository.addNotification(
            title = "Project Added",
            message = "Project '${project.name}' was successfully submitted."
        )
        
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
