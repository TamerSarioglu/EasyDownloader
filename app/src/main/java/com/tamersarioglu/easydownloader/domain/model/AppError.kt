package com.tamersarioglu.easydownloader.domain.model

/**
 * Sealed class representing different types of errors that can occur in the app
 */
sealed class AppError : Exception() {
    object NetworkError : AppError()
    object ServerError : AppError()
    data class ApiError(val code: String, override val message: String) : AppError()
    object UnauthorizedError : AppError()
    data class ValidationError(val field: String, override val message: String) : AppError()
}