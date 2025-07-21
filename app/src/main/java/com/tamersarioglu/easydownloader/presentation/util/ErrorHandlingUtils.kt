package com.tamersarioglu.easydownloader.presentation.util

import com.tamersarioglu.easydownloader.domain.model.AppError
import kotlinx.coroutines.delay
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * Utility object for comprehensive error handling across the application
 */
object ErrorHandlingUtils {

    /**
     * Maps network exceptions to domain AppError types
     */
    fun mapNetworkExceptionToAppError(exception: Throwable): AppError {
        return when (exception) {
            is UnknownHostException -> AppError.NetworkError
            is SocketTimeoutException -> AppError.NetworkError
            is IOException -> AppError.NetworkError
            is SSLException -> AppError.NetworkError
            is AppError -> exception
            else -> AppError.ServerError
        }
    }

    /**
     * Determines if an error is retryable
     */
    fun isRetryableError(error: Throwable): Boolean {
        return when (error) {
            is AppError.NetworkError -> true
            is AppError.ServerError -> true
            is AppError.ApiError -> {
                // Retry on server errors but not on client errors
                error.code in RETRYABLE_API_ERROR_CODES
            }
            is UnknownHostException -> true
            is SocketTimeoutException -> true
            is IOException -> true
            else -> false
        }
    }

    /**
     * Gets appropriate retry delay based on attempt number (exponential backoff)
     */
    fun getRetryDelay(attemptNumber: Int): Long {
        val baseDelay = 1000L // 1 second
        val maxDelay = 30000L // 30 seconds
        val delay = baseDelay * (1L shl (attemptNumber - 1)) // 2^(attempt-1) * baseDelay
        return minOf(delay, maxDelay)
    }

    /**
     * Executes a suspending operation with retry logic
     */
    suspend fun <T> executeWithRetry(
        maxAttempts: Int = 3,
        operation: suspend (attemptNumber: Int) -> Result<T>
    ): Result<T> {
        var lastException: Throwable? = null
        
        repeat(maxAttempts) { attempt ->
            try {
                val result = operation(attempt + 1)
                if (result.isSuccess) {
                    return result
                }
                
                val exception = result.exceptionOrNull()
                if (exception != null && isRetryableError(exception)) {
                    lastException = exception
                    if (attempt < maxAttempts - 1) {
                        delay(getRetryDelay(attempt + 1))
                    }
                } else {
                    return result
                }
            } catch (e: Exception) {
                lastException = e
                if (isRetryableError(e) && attempt < maxAttempts - 1) {
                    delay(getRetryDelay(attempt + 1))
                } else {
                    return Result.failure(mapNetworkExceptionToAppError(e))
                }
            }
        }
        
        return Result.failure(lastException ?: AppError.ServerError)
    }

    /**
     * Creates a user-friendly error message with action suggestions
     */
    fun createErrorMessageWithAction(error: Throwable): ErrorMessageWithAction {
        return when (error) {
            is AppError.NetworkError -> ErrorMessageWithAction(
                message = "Network connection failed. Please check your internet connection.",
                actionText = "Retry",
                canRetry = true
            )
            is AppError.ServerError -> ErrorMessageWithAction(
                message = "Server is temporarily unavailable. Please try again in a few moments.",
                actionText = "Retry",
                canRetry = true
            )
            is AppError.UnauthorizedError -> ErrorMessageWithAction(
                message = "Your session has expired. Please log in again.",
                actionText = "Login",
                canRetry = false
            )
            is AppError.ApiError -> {
                val message = ErrorMapper.mapErrorToMessage(error)
                ErrorMessageWithAction(
                    message = message,
                    actionText = if (isRetryableError(error)) "Retry" else "OK",
                    canRetry = isRetryableError(error)
                )
            }
            is AppError.ValidationError -> ErrorMessageWithAction(
                message = error.message,
                actionText = "OK",
                canRetry = false
            )
            else -> ErrorMessageWithAction(
                message = "An unexpected error occurred. Please try again.",
                actionText = "Retry",
                canRetry = true
            )
        }
    }

