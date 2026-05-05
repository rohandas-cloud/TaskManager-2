package com.rohandas.taskmanagerpro.ui.task

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.rohandas.taskmanagerpro.databinding.ActivityTodayTasksBinding
import com.rohandas.taskmanagerpro.ui.adapter.TaskAdapter
import com.rohandas.taskmanagerpro.utils.NavigationHelper
import com.rohandas.taskmanagerpro.viewmodel.TaskViewModel
import com.rohandas.taskmanagerpro.data.model.Task
import kotlinx.coroutines.launch

class TodayTasksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTodayTasksBinding
    private val viewModel: TaskViewModel by viewModels()
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTodayTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
        observeTasks()
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            tasks = emptyList(),
            onStatusChange = { updatedTask ->
                viewModel.updateTask(updatedTask)
            },
            onDelete = { task ->
                showDeleteConfirmation(task)
            }
        )

        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        binding.rvTasks.adapter = adapter
    }

    private fun showDeleteConfirmation(task: Task) {
        com.google.android.material.snackbar.Snackbar.make(binding.root, "Delete requested for: ${task.title}", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete '${task.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                if (task.id.isEmpty()) {
                    com.google.android.material.snackbar.Snackbar.make(binding.root, "Error: Task ID is missing", com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show()
                } else {
                    viewModel.deleteTask(task.id)
                    com.google.android.material.snackbar.Snackbar.make(binding.root, "Task delete requested", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.fabAddTask.setOnClickListener {
            NavigationHelper.showAddOptions(this)
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }

        NavigationHelper.setupBottomMenu(this)
    }

    private fun observeTasks() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tasks.collect { tasks ->
                    FirebaseCrashlytics.getInstance().log("Observed ${tasks.size} tasks in TodayTasksActivity")
                    adapter.updateData(tasks)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isRefreshing.collect { isRefreshing ->
                    binding.swipeRefreshLayout.isRefreshing = isRefreshing
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.operationStatus.collect { result ->
                    result?.let {
                        if (it.isSuccess) {
                            com.google.android.material.snackbar.Snackbar.make(binding.root, "Operation Successful", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
                        } else {
                            val error = it.exceptionOrNull()?.message ?: "Unknown error"
                            com.google.android.material.snackbar.Snackbar.make(binding.root, "Error: $error", com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show()
                        }
                        viewModel.resetOperationStatus()
                    }
                }
            }
        }
    }
}
