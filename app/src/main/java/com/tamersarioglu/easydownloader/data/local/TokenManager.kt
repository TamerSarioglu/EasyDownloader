package com.tamersarioglu.easydownloader.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages JWT authentication tokens using DataStore
 * Provides secure storage and retrieval of authentication tokens
 */
@Singleton
class TokenManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    
    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
    }
    
    /**
     * Save authentication token to secure storage
     * @param token JWT token to save
     */
    suspend fun saveAuthToken(token: String) {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
        }
    }
    
    /**
     * Get authentication token from storage
     * @return JWT token or null if not found
     */
    suspend fun getAuthToken(): String? {
        return dataStore.data.first()[AUTH_TOKEN_KEY]
    }
    
    /**
     * Get authentication token as Flow for reactive updates
     * @return Flow of JWT token or null
     */
    fun getAuthTokenFlow(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[AUTH_TOKEN_KEY]
        }
    }
    
    /**
     * Clear authentication token from storage
     * Used during logout or when token expires
     */
    suspend fun clearAuthToken() {
        dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN_KEY)
        }
    }
    
    /**
     * Check if user is authenticated
     * @return true if valid token exists, false otherwise
     */
    suspend fun isAuthenticated(): Boolean {
        return getAuthToken() != null
    }
}