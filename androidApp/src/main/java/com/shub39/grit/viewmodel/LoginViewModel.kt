package com.shub39.grit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shub39.grit.domain.AuthResult
import com.shub39.grit.domain.AuthService
import com.shub39.grit.domain.UserDatastore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class LoginViewModel(
    private val userDatastore: UserDatastore,
    private val authService: AuthService,
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            userDatastore.getIsLoggedIn().collect { isLoggedIn ->
                _isLoggedIn.value = isLoggedIn
            }
        }
    }

    fun login(username: String, password: String, rememberPassword: Boolean) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val result = authService.login(username, password)

            when (result) {
                is AuthResult.Success -> {
                    userDatastore.setIsLoggedIn(true)
                    userDatastore.setRememberPassword(rememberPassword)

                    if (rememberPassword) {
                        userDatastore.setUsername(username)
                        userDatastore.setPassword(password)
                    } else {
                        userDatastore.setUsername("")
                        userDatastore.setPassword("")
                    }
                }

                is AuthResult.Error -> {
                    _errorMessage.value = result.message
                }

                AuthResult.NetworkError -> {
                    _errorMessage.value = "网络错误，请稍后重试"
                }
            }

            _isLoading.value = false
        }
    }

    fun register(username: String, email: String, password: String) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val result = authService.register(username, email, password)

            when (result) {
                is AuthResult.Success -> {
                    userDatastore.setIsLoggedIn(true)
                    userDatastore.setRememberPassword(false)
                    userDatastore.setUsername("")
                    userDatastore.setPassword("")
                }

                is AuthResult.Error -> {
                    _errorMessage.value = result.message
                }

                AuthResult.NetworkError -> {
                    _errorMessage.value = "网络错误，请稍后重试"
                }
            }

            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            userDatastore.clearUserData()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    suspend fun getSavedCredentials(): Pair<String, String> {
        val username = userDatastore.getUsername().first()
        val password = userDatastore.getPassword().first()
        return Pair(username, password)
    }

    suspend fun getRememberPassword(): Boolean {
        return userDatastore.getRememberPassword().first()
    }
}