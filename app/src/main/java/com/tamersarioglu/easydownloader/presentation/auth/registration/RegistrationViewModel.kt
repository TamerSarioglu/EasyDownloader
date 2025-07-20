package com.tamersarioglu.easydownloader.presentation.auth.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.domain.usecase.RegisterUseCase
import com.tamersarioglu.easydownloader.presentation.util.ErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(RegistrationFormState())
    val formState: StateFlow<RegistrationFormState> = _formState.asStateFlow()

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

    fun register(onSuccess: () -> Unit = {}) {
        val currentForm = _formState.value

        clearErrors()

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            registerUseCase(
                username = currentForm.username,
                password = currentForm.password
            ).fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRegistrationSuccessful = true,
                        username = user.username,
                        error = null
                    )

                    _formState.value = RegistrationFormState()
                    onSuccess()
                },
                onFailure = { error -> 
                    handleRegistrationError(error)
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetSuccessState() {
        _uiState.value = _uiState.value.copy(isRegistrationSuccessful = false)
    }

    private fun clearErrors() {
        _formState.value = _formState.value.copy(usernameError = null, passwordError = null)
    }

    private fun handleRegistrationError(error: Throwable) {
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
        ErrorMapper.handleValidationError(
            error = error,
            onUsernameError = { message ->
                _formState.value = _formState.value.copy(usernameError = message)
            },
            onPasswordError = { message ->
                _formState.value = _formState.value.copy(passwordError = message)
            },
            onGeneralError = { message ->
                _uiState.value = _uiState.value.copy(error = message)
            }
        )
    }

    /**
     * Maps domain errors to user-friendly messages for registration operations
     */
    private fun mapErrorToMessage(error: Throwable): String {
        return ErrorMapper.mapViewModelError(
            error = error,
            apiErrorMapper = ErrorMapper::mapRegistrationApiErrorToMessage
        )
    }

    fun isFormValid(): Boolean {
        val form = _formState.value
        return form.username.trim().length >= 3 &&
                form.password.length >= 6 &&
                form.usernameError == null &&
                form.passwordError == null
    }

    fun resetState() {
        _uiState.value = RegistrationUiState()
        _formState.value = RegistrationFormState()
    }
}

data class RegistrationUiState(
    val isLoading: Boolean = false,
    val isRegistrationSuccessful: Boolean = false,
    val username: String = "",
    val error: String? = null
)

data class RegistrationFormState(
    val username: String = "",
    val password: String = "",
    val usernameError: String? = null,
    val passwordError: String? = null
)