package com.rohandas.taskmanagerpro.ui.task

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.rohandas.taskmanagerpro.R
import com.rohandas.taskmanagerpro.data.model.Project
import com.rohandas.taskmanagerpro.viewmodel.TaskViewModel
import com.rohandas.taskmanagerpro.application.MyFirebaseMessagingService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AddProjectActivity : AppCompatActivity() {
    private val viewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_project)

        val etTitle = findViewById<EditText>(R.id.etProjectTitle)
        val etDesc = findViewById<EditText>(R.id.etProjectDesc)
        val btnCreate = findViewById<Button>(R.id.btnCreateProject)

        viewModel.resetOperationStatus()

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        btnCreate.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val desc = etDesc.text.toString().trim()

            if (title.isNotEmpty()) {
                val newProject = Project(
                    name = title,
                    description = desc
                )
                viewModel.addProject(newProject)
            } else {
                Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe Saving State
        lifecycleScope.launch {
            viewModel.isSaving.collect { isSaving ->
                btnCreate.isEnabled = !isSaving
                btnCreate.text = if (isSaving) "Creating..." else "Create Project"
            }
        }

        // Observe Operation Status
        lifecycleScope.launch {
            viewModel.operationStatus.collect { result ->
                result?.let {
                    if (it.isSuccess) {
                        val title = etTitle.text.toString().trim()
                        MyFirebaseMessagingService.showNotification(
                            this@AddProjectActivity,
                            "Project Added",
                            "Your project '$title' has been successfully added to Firestore."
                        )
                        Toast.makeText(this@AddProjectActivity, "Project Added Successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val error = it.exceptionOrNull()?.message ?: "Unknown error"
                        Toast.makeText(this@AddProjectActivity, "Failed to add project: $error", Toast.LENGTH_LONG).show()
                        viewModel.resetOperationStatus()
                    }
                }
            }
        }
    }
}
