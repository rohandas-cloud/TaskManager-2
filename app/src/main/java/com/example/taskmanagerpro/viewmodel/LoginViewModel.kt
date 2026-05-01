package com.example.taskmanagerpro.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanagerpro.data.remote.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _loginState.value = LoginState.Error("Please enter email and password")
            return
        }
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            val result = repository.signIn(email, password)
            result.onSuccess { user ->
                _loginState.value = LoginState.Success(user)
            }.onFailure { exception ->
                _loginState.value = LoginState.Error(
                    exception.message ?: "Login failed. Please try again."
                )
            }
        }
    }

    fun isUserLoggedIn() = repository.currentUser != null
}

sealed class LoginState {
    object Loading : LoginState()
    data class Success(val user: FirebaseUser) : LoginState()
    data class Error(val message: String) : LoginState()
}
