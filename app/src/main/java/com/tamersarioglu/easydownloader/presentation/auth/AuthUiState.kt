package com.tamersarioglu.easydownloader.presentation.auth

/**
 * UI state for authentication screens (login and registration)
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val username: String = ""
)