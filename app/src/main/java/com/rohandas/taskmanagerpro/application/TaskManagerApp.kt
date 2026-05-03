package com.rohandas.taskmanagerpro.application

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics

class TaskManagerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCrashlyticsCollectionEnabled(true)
        
        // Track the user across sessions if they are already logged in
        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.let {
            crashlytics.setUserId(it.uid)
        }
        
        crashlytics.log("Application onCreate: Crashlytics initialized")
    }
}
