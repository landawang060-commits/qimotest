package com.shub39.grit.domain

import android.util.Log

class AuthServiceImpl : AuthService {

    private val registeredUsers = mutableListOf<User>()

    init {
        registeredUsers.add(User("admin", "admin@example.com", "admin123"))
        registeredUsers.add(User("test", "test@example.com", "test123"))
    }

    override suspend fun login(username: String, password: String): AuthResult {
        try {
            Thread.sleep(1000)

            val user = registeredUsers.find { 
                (it.username == username || it.email == username) && it.password == password 
            }
            
            return if (user != null) {
                AuthResult.Success
            } else {
                AuthResult.Error("用户名或密码错误")
            }
        } catch (e: Exception) {
            Log.e("AuthService", "Login error", e)
            return AuthResult.NetworkError
        }
    }

    override suspend fun register(username: String, email: String, password: String): AuthResult {
        try {
            Thread.sleep(1000)

            if (registeredUsers.any { it.username == username }) {
                return AuthResult.Error("用户名已存在")
            }

            if (registeredUsers.any { it.email == email }) {
                return AuthResult.Error("邮箱已被注册")
            }

            registeredUsers.add(User(username, email, password))
            return AuthResult.Success
        } catch (e: Exception) {
            Log.e("AuthService", "Register error", e)
            return AuthResult.NetworkError
        }
    }

    private data class User(
        val username: String,
        val email: String,
        val password: String,
    )
}