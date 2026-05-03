package com.rohandas.taskmanagerpro.utils

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.rohandas.taskmanagerpro.data.model.Task
import com.rohandas.taskmanagerpro.worker.TaskWorker
import java.util.concurrent.TimeUnit

/**
 * TaskScheduler - Schedules WorkManager jobs for task deadline reminders.
 *
 * FIXES:
 *  1. Moved to package com.rohandas.taskmanagerpro.utils  (was declared as
 *     "worker" while sitting in the "utills" folder — build broke because
 *     TaskViewModel imported it as "utils.TaskScheduler").
 *  2. Folder renamed from "utills" → "utils" (typo fix).
 *  3. Class name changed from TaskWorker → TaskScheduler to match the
 *     import in TaskViewModel.
 */
object TaskScheduler {

    fun scheduleTask(context: Context, task: Task) {
        if (task.deadline <= 0 || task.status == "Done") return

        // Calculate trigger time: 2 minutes before the deadline
        val twoMinutesInMillis = 2 * 60 * 1000L
        val triggerTime = task.deadline - twoMinutesInMillis
        val delay = triggerTime - System.currentTimeMillis()

        // If the 2-minute mark has already passed but the deadline hasn't, 
        // we schedule it to run immediately (delay = 0).
        // If the deadline itself has passed, we don't schedule anything.
        if (task.deadline <= System.currentTimeMillis()) return

        val actualDelay = if (delay < 0) 0L else delay

        val inputData = Data.Builder()
            .putString("title", "Task Reminder")
            .putString("description", "You had to complete '${task.title}' before ${task.time}. Are you done with the task?")
            .build()

        val workRequest = OneTimeWorkRequestBuilder<TaskWorker>()
            .setInitialDelay(actualDelay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(task.id)          // Tag by task ID so it can be cancelled
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }


    fun cancelTask(context: Context, taskId: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag(taskId)
    }
}
