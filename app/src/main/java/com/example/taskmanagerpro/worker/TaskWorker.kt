package com.example.taskmanagerpro.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.taskmanagerpro.R

/**
 * TaskWorker - WorkManager worker that shows a notification when a task deadline arrives.
 *
 * FIXES:
 *  - Separated from TaskScheduler.kt — previously one file tried to be both
 *    the scheduler and the worker, with a mismatched class name.
 *  - Uncommented the channel description line (was accidentally commented out).
 */
class TaskWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val title = inputData.getString("title") ?: "Task Reminder"
        val description = inputData.getString("description") ?: "You have a pending task."
        showNotification(title, description)
        return Result.success()
    }

    private fun showNotification(title: String, description: String) {
        val manager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "task_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                this.description = "Notifications for task deadlines" // <-- was commented out
            }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification_bell)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
