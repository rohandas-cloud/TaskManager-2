package com.rohandas.taskmanagerpro.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import com.rohandas.taskmanagerpro.R
import com.rohandas.taskmanagerpro.ui.dashboard.MainActivity
import com.rohandas.taskmanagerpro.viewmodel.AuthState
import com.rohandas.taskmanagerpro.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    viewModel.signIn(email, password)
                } else {
                    Toast.makeText(this, "Please enter a valid email (e.g. .com not .con)", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show()
            }
        }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // Modern State Observation using repeatOnLifecycle
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { state ->
                    when (state) {
                        is AuthState.Loading -> {
                            btnLogin.isEnabled = false
                            btnLogin.text = "Signing in..."
                        }
                        is AuthState.Success -> {
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }
                        is AuthState.Error -> {
                            btnLogin.isEnabled = true
                            btnLogin.text = "Sign In"
                            
                            val message = if (state.message.contains("blocked", ignoreCase = true)) {
                                "Too many attempts. Please try again in a few minutes."
                            } else {
                                state.message
                            }
                            Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            btnLogin.isEnabled = true
                            btnLogin.text = "Sign In"
                        }
                    }
                }
            }
        }
    }
}
