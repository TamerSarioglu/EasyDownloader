package com.tamersarioglu.easydownloader.presentation.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamersarioglu.easydownloader.domain.model.AppError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Extension functions for ViewModels to handle errors consistently
 */

/**
 * Executes a suspending operation with comprehensive error handling
 */
fun <T> ViewModel.executeWithErrorHandling(
    loadingState: MutableStateFlow<Boolean>? = null,
    errorState: MutableStateFlow<String?>? = null,
    operation: suspend () -> Result<T>,
    onSuccess: (T) -> Unit,
    onError: ((Throwable) -> Unit)? = null,
    errorContext: ErrorContext = ErrorContext.GENERAL,
    showLoading: Boolean = true
) {
    viewModelScope.launch {
        if (showLoading) {
            loadingState?.value = true
        }
        errorState?.value = null

        try {
            val result = operation()
            result.fold(
                onSuccess = { data ->
                    onSuccess(data)
                    errorState?.value = null
                },
                onFailure = { throwable ->
                    val errorMessage = ErrorMapper.mapErrorWithContext(throwable, errorContext)
                    errorState?.value = errorMessage
                    onError?.invoke(throwable)
                }
            )
        } catch (e: Exception) {
            val mappedError = ErrorHandlingUtils.mapNetworkExceptionToAppError(e)
            val errorMessage = ErrorMapper.mapErrorWithContext(mappedError, errorContext)
            errorState?.value = errorMessage
            onError?.invoke(mappedError)
        } finally {
            if (showLoading) {
                loadingState?.value = false
            }
        }
    }
}

/**
 * Executes an operation with retry logic and error handling
 */
fun <T> ViewModel.executeWithRetryAndErrorHandling(
    loadingState: MutableStateFlow<Boolean>? = null,
    errorState: MutableStateFlow<String?>? = null,
    retryManager: RetryManager,
    operation: suspend () -> Result<T>,
    onSuccess: (T) -> Unit,
    onError: ((Throwable) -> Unit)? = null,
    errorContext: ErrorContext = ErrorContext.GENERAL,
    onRetryAttempt: ((Int) -> Unit)? = null
) {
    viewModelScope.launch {
        loadingState?.value = true
        errorState?.value = null

        val result = retryManager.executeWithRetry(
            operation = operation,
            onRetryAttempt = onRetryAttempt
        )

        result.fold(
            onSuccess = { data ->
                onSuccess(data)
                errorState?.value = null
            },
            onFailure = { throwable ->
                val errorMessage = ErrorMapper.mapErrorWithContext(throwable, errorContext)
                errorState?.value = errorMessage
                onError?.invoke(throwable)
            }
        )

        loadingState?.value = false
    }
}

/**
 * Handles specific error types with custom actions
 */
fun ViewModel.handleSpecificErrors(
    error: Throwable,
    onUnauthorized: (() -> Unit)? = null,
    onNetworkError: (() -> Unit)? = null,
    onValidationError: ((AppError.ValidationError) -> Unit)? = null,
    onApiError: ((AppError.ApiError) -> Unit)? = null,
    onGenericError: ((Throwable) -> Unit)? = null
) {
    when (error) {
        is AppError.UnauthorizedError -> onUnauthorized?.invoke()
        is AppError.NetworkError -> onNetworkError?.invoke()
        is AppError.ValidationError -> onValidationError?.invoke(error)
        is AppError.ApiError -> onApiError?.invoke(error)
        else -> onGenericError?.invoke(error)
    }
}

/**
 * Creates a standardized error state holder for ViewModels
 */
data class ErrorState(
    val hasError: Boolean = false,
    val message: String? = null,
    val canRetry: Boolean = false,
    val severity: ErrorSeverity = ErrorSeverity.ERROR,
    val context: ErrorContext = ErrorContext.GENERAL
) {
    companion object {
        fun fromThrowable(
            error: Throwable,
            context: ErrorContext = ErrorContext.GENERAL
        ): ErrorState {
            val message = ErrorMapper.mapErrorWithContext(error, context)
            val canRetry = ErrorHandlingUtils.isRetryableError(error)
            val severity = ErrorHandlingUtils.getErrorSeverity(error)
            
            return ErrorState(
                hasError = true,
                message = message,
                canRetry = canRetry,
                severity = severity,
                context = context
            )
        }
        
        fun cleared(): ErrorState = ErrorState()
    }
}

