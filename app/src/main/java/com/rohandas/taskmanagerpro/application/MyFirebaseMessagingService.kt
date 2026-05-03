package com.rohandas.taskmanagerpro.application

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.rohandas.taskmanagerpro.R
import com.rohandas.taskmanagerpro.ui.dashboard.MainActivity
import com.rohandas.taskmanagerpro.ui.notification.NotificationsActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rohandas.taskmanagerpro.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val authRepository = AuthRepository()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        remoteMessage.notification?.let {
            sendNotification(it.title ?: "Task Update", it.body ?: "Check your tasks")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Update token in Realtime Database
        scope.launch {
            authRepository.updateFcmToken(token)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun sendNotification(title: String, messageBody: String) {
        showNotification(this, title, messageBody)
    }

    companion object {
        fun showNotification(context: Context, title: String, message: String) {
            val intent = Intent(context, NotificationsActivity::class.java) // Open Notifications page
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

            val channelId = "task_notifications"
            val notificationBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Set High Priority
                .setDefaults(NotificationCompat.DEFAULT_ALL) // Enable sound/vibration
                .setContentIntent(pendingIntent)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId, 
                    "Task Notifications", 
                    NotificationManager.IMPORTANCE_HIGH // Set High Importance
                )
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
        }
    }
}
