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
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete '${task.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteTask(task.id)
                Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.fabAddTask.setOnClickListener {
            NavigationHelper.showAddOptions(this)
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
    }
}
