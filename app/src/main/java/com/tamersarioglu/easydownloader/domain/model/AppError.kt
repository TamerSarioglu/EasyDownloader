package com.tamersarioglu.easydownloader.domain.model


sealed class AppError : Exception() {
    data object NetworkError : AppError()
    data object ServerError : AppError()
    data class ApiError(val code: String, override val message: String) : AppError()
    data object UnauthorizedError : AppError()
    data class ValidationError(val field: String, override val message: String) : AppError()
}