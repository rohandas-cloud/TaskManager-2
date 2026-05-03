package com.rohandas.taskmanagerpro.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rohandas.taskmanagerpro.ui.auth.LoginActivity
import com.rohandas.taskmanagerpro.ui.dashboard.MainActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Analytics
        val analytics = com.google.firebase.analytics.FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_NAME, "Splash Startup")
        analytics.logEvent(com.google.firebase.analytics.FirebaseAnalytics.Event.APP_OPEN, bundle)
        
        // Crashlytics Test Signal: Helps verify connectivity and "wake up" the dashboard
        val crashlytics = com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
        crashlytics.log("SplashActivity: App startup signal sent")
        crashlytics.setCustomKey("startup_time", System.currentTimeMillis())
        
        // Router: If logged in -> Dashboard, else -> Login
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
