package com.example.taskmanagerpro.utils

import android.app.Activity
import android.content.Intent
import android.view.View
import com.example.taskmanagerpro.R
import com.example.taskmanagerpro.ui.auth.LoginActivity
import com.example.taskmanagerpro.ui.dashboard.MainActivity
import com.example.taskmanagerpro.ui.notification.NotificationsActivity
import com.example.taskmanagerpro.ui.profile.ProfileActivity
import com.example.taskmanagerpro.ui.task.AddProjectActivity
import com.example.taskmanagerpro.ui.task.TodayTasksActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

object NavigationHelper {
    fun setupBottomMenu(activity: Activity) {
        val navHome = activity.findViewById<View>(R.id.nav_home)
        val navTasks = activity.findViewById<View>(R.id.nav_tasks)
        val navNotifications = activity.findViewById<View>(R.id.nav_notifications)
        val navProfile = activity.findViewById<View>(R.id.nav_profile)
        val fabAdd = activity.findViewById<FloatingActionButton>(R.id.fabAdd)

        listOf(navHome, navTasks, navNotifications, navProfile).forEach { it?.alpha = 0.4f }
        when (activity) {
            is MainActivity -> navHome?.alpha = 1.0f
            is TodayTasksActivity -> navTasks?.alpha = 1.0f
            is NotificationsActivity -> navNotifications?.alpha = 1.0f
            is ProfileActivity -> navProfile?.alpha = 1.0f
        }

        navHome?.setOnClickListener {
            if (activity !is MainActivity) navigate(activity, MainActivity::class.java)
        }
        navTasks?.setOnClickListener {
            if (activity !is TodayTasksActivity) navigate(activity, TodayTasksActivity::class.java)
        }
        navNotifications?.setOnClickListener {
            if (activity !is NotificationsActivity) navigate(activity, NotificationsActivity::class.java)
        }
        navProfile?.setOnClickListener {
            if (activity !is ProfileActivity) navigate(activity, ProfileActivity::class.java)
        }
        fabAdd?.setOnClickListener {
            if (activity !is AddProjectActivity) navigate(activity, AddProjectActivity::class.java)
        }
    }

    private fun navigate(from: Activity, to: Class<*>) {
        from.startActivity(Intent(from, to))
        from.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}