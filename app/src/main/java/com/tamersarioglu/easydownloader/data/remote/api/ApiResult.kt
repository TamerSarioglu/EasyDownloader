package com.tamersarioglu.easydownloader.data.remote.api

/**
 * A generic wrapper for API responses that encapsulates success and error states
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val exception: ApiException) : ApiResult<Nothing>()
}

/**
 * Custom exception types for API errors
 */
sealed class ApiException(override val message: String, cause: Throwable? = null) : Exception(message, cause) {
    object NetworkError : ApiException("Network connection error")
    object TimeoutError : ApiException("Request timeout")
    data class HttpError(val code: Int, val errorBody: String?) : ApiException("HTTP $code error")
    data class ServerError(override val message: String, val code: String? = null) : ApiException(message)
    data class ValidationError(val field: String, override val message: String) : ApiException("Validation error: $message")
    object UnauthorizedError : ApiException("Unauthorized access")
    object UnknownError : ApiException("Unknown error occurred")
}

/**
 * Extension function to convert ApiResult to standard Result
 */
fun <T> ApiResult<T>.toResult(): Result<T> {
    return when (this) {
        is ApiResult.Success -> Result.success(data)
        is ApiResult.Error -> Result.failure(exception)
    }
}