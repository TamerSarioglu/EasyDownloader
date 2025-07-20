package com.tamersarioglu.easydownloader.presentation.auth.login

data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val error: String? = null,
    val username: String = ""
)

data class LoginFormState(
    val username: String = "",
    val password: String = "",
    val usernameError: String? = null,
    val passwordError: String? = null
)