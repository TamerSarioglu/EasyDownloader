package com.tamersarioglu.easydownloader.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.domain.repository.VideoDownloaderRepository
import com.tamersarioglu.easydownloader.domain.usecase.LoginUseCase
import com.tamersarioglu.easydownloader.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val loginUseCase: LoginUseCase,
    private val repository: VideoDownloaderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _registrationForm = MutableStateFlow(AuthFormState())
    val registrationForm: StateFlow<AuthFormState> = _registrationForm.asStateFlow()

    private val _loginForm = MutableStateFlow(AuthFormState())
    val loginForm: StateFlow<AuthFormState> = _loginForm.asStateFlow()

    init {
        checkAuthenticationStatus()
    }

    private fun checkAuthenticationStatus() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val isAuthenticated = repository.isAuthenticated()
                if (isAuthenticated) {
                    val token = repository.getAuthToken()
                    if (!token.isNullOrEmpty()) {
                        validateTokenWithServer()
                    } else {
                        handleTokenExpiration()
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoggedIn = false, isLoading = false)
                }
            } catch (e: Exception) {
                handleTokenExpiration()
            }
        }
    }

    private suspend fun validateTokenWithServer() {
        try {
            val result = repository.getUserVideos()

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoggedIn = true,
                        isLoading = false,
                        username = ""
                    )
                },
                onFailure = { error ->
                    when (error) {
                        is AppError.UnauthorizedError -> {
                            handleTokenExpiration()
                        }

                        is AppError.NetworkError -> {
                            _uiState.value = _uiState.value.copy(
                                isLoggedIn = true,
                                isLoading = false,
                                username = ""
                            )
                        }

                        else -> {
                            _uiState.value = _uiState.value.copy(
                                isLoggedIn = true,
                                isLoading = false,
                                username = ""
                            )
                        }
                    }
                }
            )
        } catch (e: Exception) {
            handleTokenExpiration()
        }
    }

    private suspend fun handleTokenExpiration() {
        try {
            repository.clearAuthToken()
        } catch (e: Exception) {
        }

        _uiState.value =
            _uiState.value.copy(isLoggedIn = false, isLoading = false, username = "", error = null)
    }

    fun updateRegistrationUsername(username: String) {
        _registrationForm.value = _registrationForm.value.copy(
            username = username,
            usernameError = null
        )
    }

    fun updateRegistrationPassword(password: String) {
        _registrationForm.value = _registrationForm.value.copy(
            password = password,
            passwordError = null
        )
    }

    fun updateLoginUsername(username: String) {
        _loginForm.value = _loginForm.value.copy(
            username = username,
            usernameError = null
        )
    }

    fun updateLoginPassword(password: String) {
        _loginForm.value = _loginForm.value.copy(
            password = password,
            passwordError = null
        )
    }

    fun register() {
        val currentForm = _registrationForm.value

        clearErrors()

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val result = registerUseCase(
                    username = currentForm.username,
                    password = currentForm.password
                )

                result.fold(
                    onSuccess = { user ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            username = user.username,
                            error = null
                        )

                        _registrationForm.value = AuthFormState()
                    },
                    onFailure = { error -> handleAuthError(error) }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "An unexpected error occurred. Please try again."
                )
            }
        }
    }

    fun login() {
        val currentForm = _loginForm.value

        clearErrors()

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val result = loginUseCase(
                    username = currentForm.username,
                    password = currentForm.password
                )

                result.fold(
                    onSuccess = { user ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            username = user.username,
                            error = null
                        )

                        _loginForm.value = AuthFormState()
                    },
                    onFailure = { error -> handleAuthError(error) }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "An unexpected error occurred. Please try again."
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                repository.clearAuthToken()
                _uiState.value = AuthUiState()
                _registrationForm.value = AuthFormState()
                _loginForm.value = AuthFormState()
            } catch (e: Exception) {
                _uiState.value = AuthUiState()
                _registrationForm.value = AuthFormState()
                _loginForm.value = AuthFormState()
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun clearErrors() {
        _registrationForm.value =
            _registrationForm.value.copy(usernameError = null, passwordError = null)
        _loginForm.value = _loginForm.value.copy(usernameError = null, passwordError = null)
    }

    private fun handleAuthError(error: Throwable) {
        _uiState.value = _uiState.value.copy(isLoading = false)

        when (error) {
            is AppError.ValidationError -> {
                when (error.field) {
                    "username" -> {
                        _registrationForm.value =
                            _registrationForm.value.copy(usernameError = error.message)
                        _loginForm.value = _loginForm.value.copy(usernameError = error.message)
                    }

                    "password" -> {
                        _registrationForm.value =
                            _registrationForm.value.copy(passwordError = error.message)
                        _loginForm.value = _loginForm.value.copy(passwordError = error.message)
                    }
                }
            }

            is AppError.ApiError -> {
                val message = when (error.code) {
                    "USER_EXISTS" -> "Username already exists. Please choose a different username."
                    "INVALID_CREDENTIALS" -> "Invalid username or password. Please try again."
                    "USER_NOT_FOUND" -> "Account not found. Please check your username or register."
                    else -> error.message
                }
                _uiState.value = _uiState.value.copy(error = message)
            }

            is AppError.NetworkError -> {
                _uiState.value = _uiState.value.copy(
                    error = "Network error. Please check your connection and try again."
                )
            }

            is AppError.ServerError -> {
                _uiState.value = _uiState.value.copy(
                    error = "Server is temporarily unavailable. Please try again later."
                )
            }

            is AppError.UnauthorizedError -> {
                _uiState.value =
                    _uiState.value.copy(error = "Authentication failed. Please try again.")
            }

            else -> {
                _uiState.value = _uiState.value.copy(
                    error = "An unexpected error occurred. Please try again."
                )
            }
        }
    }

    fun isRegistrationFormValid(): Boolean {
        val form = _registrationForm.value
        return form.username.trim().length >= 3 &&
                form.password.length >= 6 &&
                form.usernameError == null &&
                form.passwordError == null
    }

    fun isLoginFormValid(): Boolean {
        val form = _loginForm.value
        return form.username.trim().isNotEmpty() &&
                form.password.isNotEmpty() &&
                form.usernameError == null &&
                form.passwordError == null
    }

    fun handleTokenExpiredDuringUsage() {
        viewModelScope.launch { handleTokenExpiration() }
    }

    fun refreshAuthenticationState() {
        viewModelScope.launch {
            try {
                val isAuthenticated = repository.isAuthenticated()
                if (isAuthenticated) {
                    validateTokenWithServer()
                } else {
                    _uiState.value = _uiState.value.copy(isLoggedIn = false)
                }
            } catch (e: Exception) {
            }
        }
    }

    fun isCurrentlyAuthenticated(): Boolean {
        return _uiState.value.isLoggedIn
    }
}

data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val username: String = "",
    val error: String? = null
)

data class AuthFormState(
    val username: String = "",
    val password: String = "",
    val usernameError: String? = null,
    val passwordError: String? = null
)