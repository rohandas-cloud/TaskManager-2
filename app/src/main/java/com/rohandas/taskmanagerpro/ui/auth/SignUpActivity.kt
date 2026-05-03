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

class SignUpActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        btnSignUp.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    viewModel.signUp(email, password, name)
                } else {
                    Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        tvLogin.setOnClickListener {
            finish()
        }

        // Modern State Observation using repeatOnLifecycle
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { state ->
                    when (state) {
                        is AuthState.Loading -> {
                            btnSignUp.isEnabled = false
                            btnSignUp.text = "Creating Account..."
                        }
                        is AuthState.Success -> {
                            startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
                            finishAffinity() // Clear task stack
                        }
                        is AuthState.Error -> {
                            btnSignUp.isEnabled = true
                            btnSignUp.text = "Create Account"
                            Toast.makeText(this@SignUpActivity, state.message, Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            btnSignUp.isEnabled = true
                            btnSignUp.text = "Create Account"
                        }
                    }
                }
            }
        }
    }
}
