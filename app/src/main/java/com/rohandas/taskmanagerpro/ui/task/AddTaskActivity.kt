package com.rohandas.taskmanagerpro.ui.task

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.rohandas.taskmanagerpro.R
import com.rohandas.taskmanagerpro.application.MyFirebaseMessagingService
import com.rohandas.taskmanagerpro.data.model.Task
import com.rohandas.taskmanagerpro.databinding.ActivityAddTaskBinding
import com.rohandas.taskmanagerpro.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.text.SimpleDateFormat

class AddTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTaskBinding
    private val viewModel: TaskViewModel by viewModels()
    private var selectedPriority = "Medium"
    private var selectedDate = ""
    private var selectedTime = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Correct ViewBinding initialization
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.resetOperationStatus()
        setupPickers()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnLow.setOnClickListener { updatePriority("Low") }
        binding.btnMedium.setOnClickListener { updatePriority("Medium") }
        binding.btnHigh.setOnClickListener { updatePriority("High") }

        binding.btnCreateTask.setOnClickListener {
            val title = binding.etTaskTitle.text.toString().trim()
            val category = binding.etCategory.text.toString().trim()
            val desc = binding.etTaskDesc.text.toString().trim()

            if (title.isNotEmpty()) {
                val deadline = calculateDeadline(selectedDate, selectedTime)
                val newTask = Task(
                    title = title,
                    description = desc,
                    category = category.ifEmpty { "General" },
                    priority = selectedPriority,
                    date = selectedDate.ifEmpty { "Today" },
                    time = selectedTime.ifEmpty { "Not set" },
                    deadline = deadline,
                    status = "Ongoing"
                )
                viewModel.addTask(newTask)
            } else {
                Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupPickers() {
        val indiaTimeZone = TimeZone.getTimeZone("Asia/Kolkata")
        val calendar = Calendar.getInstance(indiaTimeZone)
        val locale = Locale.Builder().setLanguage("en").setRegion("IN").build()

        binding.btnPickDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                selectedDate = String.format(locale, "%02d/%02d/%d", day, month + 1, year)
                binding.tvSelectedDate.text = selectedDate
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnPickTime.setOnClickListener {
            TimePickerDialog(this, { _, hour, minute ->
                selectedTime = String.format(locale, "%02d:%02d", hour, minute)
                binding.tvSelectedTime.text = selectedTime
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.isSaving.collect { isSaving ->
                binding.btnCreateTask.isEnabled = !isSaving
                binding.btnCreateTask.text = if (isSaving) "Creating..." else "Create Task"
            }
        }

        lifecycleScope.launch {
            viewModel.operationStatus.collect { result ->
                result?.let {
                    if (it.isSuccess) {
                        val title = binding.etTaskTitle.text.toString().trim()
                        MyFirebaseMessagingService.showNotification(
                            this@AddTaskActivity,
                            "Task Added",
                            "Your task '$title' has been successfully added to Firestore."
                        )
                        Toast.makeText(this@AddTaskActivity, "Task Added Successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val error = it.exceptionOrNull()?.message ?: "Unknown error"
                        Toast.makeText(this@AddTaskActivity, "Failed to add task: $error", Toast.LENGTH_LONG).show()
                        viewModel.resetOperationStatus()
                    }
                }
            }
        }
    }

    private fun calculateDeadline(dateStr: String, timeStr: String): Long {
        if (timeStr.isEmpty()) return 0L
        
        return try {
            val locale = Locale.Builder().setLanguage("en").setRegion("IN").build()
            val indiaTimeZone = TimeZone.getTimeZone("Asia/Kolkata")
            
            val finalDateStr = if (dateStr.isEmpty() || dateStr == "Today") {
                val cal = Calendar.getInstance(indiaTimeZone)
                String.format(locale, "%02d/%02d/%d", 
                    cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.YEAR))
            } else {
                dateStr
            }

            val dateTimeStr = "$finalDateStr $timeStr"
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm", locale)
            format.timeZone = indiaTimeZone
            format.parse(dateTimeStr)?.time ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    private fun updatePriority(priority: String) {
        selectedPriority = priority
        
        val activeBg = R.drawable.bg_card_blue
        val inactiveBg = R.drawable.bg_card_gray
        val white = ContextCompat.getColor(this, R.color.tp_white)
        val dark = ContextCompat.getColor(this, R.color.tp_text_primary)

        binding.btnLow.setBackgroundResource(if (priority == "Low") activeBg else inactiveBg)
        binding.btnLow.setTextColor(if (priority == "Low") white else dark)
        
        binding.btnMedium.setBackgroundResource(if (priority == "Medium") activeBg else inactiveBg)
        binding.btnMedium.setTextColor(if (priority == "Medium") white else dark)
        
        binding.btnHigh.setBackgroundResource(if (priority == "High") activeBg else inactiveBg)
        binding.btnHigh.setTextColor(if (priority == "High") white else dark)
    }
}
