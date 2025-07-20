package com.tamersarioglu.easydownloader.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.domain.repository.VideoDownloaderRepository
import com.tamersarioglu.easydownloader.presentation.util.ErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AuthStateViewModel @Inject constructor(
    private val repository: VideoDownloaderRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthenticationStatus()
    }

    private fun checkAuthenticationStatus() {
        _authState.value = _authState.value.copy(isLoading = true)

        viewModelScope.launch {
            val isAuthenticated = repository.isAuthenticated()
            if (isAuthenticated) {
                val token = repository.getAuthToken()
                if (!token.isNullOrEmpty()) {
                    validateTokenWithServer()
                } else {
                    handleTokenExpiration()
                }
            } else {
                _authState.value = _authState.value.copy(
                    isAuthenticated = false, 
                    isLoading = false
                )
            }
        }
    }

    private suspend fun validateTokenWithServer() {
        repository.getUserVideos().fold(
            onSuccess = {
                _authState.value = _authState.value.copy(
                    isAuthenticated = true,
                    isLoading = false
                )
            },
            onFailure = { error ->
                when (error) {
                    is AppError.UnauthorizedError -> {
                        handleTokenExpiration()
                    }
                    is AppError.NetworkError -> {
                        // Assume authenticated if network error (offline scenario)
                        _authState.value = _authState.value.copy(
                            isAuthenticated = true,
                            isLoading = false
                        )
                    }
                    else -> {
                        // For other errors, assume authenticated but log the issue
                        _authState.value = _authState.value.copy(
                            isAuthenticated = true,
                            isLoading = false
                        )
                    }
                }
            }
        )
    }

    private suspend fun handleTokenExpiration() {
        repository.clearAuthToken()
        _authState.value = _authState.value.copy(
            isAuthenticated = false,
            isLoading = false
        )
    }

    fun logout() {
        viewModelScope.launch {
            repository.clearAuthToken()
            _authState.value = AuthState(isAuthenticated = false)
        }
    }

    fun onAuthenticationSuccess() {
        _authState.value = _authState.value.copy(
            isAuthenticated = true,
            isLoading = false
        )
    }

    fun handleTokenExpiredDuringUsage() {
        viewModelScope.launch { 
            handleTokenExpiration() 
        }
    }

    fun refreshAuthenticationState() {
        checkAuthenticationStatus()
    }

    fun isCurrentlyAuthenticated(): Boolean {
        return _authState.value.isAuthenticated
    }

    private fun mapErrorToMessage(error: Throwable): String {
        return when (error) {
            is AppError.ValidationError -> error.message
            is AppError.ApiError -> ErrorMapper.mapAuthApiErrorToMessage(error)
            is AppError.NetworkError -> "Network error. Please check your connection and try again."
            is AppError.ServerError -> "Server is temporarily unavailable. Please try again later."
            is AppError.UnauthorizedError -> "Authentication failed. Please try again."
            else -> "An unexpected error occurred. Please try again."
        }
    }

    private fun handleAuthError(error: Throwable) {
        when (error) {
            is AppError.UnauthorizedError -> {
                // Handle token expiration
                viewModelScope.launch { 
                    handleTokenExpiration() 
                }
            }
            is AppError.NetworkError -> {
                // For network errors during auth check, assume authenticated (offline scenario)
                _authState.value = _authState.value.copy(
                    isAuthenticated = true,
                    isLoading = false
                )
            }
            else -> {
                // For other errors during auth operations, log but don't change auth state
                _authState.value = _authState.value.copy(isLoading = false)
            }
        }
    }
}

data class AuthState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false
)