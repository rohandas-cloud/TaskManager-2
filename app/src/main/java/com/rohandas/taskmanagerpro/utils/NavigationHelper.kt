package com.rohandas.taskmanagerpro.utils

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.view.View
import com.rohandas.taskmanagerpro.R
import com.rohandas.taskmanagerpro.ui.auth.LoginActivity
import com.rohandas.taskmanagerpro.ui.dashboard.MainActivity
import com.rohandas.taskmanagerpro.ui.notification.NotificationsActivity
import com.rohandas.taskmanagerpro.ui.profile.ProfileActivity
import com.rohandas.taskmanagerpro.ui.task.AddProjectActivity
import com.rohandas.taskmanagerpro.ui.task.AddTaskActivity
import com.rohandas.taskmanagerpro.ui.task.TodayTasksActivity
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
            showAddOptions(activity)
        }
    }

    fun showAddOptions(activity: Activity) {
        // Directly navigate to Add Task activity as requested
        navigate(activity, AddTaskActivity::class.java)
    }

    private fun navigate(from: Activity, to: Class<*>) {
        from.startActivity(Intent(from, to))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            from.overrideActivityTransition(
                Activity.OVERRIDE_TRANSITION_OPEN,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        } else {
            @Suppress("DEPRECATION")
            from.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}
