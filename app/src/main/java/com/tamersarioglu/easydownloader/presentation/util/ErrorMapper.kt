package com.tamersarioglu.easydownloader.presentation.util

import com.tamersarioglu.easydownloader.domain.model.AppError
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException


object ErrorMapper {

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

    private fun mapApiErrorToMessage(error: AppError.ApiError): String {
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

    fun mapViewModelError(
        error: Throwable,
        apiErrorMapper: (AppError.ApiError) -> String = ::mapApiErrorToMessage
    ): String {
        return when (error) {
            is AppError.ValidationError -> error.message
            is AppError.ApiError -> apiErrorMapper(error)
            is AppError.NetworkError -> "Network error. Please check your connection and try again."
            is AppError.ServerError -> "Server is temporarily unavailable. Please try again later."
            is AppError.UnauthorizedError -> "Authentication failed. Please try again."
            else -> "An unexpected error occurred. Please try again."
        }
    }

    fun handleValidationError(
        error: AppError.ValidationError,
        onUsernameError: (String) -> Unit,
        onPasswordError: (String) -> Unit,
        onGeneralError: (String) -> Unit
    ) {
        when (error.field) {
            "username" -> onUsernameError(error.message)
            "password" -> onPasswordError(error.message)
            else -> onGeneralError(error.message)
        }
    }

    /**
     * Maps network exceptions to user-friendly messages
     */
    fun mapNetworkExceptionToMessage(exception: Throwable): String {
        return when (exception) {
            is UnknownHostException -> "No internet connection. Please check your network settings."
            is SocketTimeoutException -> "Request timed out. Please check your connection and try again."
            is SSLException -> "Secure connection failed. Please try again."
            is IOException -> "Network error occurred. Please check your connection."
            else -> "Connection failed. Please try again."
        }
    }

    /**
     * Maps video-related API errors to user-friendly messages
     */
    fun mapVideoApiErrorToMessage(error: AppError.ApiError): String {
        return when (error.code) {
            "INVALID_URL" -> "The provided URL is not valid. Please check the URL format."
            "UNSUPPORTED_PLATFORM" -> "This video platform is not supported. Please use a supported platform."
            "VIDEO_UNAVAILABLE" -> "The video is unavailable, private, or has been removed."
            "VIDEO_TOO_LONG" -> "The video is too long to download. Maximum duration exceeded."
            "VIDEO_PROCESSING" -> "The video is still being processed. Please try again later."
            "DOWNLOAD_LIMIT_EXCEEDED" -> "You have reached your download limit. Please try again later."
            "QUOTA_EXCEEDED" -> "Daily download quota exceeded. Please try again tomorrow."
            "VIDEO_ALREADY_SUBMITTED" -> "This video has already been submitted for download."
            "INVALID_VIDEO_FORMAT" -> "The video format is not supported for download."
            "GEOBLOCKED" -> "This video is not available in your region."
            "COPYRIGHT_PROTECTED" -> "This video is copyright protected and cannot be downloaded."
            else -> error.message.ifEmpty { "Video processing failed. Please try again." }
        }
    }

    /**
     * Enhanced error mapping with context-specific handling
     */
    fun mapErrorWithContext(
        error: Throwable,
        context: ErrorContext = ErrorContext.GENERAL
    ): String {
        return when (error) {
            is AppError.ValidationError -> error.message
            is AppError.ApiError -> {
                when (context) {
                    ErrorContext.LOGIN -> mapLoginApiErrorToMessage(error)
                    ErrorContext.REGISTRATION -> mapRegistrationApiErrorToMessage(error)
                    ErrorContext.VIDEO_SUBMISSION -> mapVideoApiErrorToMessage(error)
                    ErrorContext.VIDEO_LIST -> mapApiErrorToMessage(error)
                    ErrorContext.AUTH -> mapAuthApiErrorToMessage(error)
                    ErrorContext.GENERAL -> mapApiErrorToMessage(error)
                }
            }
            is AppError.NetworkError -> "Network connection failed. Please check your internet connection and try again."
            is AppError.ServerError -> "Server is temporarily unavailable. Please try again in a few moments."
            is AppError.UnauthorizedError -> {
                when (context) {
                    ErrorContext.LOGIN -> "Invalid credentials. Please check your username and password."
                    else -> "Your session has expired. Please log in again."
                }
            }
            is UnknownHostException, is SocketTimeoutException, is IOException, is SSLException -> {
                mapNetworkExceptionToMessage(error)
            }
            else -> "An unexpected error occurred. Please try again."
        }
    }
}

/**
 * Enum representing different contexts where errors can occur
 */
enum class ErrorContext {
    GENERAL,
    LOGIN,
    REGISTRATION,
    VIDEO_SUBMISSION,
    VIDEO_LIST,
    AUTH
}