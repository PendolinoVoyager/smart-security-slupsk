package com.example.iot_app_android.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel : ViewModel() {
    private val _email = MutableStateFlow("")
    private val _password = MutableStateFlow("")
    private val _errorMessage = MutableStateFlow<String?>(null)

    val email: StateFlow<String> get() = _email
    val password: StateFlow<String> get() = _password
    val errorMessage: StateFlow<String?> get() = _errorMessage

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }

    fun login(context: Context, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        _errorMessage.value = null

        if (_email.value.isBlank()) {
            _errorMessage.value = "Email cannot be empty."
            onFailure("Email cannot be empty.")
            return
        }

        if (!_email.value.contains("@")) {
            _errorMessage.value = "Invalid email address."
            onFailure("Invalid email address.")
            return
        }

        if (_password.value.isBlank()) {
            _errorMessage.value = "Password cannot be empty."
            onFailure("Password cannot be empty.")
            return
        }

        viewModelScope.launch {
            val (success, message) = AuthService.login(context, _email.value, _password.value)
            if (success) {
                onSuccess()
            } else {
                _errorMessage.value = message
                onFailure(message)
            }
        }
    }
}