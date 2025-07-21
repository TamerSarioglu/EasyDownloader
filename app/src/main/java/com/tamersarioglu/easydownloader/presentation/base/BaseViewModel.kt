package com.tamersarioglu.easydownloader.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.presentation.util.ErrorContext
import com.tamersarioglu.easydownloader.presentation.util.ErrorHandlingUtils
import com.tamersarioglu.easydownloader.presentation.util.ErrorMapper
import com.tamersarioglu.easydownloader.presentation.util.RetryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel class that provides common error handling functionality
 */
abstract class BaseViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    protected val retryManager = RetryManager()

    /**
     * Executes a suspending operation with error handling
     */
    protected fun <T> executeOperation(
        operation: suspend () -> Result<T>,
        onSuccess: (T) -> Unit,
        onError: ((Throwable) -> Unit)? = null,
        errorContext: ErrorContext = ErrorContext.GENERAL,
        showLoading: Boolean = true
    ) {
        viewModelScope.launch {
            if (showLoading) {
                _isLoading.value = true
            }
            _error.value = null

            try {
                val result = operation()
                result.fold(
                    onSuccess = { data ->
                        onSuccess(data)
                        _error.value = null
                    },
                    onFailure = { throwable ->
                        handleError(throwable, errorContext)
                        onError?.invoke(throwable)
                    }
                )
            } catch (e: Exception) {
                val mappedError = ErrorHandlingUtils.mapNetworkExceptionToAppError(e)
                handleError(mappedError, errorContext)
                onError?.invoke(mappedError)
            } finally {
                if (showLoading) {
                    _isLoading.value = false
                }
            }
        }
    }

    /**
     * Executes an operation with retry logic
     */
    protected fun <T> executeWithRetry(
        operation: suspend () -> Result<T>,
        onSuccess: (T) -> Unit,
        onError: ((Throwable) -> Unit)? = null,
        errorContext: ErrorContext = ErrorContext.GENERAL,
        maxAttempts: Int = 3
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            // Create a new retry manager with specified max attempts
            val customRetryManager = RetryManager(maxAttempts = maxAttempts)
            
            val result = customRetryManager.executeWithRetry(
                operation = operation,
                onRetryAttempt = { attempt ->
                    // Optional: Update UI to show retry attempt
                    onRetryAttempt(attempt)
                }
            )

            result.fold(
                onSuccess = { data ->
                    onSuccess(data)
                    _error.value = null
                },
                onFailure = { throwable ->
                    handleError(throwable, errorContext)
                    onError?.invoke(throwable)
                }
            )

            _isLoading.value = false
        }
    }

    /**
     * Called when a retry attempt is made - can be overridden by subclasses
     */
    protected open fun onRetryAttempt(attempt: Int) {
        // Default implementation - can be overridden by subclasses
    }

    /**
     * Handles errors and updates the error state
     */
    protected open fun handleError(
        throwable: Throwable,
        context: ErrorContext = ErrorContext.GENERAL
    ) {
        val errorMessage = ErrorMapper.mapErrorWithContext(throwable, context)
        _error.value = errorMessage

        // Handle specific error types
        when (throwable) {
            is AppError.UnauthorizedError -> {
                handleUnauthorizedError()
            }
            is AppError.NetworkError -> {
                handleNetworkError()
            }
        }
    }

    /**
     * Clears the current error state
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Retries the last failed operation if possible
     */
    fun retry() {
        if (retryManager.canRetry()) {
            onRetryRequested()
        }
    }

    /**
     * Called when unauthorized error occurs - override in subclasses
     */
    protected open fun handleUnauthorizedError() {
        // Default implementation - can be overridden by subclasses
    }

    /**
     * Called when network error occurs - override in subclasses
     */
    protected open fun handleNetworkError() {
        // Default implementation - can be overridden by subclasses
    }

    /**
     * Called when retry is requested - override in subclasses
     */
    protected open fun onRetryRequested() {
        // Default implementation - can be overridden by subclasses
    }

    /**
     * Checks if the current error is retryable
     */
    fun canRetry(): Boolean {
        return retryManager.canRetry()
    }

    /**
     * Gets the current retry state
     */
    fun getRetryState() = retryManager.retryState

    /**
     * Resets the retry manager state
     */
    protected fun resetRetryState() {
        retryManager.reset()
    }
}