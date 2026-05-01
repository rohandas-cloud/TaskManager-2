package com.example.taskmanagerpro.utils

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.taskmanagerpro.data.model.Task
import com.example.taskmanagerpro.worker.TaskWorker
import java.util.concurrent.TimeUnit

/**
 * TaskScheduler - Schedules WorkManager jobs for task deadline reminders.
 *
 * FIXES:
 *  1. Moved to package com.example.taskmanagerpro.utils  (was declared as
 *     "worker" while sitting in the "utills" folder — build broke because
 *     TaskViewModel imported it as "utils.TaskScheduler").
 *  2. Folder renamed from "utills" → "utils" (typo fix).
 *  3. Class name changed from TaskWorker → TaskScheduler to match the
 *     import in TaskViewModel.
 */
object TaskScheduler {

    fun scheduleTask(context: Context, task: Task) {
        val delay = task.deadline - System.currentTimeMillis()
        if (delay <= 0) return  // Deadline already passed — skip scheduling

        val inputData = Data.Builder()
            .putString("title", task.title)
            .putString("description", task.description)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<TaskWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(task.id)          // Tag by task ID so it can be cancelled
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    fun cancelTask(context: Context, taskId: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag(taskId)
    }
}
