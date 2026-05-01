package com.example.taskmanagerpro.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.taskmanagerpro.R
import com.example.taskmanagerpro.ui.auth.LoginActivity       // FIX: was root package
import com.example.taskmanagerpro.ui.profile.ProfileActivity
import com.example.taskmanagerpro.ui.task.AddProjectActivity
import com.example.taskmanagerpro.ui.task.TodayTasksActivity
import com.example.taskmanagerpro.utils.NavigationHelper       // FIX: was root package / typo
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        checkAuthentication()
        NavigationHelper.setupBottomMenu(this)

        findViewById<TextView>(R.id.btnViewTasks).setOnClickListener {
            startActivity(Intent(this, TodayTasksActivity::class.java))
        }
        findViewById<View>(R.id.profileSection).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    override fun onStart() {
        super.onStart()
        checkAuthentication()
    }

    private fun checkAuthentication() {
        if (auth.currentUser == null) navigateToLogin()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