/**
 * Extension to create error state flow for ViewModels
 */
fun ViewModel.createErrorStateFlow(): MutableStateFlow<ErrorState> {
    return MutableStateFlow(ErrorState())
}

/**
 * Extension to update error state from throwable
 */
fun MutableStateFlow<ErrorState>.updateFromError(
    error: Throwable,
    context: ErrorContext = ErrorContext.GENERAL
) {
    value = ErrorState.fromThrowable(error, context)
}

/**
 * Extension to clear error state
 */
fun MutableStateFlow<ErrorState>.clearError() {
    value = ErrorState.cleared()
}

/**
 * Executes operation with automatic error state management
 */
fun <T> ViewModel.executeWithErrorState(
    errorStateFlow: MutableStateFlow<ErrorState>,
    loadingState: MutableStateFlow<Boolean>? = null,
    operation: suspend () -> Result<T>,
    onSuccess: (T) -> Unit,
    errorContext: ErrorContext = ErrorContext.GENERAL,
    showLoading: Boolean = true
) {
    viewModelScope.launch {
        if (showLoading) {
            loadingState?.value = true
        }
        errorStateFlow.clearError()

        try {
            val result = operation()
            result.fold(
                onSuccess = { data ->
                    onSuccess(data)
                    errorStateFlow.clearError()
                },
                onFailure = { throwable ->
                    errorStateFlow.updateFromError(throwable, errorContext)
                }
            )
        } catch (e: Exception) {
            val mappedError = ErrorHandlingUtils.mapNetworkExceptionToAppError(e)
            errorStateFlow.updateFromError(mappedError, errorContext)
        } finally {
            if (showLoading) {
                loadingState?.value = false
            }
        }
    }
}

/**
 * Comprehensive error handling function that combines all error handling utilities
 */
fun <T> ViewModel.executeWithComprehensiveErrorHandling(
    loadingState: MutableStateFlow<Boolean>? = null,
    errorStateFlow: MutableStateFlow<ErrorState>? = null,
    retryManager: RetryManager? = null,
    operation: suspend () -> Result<T>,
    onSuccess: (T) -> Unit,
    onError: ((Throwable) -> Unit)? = null,
    errorContext: ErrorContext = ErrorContext.GENERAL,
    maxRetries: Int = 3,
    showLoading: Boolean = true,
    handleCommonErrors: Boolean = true
) {
    viewModelScope.launch {
        if (showLoading) {
            loadingState?.value = true
        }
        errorStateFlow?.clearError()

        try {
            val result = if (retryManager != null) {
                retryManager.executeWithRetry(operation)
            } else {
                ErrorHandlingUtils.executeWithRetry(maxRetries, { operation() })
            }

            result.fold(
                onSuccess = { data ->
                    onSuccess(data)
                    errorStateFlow?.clearError()
                },
                onFailure = { throwable ->
                    val errorMessage = ErrorMapper.mapErrorWithContext(throwable, errorContext)
                    errorStateFlow?.updateFromError(throwable, errorContext)
                    
                    if (handleCommonErrors) {
                        ErrorHandlingUtils.handleCommonErrors(
                            error = throwable,
                            onUnauthorized = { 
                                // Handle unauthorized - could trigger logout
                            },
                            onNetworkError = {
                                // Handle network error - could show offline indicator
                            },
                            onServerError = {
                                // Handle server error - could show retry option
                            },
                            onValidationError = { validationError ->
                                // Handle validation error - could focus on specific field
                            }
                        )
                    }
                    
                    onError?.invoke(throwable)
                }
            )
        } catch (e: Exception) {
            val mappedError = ErrorHandlingUtils.mapNetworkExceptionToAppError(e)
            val errorMessage = ErrorMapper.mapErrorWithContext(mappedError, errorContext)
            errorStateFlow?.updateFromError(mappedError, errorContext)
            onError?.invoke(mappedError)
        } finally {
            if (showLoading) {
                loadingState?.value = false
            }
        }
    }
}