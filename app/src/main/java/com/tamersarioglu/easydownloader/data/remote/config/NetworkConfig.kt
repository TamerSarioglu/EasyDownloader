package com.tamersarioglu.easydownloader.data.remote.config

object NetworkConfig {
    
    const val BASE_URL = "https://api.easydownloader.com/api/"
    
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
    
    const val MAX_RETRY_ATTEMPTS = 3
    const val RETRY_DELAY_MS = 1000L
    
    const val CONTENT_TYPE_JSON = "application/json"
    const val AUTHORIZATION_HEADER = "Authorization"
    const val BEARER_PREFIX = "Bearer "
    
    const val DATASTORE_NAME = "auth_preferences"
    
    val EXCLUDED_AUTH_ENDPOINTS = setOf(
        "register",
        "login"
    )
}