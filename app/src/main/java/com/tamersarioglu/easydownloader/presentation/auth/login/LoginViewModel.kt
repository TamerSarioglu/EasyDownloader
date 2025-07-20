package com.tamersarioglu.easydownloader.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.domain.usecase.LoginUseCase
import com.tamersarioglu.easydownloader.presentation.util.ErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(LoginFormState())
    val formState: StateFlow<LoginFormState> = _formState.asStateFlow()

    fun updateUsername(username: String) {
        _formState.value = _formState.value.copy(
            username = username,
            usernameError = null
        )
    }

    fun updatePassword(password: String) {
        _formState.value = _formState.value.copy(
            password = password,
            passwordError = null
        )
    }

    fun login(onSuccess: () -> Unit = {}) {
        val currentForm = _formState.value

        clearErrors()

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            loginUseCase(
                username = currentForm.username,
                password = currentForm.password
            ).fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccessful = true,
                        username = user.username,
                        error = null
                    )

                    _formState.value = LoginFormState()
                    onSuccess()
                },
                onFailure = { error -> 
                    handleLoginError(error)
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetSuccessState() {
        _uiState.value = _uiState.value.copy(isLoginSuccessful = false)
    }

    private fun clearErrors() {
        _formState.value = _formState.value.copy(usernameError = null, passwordError = null)
    }

    private fun handleLoginError(error: Throwable) {
        _uiState.value = _uiState.value.copy(isLoading = false)

        when (error) {
            is AppError.ValidationError -> {
                handleValidationError(error)
            }
            else -> {
                _uiState.value = _uiState.value.copy(error = mapErrorToMessage(error))
            }
        }
    }

    private fun handleValidationError(error: AppError.ValidationError) {
        when (error.field) {
            "username" -> {
                _formState.value = _formState.value.copy(usernameError = error.message)
            }
            "password" -> {
                _formState.value = _formState.value.copy(passwordError = error.message)
            }
            else -> {
                // For validation errors without specific field, show as general error
                _uiState.value = _uiState.value.copy(error = error.message)
            }
        }
    }

    private fun mapErrorToMessage(error: Throwable): String {
        return when (error) {
            is AppError.ValidationError -> error.message
            is AppError.ApiError -> ErrorMapper.mapLoginApiErrorToMessage(error)
            is AppError.NetworkError -> "Network error. Please check your connection and try again."
            is AppError.ServerError -> "Server is temporarily unavailable. Please try again later."
            is AppError.UnauthorizedError -> "Authentication failed. Please try again."
            else -> "An unexpected error occurred. Please try again."
        }
    }

    fun isFormValid(): Boolean {
        val form = _formState.value
        return form.username.trim().isNotEmpty() &&
                form.password.isNotEmpty() &&
                form.usernameError == null &&
                form.passwordError == null
    }

    fun resetState() {
        _uiState.value = LoginUiState()
        _formState.value = LoginFormState()
    }
}