    /**
     * Determines error severity for UI presentation
     */
    fun getErrorSeverity(error: Throwable): ErrorSeverity {
        return when (error) {
            is AppError.UnauthorizedError -> ErrorSeverity.CRITICAL
            is AppError.ValidationError -> ErrorSeverity.WARNING
            is AppError.NetworkError -> ErrorSeverity.WARNING
            is AppError.ServerError -> ErrorSeverity.ERROR
            is AppError.ApiError -> {
                when (error.code) {
                    "RATE_LIMITED" -> ErrorSeverity.WARNING
                    "SERVICE_UNAVAILABLE" -> ErrorSeverity.ERROR
                    "FORBIDDEN" -> ErrorSeverity.CRITICAL
                    else -> ErrorSeverity.ERROR
                }
            }
            else -> ErrorSeverity.ERROR
        }
    }

    /**
     * Checks if the device has network connectivity
     */
    fun isNetworkError(error: Throwable): Boolean {
        return error is AppError.NetworkError ||
                error is UnknownHostException ||
                error is SocketTimeoutException ||
                error is IOException
    }

    /**
     * Creates a comprehensive error state from any throwable
     */
    fun createErrorState(
        error: Throwable,
        context: ErrorContext = ErrorContext.GENERAL
    ): ErrorState {
        return ErrorState.fromThrowable(error, context)
    }

    /**
     * Handles common error scenarios with predefined actions
     */
    fun handleCommonErrors(
        error: Throwable,
        onUnauthorized: () -> Unit = {},
        onNetworkError: () -> Unit = {},
        onServerError: () -> Unit = {},
        onValidationError: (AppError.ValidationError) -> Unit = {},
        onGenericError: (Throwable) -> Unit = {}
    ) {
        when (error) {
            is AppError.UnauthorizedError -> onUnauthorized()
            is AppError.NetworkError -> onNetworkError()
            is AppError.ServerError -> onServerError()
            is AppError.ValidationError -> onValidationError(error)
            else -> onGenericError(error)
        }
    }

    /**
     * Determines if an error should trigger automatic logout
     */
    fun shouldTriggerLogout(error: Throwable): Boolean {
        return when (error) {
            is AppError.UnauthorizedError -> true
            is AppError.ApiError -> error.code in LOGOUT_TRIGGERING_ERROR_CODES
            else -> false
        }
    }

    /**
     * Gets user-friendly error title based on error type
     */
    fun getErrorTitle(error: Throwable): String {
        return when (error) {
            is AppError.NetworkError -> "Connection Problem"
            is AppError.ServerError -> "Server Error"
            is AppError.UnauthorizedError -> "Authentication Required"
            is AppError.ValidationError -> "Invalid Input"
            is AppError.ApiError -> when (error.code) {
                "RATE_LIMITED" -> "Too Many Requests"
                "SERVICE_UNAVAILABLE" -> "Service Unavailable"
                "FORBIDDEN" -> "Access Denied"
                else -> "Error"
            }
            else -> "Unexpected Error"
        }
    }

    private val RETRYABLE_API_ERROR_CODES = setOf(
        "SERVICE_UNAVAILABLE",
        "RATE_LIMITED",
        "TIMEOUT",
        "INTERNAL_SERVER_ERROR"
    )

    private val LOGOUT_TRIGGERING_ERROR_CODES = setOf(
        "TOKEN_EXPIRED",
        "INVALID_TOKEN",
        "TOKEN_REVOKED",
        "UNAUTHORIZED",
        "FORBIDDEN"
    )
}

/**
 * Data class representing an error message with suggested action
 */
data class ErrorMessageWithAction(
    val message: String,
    val actionText: String,
    val canRetry: Boolean
)

/**
 * Enum representing error severity levels
 */
enum class ErrorSeverity {
    WARNING,    // Yellow/orange - user can continue
    ERROR,      // Red - operation failed but recoverable
    CRITICAL    // Red with emphasis - requires immediate attention
}