package com.example.taskmanagerpro.ui.task

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.taskmanagerpro.R
import com.example.taskmanagerpro.data.model.Task
import com.example.taskmanagerpro.utils.NavigationHelper
import com.example.taskmanagerpro.viewmodel.TaskViewModel
import java.util.Calendar

class AddProjectActivity : AppCompatActivity() {
    private val viewModel: TaskViewModel by viewModels()

    private lateinit var etTitle: EditText
    private lateinit var etDesc: EditText
    private lateinit var btnDate: Button
    private var selectedDeadlineMillis: Long = 0L
    private var saveInitiated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_project)

        etTitle = findViewById(R.id.etProjectTitle)
        etDesc = findViewById(R.id.etProjectDesc)
        btnDate = findViewById(R.id.btnSelectDate)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        btnDate.setOnClickListener { showDateTimePicker() }
        findViewById<Button>(R.id.btnCreateProject).setOnClickListener { createTask() }

        NavigationHelper.setupBottomMenu(this)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            if (!isLoading && saveInitiated) {
                saveInitiated = false
                Toast.makeText(this, "Task Created!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                saveInitiated = false
                Toast.makeText(this, "Error: $it", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            TimePickerDialog(this, { _, hour, minute ->
                calendar.set(year, month, day, hour, minute)
                selectedDeadlineMillis = calendar.timeInMillis
                btnDate.text = "$day/${month + 1}/$year $hour:${minute.toString().padStart(2, '0')}"
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun createTask() {
        val title = etTitle.text.toString().trim()
        val desc = etDesc.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedDeadlineMillis == 0L) {
            Toast.makeText(this, "Please select a deadline", Toast.LENGTH_SHORT).show()
            return
        }

        val task = Task(title = title, description = desc, deadline = selectedDeadlineMillis)
        saveInitiated = true
        viewModel.addTask(task, this)
    }
}