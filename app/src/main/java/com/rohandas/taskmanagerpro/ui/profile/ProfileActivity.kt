package com.rohandas.taskmanagerpro.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.rohandas.taskmanagerpro.databinding.ActivityProfileBinding
import com.rohandas.taskmanagerpro.ui.auth.LoginActivity
import com.rohandas.taskmanagerpro.utils.NavigationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        NavigationHelper.setupBottomMenu(this)
        setupUserInfo()

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ -> performLogout() }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnTestCrash.setOnClickListener {
            Log.d("ProfileActivity", "Test Crash triggered by user - App will now close.")
            FirebaseCrashlytics.getInstance().setCustomKey("last_action", "clicked_test_crash")
            
            // This will force the app to crash and close immediately
            throw RuntimeException("Manual Test Crash - TaskManagerPro")
        }
    }

    private fun setupUserInfo() {
        val user = FirebaseAuth.getInstance().currentUser
        binding.tvProfileName.text = user?.displayName ?: "User"
        binding.tvProfileEmail.text = user?.email ?: "No Email"
    }

    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
