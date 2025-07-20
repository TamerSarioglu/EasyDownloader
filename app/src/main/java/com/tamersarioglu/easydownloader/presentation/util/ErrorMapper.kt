package com.tamersarioglu.easydownloader.presentation.util

import com.tamersarioglu.easydownloader.domain.model.AppError

/**
 * Utility object for mapping domain errors to user-friendly messages
 */
object ErrorMapper {

    /**
     * Maps a Throwable to a user-friendly error message
     */
    fun mapErrorToMessage(error: Throwable): String {
        return when (error) {
            is AppError.ValidationError -> error.message
            is AppError.ApiError -> mapApiErrorToMessage(error)
            is AppError.NetworkError -> "Network error. Please check your connection and try again."
            is AppError.ServerError -> "Server is temporarily unavailable. Please try again later."
            is AppError.UnauthorizedError -> "Authentication failed. Please try again."
            else -> "An unexpected error occurred. Please try again."
        }
    }

    /**
     * Maps API error codes to user-friendly messages for login operations
     */
    fun mapLoginApiErrorToMessage(error: AppError.ApiError): String {
        return when (error.code) {
            "INVALID_CREDENTIALS" -> "Invalid username or password. Please try again."
            "USER_NOT_FOUND" -> "Account not found. Please check your username or register."
            "ACCOUNT_LOCKED" -> "Your account has been temporarily locked. Please try again later."
            "TOO_MANY_ATTEMPTS" -> "Too many login attempts. Please wait before trying again."
            "ACCOUNT_DISABLED" -> "Your account has been disabled. Please contact support."
            "LOGIN_REQUIRED" -> "Please log in to continue."
            "SESSION_EXPIRED" -> "Your session has expired. Please log in again."
            else -> error.message.ifEmpty { "Login failed. Please try again." }
        }
    }

    /**
     * Maps API error codes to user-friendly messages for registration operations
     */
    fun mapRegistrationApiErrorToMessage(error: AppError.ApiError): String {
        return when (error.code) {
            "USER_EXISTS" -> "Username already exists. Please choose a different username."
            "INVALID_USERNAME" -> "Username contains invalid characters. Please use only letters, numbers, and underscores."
            "USERNAME_TOO_SHORT" -> "Username must be at least 3 characters long."
            "USERNAME_TOO_LONG" -> "Username must be less than 50 characters long."
            "PASSWORD_TOO_WEAK" -> "Password must be at least 6 characters long and contain letters and numbers."
            "PASSWORD_TOO_SHORT" -> "Password must be at least 6 characters long."
            "PASSWORD_TOO_LONG" -> "Password must be less than 128 characters long."
            "REGISTRATION_DISABLED" -> "Registration is currently disabled. Please try again later."
            "EMAIL_REQUIRED" -> "Email address is required for registration."
            "INVALID_EMAIL" -> "Please enter a valid email address."
            else -> error.message.ifEmpty { "Registration failed. Please try again." }
        }
    }

    /**
     * Maps API error codes to user-friendly messages for authentication operations
     */
    fun mapAuthApiErrorToMessage(error: AppError.ApiError): String {
        return when (error.code) {
            "TOKEN_EXPIRED" -> "Your session has expired. Please log in again."
            "INVALID_TOKEN" -> "Invalid authentication token. Please log in again."
            "TOKEN_REVOKED" -> "Your session has been revoked. Please log in again."
            "TOKEN_MALFORMED" -> "Authentication token is invalid. Please log in again."
            "REFRESH_TOKEN_EXPIRED" -> "Your session has expired. Please log in again."
            "UNAUTHORIZED" -> "You are not authorized to perform this action."
            "FORBIDDEN" -> "Access denied. You don't have permission to perform this action."
            "AUTH_REQUIRED" -> "Authentication is required. Please log in."
            else -> error.message.ifEmpty { "Authentication error. Please log in again." }
        }
    }

    /**
     * Generic API error mapping that falls back to specific mappers based on context
     */
    private fun mapApiErrorToMessage(error: AppError.ApiError): String {
        // Default generic mapping - specific contexts should use their own mappers
        return when (error.code) {
            "INVALID_CREDENTIALS" -> "Invalid username or password. Please try again."
            "USER_NOT_FOUND" -> "Account not found. Please check your username or register."
            "USER_EXISTS" -> "Username already exists. Please choose a different username."
            "TOKEN_EXPIRED" -> "Your session has expired. Please log in again."
            "INVALID_TOKEN" -> "Invalid authentication token. Please log in again."
            "UNAUTHORIZED" -> "You are not authorized to perform this action."
            "FORBIDDEN" -> "Access denied. You don't have permission to perform this action."
            "BAD_REQUEST" -> "Invalid request. Please check your input and try again."
            "NOT_FOUND" -> "The requested resource was not found."
            "CONFLICT" -> "A conflict occurred. Please try again."
            "RATE_LIMITED" -> "Too many requests. Please wait before trying again."
            "SERVICE_UNAVAILABLE" -> "Service is temporarily unavailable. Please try again later."
            else -> error.message.ifEmpty { "An error occurred. Please try again." }
        }
    }
}