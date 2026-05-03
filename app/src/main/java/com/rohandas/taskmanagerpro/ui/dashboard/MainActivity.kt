package com.rohandas.taskmanagerpro.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.rohandas.taskmanagerpro.databinding.ActivityDashboardBinding
import com.rohandas.taskmanagerpro.ui.adapter.TaskAdapter
import com.rohandas.taskmanagerpro.ui.profile.ProfileActivity
import com.rohandas.taskmanagerpro.ui.task.TodayTasksActivity
import com.rohandas.taskmanagerpro.utils.NavigationHelper
import com.rohandas.taskmanagerpro.viewmodel.TaskViewModel
import com.rohandas.taskmanagerpro.data.repository.AuthRepository
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: TaskViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeData()
        checkDatabaseAndFcm()
        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }

    private fun checkDatabaseAndFcm() {
        val authRepository = AuthRepository()
        
        lifecycleScope.launch {
            val result = authRepository.checkRealtimeDatabaseConnection()
            if (result.isSuccess) {
                Log.d("MainActivity", "Realtime Database is working properly.")
            } else {
                Log.e("MainActivity", "Realtime Database connection error: ${result.exceptionOrNull()?.message}")
                Toast.makeText(this@MainActivity, "RTDB Connection Failed", Toast.LENGTH_SHORT).show()
            }

            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    lifecycleScope.launch {
                        authRepository.updateFcmToken(token)
                    }
                }
            }
        }
    }

    private fun setupUI() {
        NavigationHelper.setupBottomMenu(this)

        binding.btnViewTasks.setOnClickListener {
            startActivity(Intent(this, TodayTasksActivity::class.java))
        }

        binding.profileSection.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            FirebaseCrashlytics.getInstance().setUserId(it.uid)
            binding.tvUserName.text = "Hello, ${it.displayName ?: "User"}!"
        }

        taskAdapter = TaskAdapter(
            tasks = emptyList(),
            onStatusChange = { updatedTask ->
                viewModel.updateTask(updatedTask)
            },
            onDelete = { task ->
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete Task")
                    .setMessage("Are you sure you want to delete '${task.title}'?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteTask(task.id)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        binding.rvDashboardTasks.layoutManager = LinearLayoutManager(this)
        binding.rvDashboardTasks.adapter = taskAdapter
    }

    private fun observeData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tasks.collect { tasks ->
                    binding.tvTaskCount.text = "${tasks.size} Total Entries"
                    
                    val todoCount = tasks.count { it.status == "Ongoing" }
                    val completedCount = tasks.count { it.status == "Done" }
                    val canceledCount = tasks.count { it.status == "Canceled" }
                    
                    TransitionManager.beginDelayedTransition(binding.root as ViewGroup)

                    binding.tvTodoCount.text = "$todoCount Tasks"
                    binding.tvCompletedCount.text = "$completedCount Tasks"
                    binding.tvOngoingCount.text = "$todoCount Tasks" 
                    binding.tvCanceledCount.text = "$canceledCount Tasks"
                    
                    taskAdapter.updateData(tasks.take(5))
                }
            }
        }
    }
}
