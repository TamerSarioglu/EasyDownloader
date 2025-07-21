package com.tamersarioglu.easydownloader.presentation.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Represents the current state of retry operations
 */
data class RetryState(
    val isRetrying: Boolean = false,
    val currentAttempt: Int = 0,
    val lastSuccessful: Boolean = false,
    val lastError: Throwable? = null,
    val exhaustedRetries: Boolean = false
)

/**
 * Manages retry logic for operations with exponential backoff and state tracking
 */
class RetryManager(
    private val maxAttempts: Int = 3,
    private val baseDelayMs: Long = 1000L
) {
    private val _retryState = MutableStateFlow(RetryState())
    val retryState: StateFlow<RetryState> = _retryState.asStateFlow()

    /**
     * Executes an operation with retry logic
     */
    suspend fun <T> executeWithRetry(
        operation: suspend () -> Result<T>,
        onRetryAttempt: ((Int) -> Unit)? = null
    ): Result<T> {
        _retryState.value = RetryState(isRetrying = true, currentAttempt = 1)
        
        repeat(maxAttempts) { attempt ->
            try {
                val result = operation()
                
                if (result.isSuccess) {
                    _retryState.value = RetryState(isRetrying = false, lastSuccessful = true)
                    return result
                }
                
                val exception = result.exceptionOrNull()
                if (exception != null && ErrorHandlingUtils.isRetryableError(exception)) {
                    if (attempt < maxAttempts - 1) {
                        onRetryAttempt?.invoke(attempt + 1)
                        _retryState.value = _retryState.value.copy(
                            currentAttempt = attempt + 2,
                            lastError = exception
                        )
                        delay(ErrorHandlingUtils.getRetryDelay(attempt + 1))
                    } else {
                        _retryState.value = RetryState(
                            isRetrying = false,
                            lastSuccessful = false,
                            lastError = exception,
                            exhaustedRetries = true
                        )
                        return result
                    }
                } else {
                    _retryState.value = RetryState(
                        isRetrying = false,
                        lastSuccessful = false,
                        lastError = exception
                    )
                    return result
                }
            } catch (e: Exception) {
                val mappedError = ErrorHandlingUtils.mapNetworkExceptionToAppError(e)
                
                if (ErrorHandlingUtils.isRetryableError(mappedError) && attempt < maxAttempts - 1) {
                    onRetryAttempt?.invoke(attempt + 1)
                    _retryState.value = _retryState.value.copy(
                        currentAttempt = attempt + 2,
                        lastError = mappedError
                    )
                    delay(ErrorHandlingUtils.getRetryDelay(attempt + 1))
                } else {
                    _retryState.value = RetryState(
                        isRetrying = false,
                        lastSuccessful = false,
                        lastError = mappedError,
                        exhaustedRetries = attempt == maxAttempts - 1
                    )
                    return Result.failure(mappedError)
                }
            }
        }
        
        val lastError = _retryState.value.lastError ?: Exception("Unknown error")
        _retryState.value = RetryState(
            isRetrying = false,
            lastSuccessful = false,
            lastError = lastError,
            exhaustedRetries = true
        )
        return Result.failure(lastError)
    }

    /**
     * Resets the retry state
     */
    fun reset() {
        _retryState.value = RetryState()
    }

    /**
     * Checks if the last operation can be retried
     */
    fun canRetry(): Boolean {
        val state = _retryState.value
        return !state.isRetrying && 
               state.lastError != null && 
               ErrorHandlingUtils.isRetryableError(state.lastError) &&
               !state.exhaustedRetries
    }
}

/**
 * Extension function to create a RetryManager for ViewModels
 */
fun createRetryManager(maxAttempts: Int = 3): RetryManager {
    return RetryManager(maxAttempts = maxAttempts)
}