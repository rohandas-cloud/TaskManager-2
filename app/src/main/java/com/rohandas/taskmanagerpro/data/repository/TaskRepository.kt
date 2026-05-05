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

class TaskRepository private constructor() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val notificationRepository = NotificationRepository()
    private val userId: String? get() = auth.currentUser?.uid

    private val repositoryScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

    private val _tasksFlow = kotlinx.coroutines.flow.MutableStateFlow<List<Task>>(emptyList())
    val tasksFlow: kotlinx.coroutines.flow.StateFlow<List<Task>> = _tasksFlow

    private val _projectsFlow = kotlinx.coroutines.flow.MutableStateFlow<List<Project>>(emptyList())
    val projectsFlow: kotlinx.coroutines.flow.StateFlow<List<Project>> = _projectsFlow

    private var tasksListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var projectsListener: com.google.firebase.firestore.ListenerRegistration? = null

    companion object {
        @Volatile
        private var instance: TaskRepository? = null

        fun getInstance(): TaskRepository {
            return instance ?: synchronized(this) {
                instance ?: TaskRepository().also { instance = it }
            }
        }
    }

    init {
        // Automatically start listening when the repository is created
        startListening()
        
        // Listen for Auth changes to restart listeners with new UID
        auth.addAuthStateListener {
            startListening()
        }
    }

    fun refresh() {
        startListening()
    }

    private fun startListening() {
        val uid = userId
        if (uid == null) {
            _tasksFlow.value = emptyList()
            _projectsFlow.value = emptyList()
            tasksListener?.remove()
            projectsListener?.remove()
            return
        }

        // Remove old listeners if they exist
        tasksListener?.remove()
        projectsListener?.remove()

        // Persistent Tasks Listener
        tasksListener = firestore.collection("tasks")
            .whereEqualTo("userId", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore", "Error fetching tasks: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val tasks = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Task::class.java)?.apply { 
                            id = doc.id // Manually set the ID from document metadata
                        }
                    }
                    Log.d("Firestore", "Fetched ${tasks.size} tasks for UID: $uid")
                    _tasksFlow.value = tasks
                }
            }

        // Persistent Projects Listener
        projectsListener = firestore.collection("projects")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore", "Error fetching projects: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val projects = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Project::class.java)?.apply {
                            id = doc.id
                        }
                    }
                    _projectsFlow.value = projects
                }
            }
    }

    // --- Tasks ---

    // Now returns the shared flow instead of creating a new one
    fun getTasks(): Flow<List<Task>> = tasksFlow

    suspend fun addTask(task: Task): Result<Task> = try {
        val uid = userId ?: throw Exception("No authenticated user")
        val taskRef = firestore.collection("tasks").document()
        val finalTask = task.copy(id = taskRef.id, userId = uid)
        taskRef.set(finalTask).await()
        
        notificationRepository.addNotification(
            title = "Task Added",
            message = "Task '${task.title}' was successfully submitted."
        )
        Result.success(finalTask)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateTask(task: Task): Result<Unit> = try {
        firestore.collection("tasks").document(task.id).set(task).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteTask(taskId: String): Result<Unit> = try {
        Log.d("Firestore", "Attempting to delete task: $taskId")
        firestore.collection("tasks").document(taskId).delete().await()
        Log.d("Firestore", "Successfully deleted task: $taskId")
        
        // --- GLOBAL SYNC FIX ---
        // Immediately update the shared flow so ALL activities/viewmodels see the change
        val currentTasks = _tasksFlow.value
        _tasksFlow.value = currentTasks.filter { it.id != taskId }
        
        notificationRepository.addNotification(
            title = "Task Deleted",
            message = "A task was successfully removed.",
            type = "warning"
        )
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("Firestore", "Error deleting task $taskId: ${e.message}")
        Result.failure(e)
    }

    // --- Projects ---

    fun getProjects(): Flow<List<Project>> = projectsFlow

    suspend fun addProject(project: Project): Result<Unit> = try {
        val uid = userId ?: throw Exception("No authenticated user")
        val projectRef = firestore.collection("projects").document()
        val finalProject = project.copy(id = projectRef.id, userId = uid)
        projectRef.set(finalProject).await()
        
        notificationRepository.addNotification(
            title = "Project Added",
            message = "Project '${project.name}' was successfully submitted."
        )
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
