package com.tamersarioglu.easydownloader.data.mapper

import com.tamersarioglu.easydownloader.data.remote.dto.*
import com.tamersarioglu.easydownloader.domain.model.*

/**
 * Extension functions to map between network DTOs and domain models
 */

// DTO to Domain Model Mappers

/**
 * Maps AuthResponse DTO to User domain model
 */
fun AuthResponse.toDomainModel(): User {
    return User(
        username = username,
        token = token
    )
}

/**
 * Maps VideoSummaryDto to VideoItem domain model
 */
fun VideoSummaryDto.toDomainModel(): VideoItem {
    return VideoItem(
        id = id,
        originalUrl = originalUrl,
        status = status.toVideoStatus(),
        createdAt = createdAt,
        errorMessage = errorMessage
    )
}

/**
 * Maps VideoSubmissionResponse to VideoItem domain model
 */
fun VideoSubmissionResponse.toDomainModel(): VideoItem {
    return VideoItem(
        id = videoId,
        originalUrl = "", // URL not provided in response, will be set by caller
        status = status.toVideoStatus(),
        createdAt = "", // Creation time not provided in response
        errorMessage = null
    )
}

/**
 * Maps VideoStatusResponse to VideoItem domain model
 */
fun VideoStatusResponse.toDomainModel(): VideoItem {
    return VideoItem(
        id = videoId,
        originalUrl = "", // URL not provided in response, will be set by caller
        status = status.toVideoStatus(),
        createdAt = "", // Creation time not provided in response
        errorMessage = errorMessage
    )
}

/**
 * Maps ErrorResponse to AppError domain model
 */
fun ErrorResponse.toDomainModel(): AppError {
    return when {
        code == "UNAUTHORIZED" || code == "401" -> AppError.UnauthorizedError
        code?.startsWith("4") == true -> AppError.ApiError(code, message)
        code?.startsWith("5") == true -> AppError.ServerError
        else -> AppError.ApiError(code ?: "UNKNOWN", message)
    }
}

/**
 * Maps ValidationErrorResponse to AppError domain model
 */
fun ValidationErrorResponse.toDomainModel(): AppError {
    val firstValidationError = validationErrors?.entries?.firstOrNull()
    return if (firstValidationError != null) {
        val field = firstValidationError.key
        val errorMessage = firstValidationError.value.firstOrNull() ?: message
        AppError.ValidationError(field, errorMessage)
    } else {
        AppError.ApiError("VALIDATION_ERROR", message)
    }
}

// Domain Model to DTO Mappers (for requests)

/**
 * Creates RegisterRequest from username and password
 */
fun createRegisterRequest(username: String, password: String): RegisterRequest {
    require(username.isNotBlank()) { "Username cannot be blank" }
    require(password.isNotBlank()) { "Password cannot be blank" }
    require(username.length >= 3) { "Username must be at least 3 characters long" }
    require(password.length >= 6) { "Password must be at least 6 characters long" }
    
    return RegisterRequest(
        username = username.trim(),
        password = password
    )
}

/**
 * Creates LoginRequest from username and password
 */
fun createLoginRequest(username: String, password: String): LoginRequest {
    require(username.isNotBlank()) { "Username cannot be blank" }
    require(password.isNotBlank()) { "Password cannot be blank" }
    
    return LoginRequest(
        username = username.trim(),
        password = password
    )
}

/**
 * Creates VideoSubmissionRequest from URL
 */
fun createVideoSubmissionRequest(url: String): VideoSubmissionRequest {
    require(url.isNotBlank()) { "URL cannot be blank" }
    require(isValidUrl(url)) { "Invalid URL format" }
    
    return VideoSubmissionRequest(
        url = url.trim()
    )
}

// Helper Functions

/**
 * Converts string status to VideoStatus enum with null safety
 */
private fun String.toVideoStatus(): VideoStatus {
    return when (this.uppercase()) {
        "PENDING" -> VideoStatus.PENDING
        "COMPLETE", "COMPLETED" -> VideoStatus.COMPLETE
        "FAILED", "ERROR" -> VideoStatus.FAILED
        else -> VideoStatus.PENDING // Default to PENDING for unknown statuses
    }
}

/**
 * Basic URL validation
 */
private fun isValidUrl(url: String): Boolean {
    return try {
        val trimmedUrl = url.trim()
        trimmedUrl.startsWith("http://") || trimmedUrl.startsWith("https://")
    } catch (e: Exception) {
        false
    }
}

// List Mappers

/**
 * Maps list of VideoSummaryDto to list of VideoItem domain models
 */
fun List<VideoSummaryDto>.toDomainModel(): List<VideoItem> {
    return this.map { it.toDomainModel() }
}

/**
 * Maps VideoListResponse to list of VideoItem domain models
 */
fun VideoListResponse.toDomainModel(): List<VideoItem> {
    return videos.toDomainModel()
}