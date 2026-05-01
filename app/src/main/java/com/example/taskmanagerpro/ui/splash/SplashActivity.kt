package com.example.taskmanagerpro.ui.splash

// FIX: Was declared as "package com.example.taskmanagerpro" (root) while
// living in ui/SplashActivity/. Corrected to match its folder location.
// AndroidManifest.xml entry updated accordingly (see AndroidManifest fix).

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.taskmanagerpro.ui.auth.LoginActivity
import com.example.taskmanagerpro.ui.dashboard.MainActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // No layout needed — purely a routing gate
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
