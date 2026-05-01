package com.example.taskmanagerpro.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanagerpro.data.model.Task
import com.example.taskmanagerpro.data.remote.TaskRepository
import com.example.taskmanagerpro.utils.TaskScheduler
import kotlinx.coroutines.launch

class TaskViewModel : ViewModel() {
    private val repository = TaskRepository()

    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> = _tasks

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init { loadTasks() }

    fun loadTasks() {
        viewModelScope.launch {
            repository.getTasksStream().collect { taskList ->
                _tasks.postValue(taskList)
            }
        }
    }

    fun addTask(task: Task, context: Context) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            val result = repository.addTask(task)
            result.onSuccess { taskId ->
                TaskScheduler.scheduleTask(context, task.copy(id = taskId))
                _isLoading.postValue(false)
            }.onFailure { exception ->
                _errorMessage.postValue(exception.message)
                _isLoading.postValue(false)
            }
        }
    }

    fun toggleComplete(task: Task) {
        viewModelScope.launch {
            repository.updateTaskStatus(task.id, !task.isCompleted)
        }
    }
}