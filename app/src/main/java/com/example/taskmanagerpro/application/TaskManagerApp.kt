package com.example.taskmanagerpro.application

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics

class TaskManagerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Enable Crashlytics collection (optional, but good for debug)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
    }
}