package com.tamersarioglu.easydownloader.data.mapper

import com.tamersarioglu.easydownloader.data.remote.dto.*
import com.tamersarioglu.easydownloader.domain.model.*

fun AuthResponse.toDomainModel(): User {
    return User(
        username = username,
        token = token
    )
}

fun VideoSummaryDto.toDomainModel(): VideoItem {
    return VideoItem(
        id = id,
        originalUrl = originalUrl,
        status = status.toVideoStatus(),
        createdAt = createdAt,
        errorMessage = errorMessage
    )
}

fun VideoSubmissionResponse.toDomainModel(): VideoItem {
    return VideoItem(
        id = videoId,
        originalUrl = "",
        status = status.toVideoStatus(),
        createdAt = "",
        errorMessage = null
    )
}

fun VideoStatusResponse.toDomainModel(): VideoItem {
    return VideoItem(
        id = videoId,
        originalUrl = "",
        status = status.toVideoStatus(),
        createdAt = "",
        errorMessage = errorMessage
    )
}

fun ErrorResponse.toDomainModel(): AppError {
    return when {
        code == "UNAUTHORIZED" || code == "401" -> AppError.UnauthorizedError
        code?.startsWith("4") == true -> AppError.ApiError(code, message)
        code?.startsWith("5") == true -> AppError.ServerError
        else -> AppError.ApiError(code ?: "UNKNOWN", message)
    }
}

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

fun createLoginRequest(username: String, password: String): LoginRequest {
    require(username.isNotBlank()) { "Username cannot be blank" }
    require(password.isNotBlank()) { "Password cannot be blank" }
    
    return LoginRequest(
        username = username.trim(),
        password = password
    )
}

fun createVideoSubmissionRequest(url: String): VideoSubmissionRequest {
    require(url.isNotBlank()) { "URL cannot be blank" }
    require(isValidUrl(url)) { "Invalid URL format" }
    
    return VideoSubmissionRequest(
        url = url.trim()
    )
}

private fun String.toVideoStatus(): VideoStatus {
    return when (this.uppercase()) {
        "PENDING" -> VideoStatus.PENDING
        "COMPLETE", "COMPLETED" -> VideoStatus.COMPLETE
        "FAILED", "ERROR" -> VideoStatus.FAILED
        else -> VideoStatus.PENDING
    }
}

private fun isValidUrl(url: String): Boolean {
    return try {
        val trimmedUrl = url.trim()
        trimmedUrl.startsWith("http://") || trimmedUrl.startsWith("https://")
    } catch (e: Exception) {
        false
    }
}

fun List<VideoSummaryDto>.toDomainModel(): List<VideoItem> {
    return this.map { it.toDomainModel() }
}

fun VideoListResponse.toDomainModel(): List<VideoItem> {
    return videos.toDomainModel()
}