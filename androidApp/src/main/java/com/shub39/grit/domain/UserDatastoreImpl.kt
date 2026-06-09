package com.shub39.grit.domain

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserDatastoreImpl(
    private val dataStore: DataStore<Preferences>,
) : UserDatastore {

    private companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USERNAME = stringPreferencesKey("username")
        val PASSWORD = stringPreferencesKey("password")
        val REMEMBER_PASSWORD = booleanPreferencesKey("remember_password")
    }

    override fun getIsLoggedIn(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[IS_LOGGED_IN] ?: false
        }
    }

    override suspend fun setIsLoggedIn(isLoggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = isLoggedIn
        }
    }

    override fun getUsername(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[USERNAME] ?: ""
        }
    }

    override suspend fun setUsername(username: String) {
        dataStore.edit { preferences ->
            preferences[USERNAME] = username
        }
    }

    override fun getPassword(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[PASSWORD] ?: ""
        }
    }

    override suspend fun setPassword(password: String) {
        dataStore.edit { preferences ->
            preferences[PASSWORD] = password
        }
    }

    override fun getRememberPassword(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[REMEMBER_PASSWORD] ?: false
        }
    }

    override suspend fun setRememberPassword(remember: Boolean) {
        dataStore.edit { preferences ->
            preferences[REMEMBER_PASSWORD] = remember
        }
    }

    override suspend fun clearUserData() {
        dataStore.edit { preferences ->
            preferences.remove(IS_LOGGED_IN)
            preferences.remove(USERNAME)
            preferences.remove(PASSWORD)
            preferences.remove(REMEMBER_PASSWORD)
        }
    }
}