package com.rohandas.taskmanagerpro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rohandas.taskmanagerpro.data.model.Project
import com.rohandas.taskmanagerpro.data.model.Task
import com.rohandas.taskmanagerpro.data.repository.TaskRepository
import com.rohandas.taskmanagerpro.utils.TaskScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaskRepository.getInstance()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _operationStatus = MutableStateFlow<Result<Unit>?>(null)
    val operationStatus: StateFlow<Result<Unit>?> = _operationStatus

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getTasks().collect { _tasks.value = it }
        }
        viewModelScope.launch {
            repository.getProjects().collect { _projects.value = it }
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.addTask(task)
            if (result.isSuccess) {
                val savedTask = result.getOrNull()
                if (savedTask != null) {
                    TaskScheduler.scheduleTask(getApplication(), savedTask)
                }
                _operationStatus.value = Result.success(Unit)
            } else {
                _operationStatus.value = Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
            }
            _isSaving.value = false
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refresh()
            // Give it a moment to sync and then stop the animation
            kotlinx.coroutines.delay(1000)
            _isRefreshing.value = false
        }
    }

    fun resetOperationStatus() {
        _operationStatus.value = null
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
            
            // Manage scheduling
            if (task.status == "Done" || task.status == "Canceled") {
                TaskScheduler.cancelTask(getApplication(), task.id)
            } else {
                // Reschedule in case time/date changed
                TaskScheduler.cancelTask(getApplication(), task.id)
                TaskScheduler.scheduleTask(getApplication(), task)
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            val result = repository.deleteTask(taskId)
            if (result.isSuccess) {
                TaskScheduler.cancelTask(getApplication(), taskId)
            }
            _operationStatus.value = result
        }
    }

    fun addProject(project: Project) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.addProject(project)
            _operationStatus.value = result
            _isSaving.value = false
        }
    }
}
