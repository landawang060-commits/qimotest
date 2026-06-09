package com.shub39.grit.domain

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
    object NetworkError : AuthResult()
}

interface AuthService {
    suspend fun login(username: String, password: String): AuthResult
    suspend fun register(username: String, email: String, password: String): AuthResult
}