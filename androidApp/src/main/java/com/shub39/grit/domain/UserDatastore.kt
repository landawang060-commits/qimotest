package com.shub39.grit.domain

import kotlinx.coroutines.flow.Flow

interface UserDatastore {
    fun getIsLoggedIn(): Flow<Boolean>

    suspend fun setIsLoggedIn(isLoggedIn: Boolean)

    fun getUsername(): Flow<String>

    suspend fun setUsername(username: String)

    fun getPassword(): Flow<String>

    suspend fun setPassword(password: String)

    fun getRememberPassword(): Flow<Boolean>

    suspend fun setRememberPassword(remember: Boolean)

    suspend fun clearUserData()
}