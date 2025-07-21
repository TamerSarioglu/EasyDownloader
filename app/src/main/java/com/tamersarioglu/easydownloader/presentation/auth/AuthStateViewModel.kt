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
                        _authState.value = _authState.value.copy(
                            isAuthenticated = true,
                            isLoading = false
                        )
                    }
                    else -> {
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
            try {
                // Clear authentication tokens and user data
                repository.clearAuthToken()
                
                // Reset authentication state to initial values
                _authState.value = AuthState(isAuthenticated = false, isLoading = false)
            } catch (e: Exception) {
                // Even if clearing token fails, reset auth state
                _authState.value = AuthState(isAuthenticated = false, isLoading = false)
            }
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
        return ErrorMapper.mapViewModelError(
            error = error,
            apiErrorMapper = ErrorMapper::mapAuthApiErrorToMessage
        )
    }

    private fun handleAuthError(error: Throwable) {

        when (error) {
            is AppError.UnauthorizedError -> {
                viewModelScope.launch { 
                    handleTokenExpiration() 
                }
            }
            is AppError.NetworkError -> {
                _authState.value = _authState.value.copy(
                    isAuthenticated = true,
                    isLoading = false
                )
            }
            else -> {
                _authState.value = _authState.value.copy(isLoading = false)
            }
        }
    }
}

data class AuthState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false
)