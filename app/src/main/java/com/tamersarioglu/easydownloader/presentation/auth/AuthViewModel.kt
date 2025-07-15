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

/**
 * ViewModel for authentication screens (login and registration).
 *
 * This ViewModel handles:
 * - Registration and login functionality
 * - AuthUiState management with proper loading and error states
 * - Input validation and user feedback
 * - Authentication success and navigation handling
 *
 * Requirements covered: 1.2, 1.3, 1.4, 1.5, 2.2, 2.3, 2.4, 5.2, 6.6
 */
@HiltViewModel
class AuthViewModel
@Inject
constructor(
        private val registerUseCase: RegisterUseCase,
        private val loginUseCase: LoginUseCase,
        private val repository: VideoDownloaderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Form state for registration
    private val _registrationForm = MutableStateFlow(AuthFormState())
    val registrationForm: StateFlow<AuthFormState> = _registrationForm.asStateFlow()

    // Form state for login
    private val _loginForm = MutableStateFlow(AuthFormState())
    val loginForm: StateFlow<AuthFormState> = _loginForm.asStateFlow()

    init {
        checkAuthenticationStatus()
    }

    /**
     * Checks if user is already authenticated on ViewModel initialization. This handles automatic
     * login for returning users and validates token.
     *
     * Requirements: 2.5, 2.6, 5.2, 5.3
     */
    private fun checkAuthenticationStatus() {
        // Set loading state while checking authentication
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                val isAuthenticated = repository.isAuthenticated()
                if (isAuthenticated) {
                    val token = repository.getAuthToken()
                    if (!token.isNullOrEmpty()) {
                        // Validate token by making a test API call
                        validateTokenWithServer()
                    } else {
                        // Token is null or empty, user needs to login
                        handleTokenExpiration()
                    }
                } else {
                    // No token stored, user needs to login
                    _uiState.value = _uiState.value.copy(isLoggedIn = false, isLoading = false)
                }
            } catch (e: Exception) {
                // If there's an error checking auth status, assume not authenticated
                // This is a safe fallback that doesn't expose the user to errors
                handleTokenExpiration()
            }
        }
    }

    /**
     * Validates the stored token by making a test API call. If token is valid, user is
     * automatically logged in. If token is expired or invalid, user is logged out.
     *
     * Requirements: 2.5, 2.6, 5.3
     */
    private suspend fun validateTokenWithServer() {
        try {
            // Try to fetch user videos to validate token
            val result = repository.getUserVideos()

            result.fold(
                    onSuccess = {
                        // Token is valid, user is authenticated
                        _uiState.value =
                                _uiState.value.copy(
                                        isLoggedIn = true,
                                        isLoading = false,
                                        username =
                                                "" // We don't store username locally for security
                                )
                    },
                    onFailure = { error ->
                        when (error) {
                            is AppError.UnauthorizedError -> {
                                // Token is expired or invalid
                                handleTokenExpiration()
                            }
                            is AppError.NetworkError -> {
                                // Network error - assume user is authenticated but show offline
                                // state
                                // We'll validate token when network is restored
                                _uiState.value =
                                        _uiState.value.copy(isLoggedIn = true, isLoading = false, username = "")
                            }
                            else -> {
                                // Other errors - assume token is valid but there's a server issue
                                _uiState.value =
                                        _uiState.value.copy(isLoggedIn = true, isLoading = false, username = "")
                            }
                        }
                    }
            )
        } catch (e: Exception) {
            // If validation fails, handle as token expiration
            handleTokenExpiration()
        }
    }

    /**
     * Handles token expiration scenarios by clearing stored data and logging out user.
     *
     * Requirements: 2.6, 5.3
     */
    private suspend fun handleTokenExpiration() {
        try {
            // Clear the expired/invalid token
            repository.clearAuthToken()
        } catch (e: Exception) {
            // Even if clearing fails, we should reset the UI state
        }

        // Reset UI state to logged out
        _uiState.value = _uiState.value.copy(isLoggedIn = false, isLoading = false, username = "", error = null)
    }

    /** Updates the username field for registration form. */
    fun updateRegistrationUsername(username: String) {
        _registrationForm.value =
                _registrationForm.value.copy(
                        username = username,
                        usernameError = null // Clear error when user starts typing
                )
    }

    /** Updates the password field for registration form. */
    fun updateRegistrationPassword(password: String) {
        _registrationForm.value =
                _registrationForm.value.copy(
                        password = password,
                        passwordError = null // Clear error when user starts typing
                )
    }

    /** Updates the username field for login form. */
    fun updateLoginUsername(username: String) {
        _loginForm.value =
                _loginForm.value.copy(
                        username = username,
                        usernameError = null // Clear error when user starts typing
                )
    }

    /** Updates the password field for login form. */
    fun updateLoginPassword(password: String) {
        _loginForm.value =
                _loginForm.value.copy(
                        password = password,
                        passwordError = null // Clear error when user starts typing
                )
    }

    /**
     * Handles user registration with input validation and error handling.
     *
     * Requirements: 1.2, 1.4, 1.5
     */
    fun register() {
        val currentForm = _registrationForm.value

        // Clear any existing errors
        clearErrors()

        // Set loading state
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val result =
                        registerUseCase(
                                username = currentForm.username,
                                password = currentForm.password
                        )

                result.fold(
                        onSuccess = { user ->
                            // Registration successful
                            _uiState.value =
                                    _uiState.value.copy(
                                            isLoading = false,
                                            isLoggedIn = true,
                                            username = user.username,
                                            error = null
                                    )

                            // Clear the form
                            _registrationForm.value = AuthFormState()
                        },
                        onFailure = { error -> handleAuthError(error) }
                )
            } catch (e: Exception) {
                // Handle unexpected errors
                _uiState.value =
                        _uiState.value.copy(
                                isLoading = false,
                                error = "An unexpected error occurred. Please try again."
                        )
            }
        }
    }

    /**
     * Handles user login with credential validation and error handling.
     *
     * Requirements: 2.2, 2.4
     */
    fun login() {
        val currentForm = _loginForm.value

        // Clear any existing errors
        clearErrors()

        // Set loading state
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val result =
                        loginUseCase(
                                username = currentForm.username,
                                password = currentForm.password
                        )

                result.fold(
                        onSuccess = { user ->
                            // Login successful
                            _uiState.value =
                                    _uiState.value.copy(
                                            isLoading = false,
                                            isLoggedIn = true,
                                            username = user.username,
                                            error = null
                                    )

                            // Clear the form
                            _loginForm.value = AuthFormState()
                        },
                        onFailure = { error -> handleAuthError(error) }
                )
            } catch (e: Exception) {
                // Handle unexpected errors
                _uiState.value =
                        _uiState.value.copy(
                                isLoading = false,
                                error = "An unexpected error occurred. Please try again."
                        )
            }
        }
    }

    /**
     * Handles logout functionality. Clears authentication tokens and resets UI state.
     *
     * Requirements: 5.2
     */
    fun logout() {
        viewModelScope.launch {
            try {
                repository.clearAuthToken()

                // Reset all state
                _uiState.value = AuthUiState()
                _registrationForm.value = AuthFormState()
                _loginForm.value = AuthFormState()
            } catch (e: Exception) {
                // Even if clearing token fails, reset UI state for security
                _uiState.value = AuthUiState()
                _registrationForm.value = AuthFormState()
                _loginForm.value = AuthFormState()
            }
        }
    }

    /** Clears any error messages from the UI state. */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /** Clears all form errors. */
    private fun clearErrors() {
        _registrationForm.value =
                _registrationForm.value.copy(usernameError = null, passwordError = null)
        _loginForm.value = _loginForm.value.copy(usernameError = null, passwordError = null)
    }

    /**
     * Handles authentication errors and updates UI state accordingly.
     *
     * Requirements: 6.6 (Error handling)
     */
    private fun handleAuthError(error: Throwable) {
        _uiState.value = _uiState.value.copy(isLoading = false)

        when (error) {
            is AppError.ValidationError -> {
                // Handle field-specific validation errors
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
                // Handle API-specific errors
                val message =
                        when (error.code) {
                            "USER_EXISTS" ->
                                    "Username already exists. Please choose a different username."
                            "INVALID_CREDENTIALS" ->
                                    "Invalid username or password. Please try again."
                            "USER_NOT_FOUND" ->
                                    "Account not found. Please check your username or register."
                            else -> error.message
                        }
                _uiState.value = _uiState.value.copy(error = message)
            }
            is AppError.NetworkError -> {
                _uiState.value =
                        _uiState.value.copy(
                                error = "Network error. Please check your connection and try again."
                        )
            }
            is AppError.ServerError -> {
                _uiState.value =
                        _uiState.value.copy(
                                error = "Server is temporarily unavailable. Please try again later."
                        )
            }
            is AppError.UnauthorizedError -> {
                _uiState.value =
                        _uiState.value.copy(error = "Authentication failed. Please try again.")
            }
            else -> {
                _uiState.value =
                        _uiState.value.copy(
                                error = "An unexpected error occurred. Please try again."
                        )
            }
        }
    }

    /** Validates registration form and returns true if valid. */
    fun isRegistrationFormValid(): Boolean {
        val form = _registrationForm.value
        return form.username.trim().length >= 3 &&
                form.password.length >= 6 &&
                form.usernameError == null &&
                form.passwordError == null
    }

    /** Validates login form and returns true if valid. */
    fun isLoginFormValid(): Boolean {
        val form = _loginForm.value
        return form.username.trim().isNotEmpty() &&
                form.password.isNotEmpty() &&
                form.usernameError == null &&
                form.passwordError == null
    }

    /**
     * Handles token expiration that occurs during app usage. This method can be called by other
     * ViewModels when they receive UnauthorizedError.
     *
     * Requirements: 2.6, 5.3
     */
    fun handleTokenExpiredDuringUsage() {
        viewModelScope.launch { handleTokenExpiration() }
    }

    /**
     * Refreshes the authentication state by re-validating the token. Useful for handling network
     * restoration scenarios.
     *
     * Requirements: 2.5, 2.6
     */
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
                // If refresh fails, maintain current state but don't show errors
                // This prevents disrupting user experience during background checks
            }
        }
    }

    /**
     * Checks if the user is currently authenticated based on UI state. This is a synchronous check
     * that doesn't validate with server.
     *
     * Requirements: 5.2, 5.3
     */
    fun isCurrentlyAuthenticated(): Boolean {
        return _uiState.value.isLoggedIn
    }
}

/** Data class representing the state of authentication forms. */
data class AuthFormState(
        val username: String = "",
        val password: String = "",
        val usernameError: String? = null,
        val passwordError: String? = null
)
