package com.tamersarioglu.easydownloader.data.remote.config

/**
 * Network configuration constants and settings
 * Centralizes network-related configuration values
 */
object NetworkConfig {
    
    // Base URL for the API
    const val BASE_URL = "https://api.easydownloader.com/api/"
    
    // Timeout configurations (in seconds)
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
    
    // Retry configuration
    const val MAX_RETRY_ATTEMPTS = 3
    const val RETRY_DELAY_MS = 1000L
    
    // HTTP headers
    const val CONTENT_TYPE_JSON = "application/json"
    const val AUTHORIZATION_HEADER = "Authorization"
    const val BEARER_PREFIX = "Bearer "
    
    // DataStore configuration
    const val DATASTORE_NAME = "auth_preferences"
    
    // Endpoints that don't require authentication
    val EXCLUDED_AUTH_ENDPOINTS = setOf(
        "register",
        "login"
    )
}