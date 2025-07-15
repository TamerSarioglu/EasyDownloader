package com.tamersarioglu.easydownloader.data.repository

import com.tamersarioglu.easydownloader.data.local.TokenManager
import com.tamersarioglu.easydownloader.data.mapper.createLoginRequest
import com.tamersarioglu.easydownloader.data.mapper.createRegisterRequest
import com.tamersarioglu.easydownloader.data.mapper.createVideoSubmissionRequest
import com.tamersarioglu.easydownloader.data.mapper.toDomainModel
import com.tamersarioglu.easydownloader.data.remote.api.ApiErrorHandler
import com.tamersarioglu.easydownloader.data.remote.api.ApiException
import com.tamersarioglu.easydownloader.data.remote.api.ApiResult
import com.tamersarioglu.easydownloader.data.remote.api.VideoDownloaderApiService
import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.domain.model.User
import com.tamersarioglu.easydownloader.domain.model.VideoItem
import com.tamersarioglu.easydownloader.domain.repository.VideoDownloaderRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of VideoDownloaderRepository that handles all data operations
 * with proper error handling and token management.
 */
@Singleton
class VideoDownloaderRepositoryImpl @Inject constructor(
    private val apiService: VideoDownloaderApiService,
    private val tokenManager: TokenManager
) : VideoDownloaderRepository {

    override suspend fun register(username: String, password: String): Result<User> {
        return try {
            // Validate input parameters
            val request = createRegisterRequest(username, password)
            
            // Make API call with error handling
            val apiResult = ApiErrorHandler.safeApiCall {
                apiService.register(request)
            }
            
            when (apiResult) {
                is ApiResult.Success -> {
                    val user = apiResult.data.toDomainModel()
                    // Automatically store the JWT token on successful registration
                    saveAuthToken(user.token)
                    Result.success(user)
                }
                is ApiResult.Error -> {
                    Result.failure(mapApiExceptionToAppError(apiResult.exception))
                }
            }
        } catch (e: IllegalArgumentException) {
            // Handle validation errors from createRegisterRequest
            val field = when {
                e.message?.contains("Username") == true -> "username"
                e.message?.contains("Password") == true -> "password"
                else -> "input"
            }
            Result.failure(AppError.ValidationError(field, e.message ?: "Invalid input"))
        } catch (e: Exception) {
            Result.failure(AppError.NetworkError)
        }
    }

    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            // Validate input parameters
            val request = createLoginRequest(username, password)
            
            // Make API call with error handling
            val apiResult = ApiErrorHandler.safeApiCall {
                apiService.login(request)
            }
            
            when (apiResult) {
                is com.tamersarioglu.easydownloader.data.remote.api.ApiResult.Success -> {
                    val user = apiResult.data.toDomainModel()
                    // Automatically store the JWT token on successful login
                    saveAuthToken(user.token)
                    Result.success(user)
                }
                is com.tamersarioglu.easydownloader.data.remote.api.ApiResult.Error -> {
                    Result.failure(mapApiExceptionToAppError(apiResult.exception))
                }
            }
        } catch (e: IllegalArgumentException) {
            // Handle validation errors from createLoginRequest
            val field = when {
                e.message?.contains("Username") == true -> "username"
                e.message?.contains("Password") == true -> "password"
                else -> "input"
            }
            Result.failure(AppError.ValidationError(field, e.message ?: "Invalid input"))
        } catch (e: Exception) {
            Result.failure(AppError.NetworkError)
        }
    }

    override suspend fun submitVideo(url: String): Result<String> {
        return try {
            // Validate URL format
            val request = createVideoSubmissionRequest(url)
            
            // Make API call with error handling
            val apiResult = ApiErrorHandler.safeApiCall {
                apiService.submitVideo(request)
            }
            
            when (apiResult) {
                is com.tamersarioglu.easydownloader.data.remote.api.ApiResult.Success -> {
                    Result.success(apiResult.data.videoId)
                }
                is com.tamersarioglu.easydownloader.data.remote.api.ApiResult.Error -> {
                    Result.failure(mapApiExceptionToAppError(apiResult.exception))
                }
            }
        } catch (e: IllegalArgumentException) {
            // Handle validation errors from createVideoSubmissionRequest
            Result.failure(AppError.ValidationError("url", e.message ?: "Invalid URL"))
        } catch (e: Exception) {
            Result.failure(AppError.NetworkError)
        }
    }

    override suspend fun getUserVideos(): Result<List<VideoItem>> {
        return try {
            // Make API call with error handling
            val apiResult = ApiErrorHandler.safeApiCall {
                apiService.getUserVideos()
            }
            
            when (apiResult) {
                is ApiResult.Success -> {
                    val videos = apiResult.data.toDomainModel()
                    Result.success(videos)
                }
                is ApiResult.Error -> {
                    Result.failure(mapApiExceptionToAppError(apiResult.exception))
                }
            }
        } catch (e: Exception) {
            Result.failure(AppError.NetworkError)
        }
    }

    override suspend fun getVideoStatus(videoId: String): Result<VideoItem> {
        return try {
            // Validate video ID
            require(videoId.isNotBlank()) { "Video ID cannot be blank" }
            
            // Make API call with error handling
            val apiResult = ApiErrorHandler.safeApiCall {
                apiService.getVideoStatus(videoId)
            }
            
            when (apiResult) {
                is ApiResult.Success -> {
                    val videoItem = apiResult.data.toDomainModel()
                    Result.success(videoItem)
                }
                is ApiResult.Error -> {
                    Result.failure(mapApiExceptionToAppError(apiResult.exception))
                }
            }
        } catch (e: IllegalArgumentException) {
            Result.failure(AppError.ValidationError("videoId", e.message ?: "Invalid video ID"))
        } catch (e: Exception) {
            Result.failure(AppError.NetworkError)
        }
    }

    override suspend fun saveAuthToken(token: String) {
        tokenManager.saveAuthToken(token)
    }

    override suspend fun getAuthToken(): String? {
        return tokenManager.getAuthToken()
    }

    override suspend fun clearAuthToken() {
        tokenManager.clearAuthToken()
    }

    override suspend fun isAuthenticated(): Boolean {
        return tokenManager.isAuthenticated()
    }

    /**
     * Maps ApiException to AppError for consistent error handling across the app
     */
    private fun mapApiExceptionToAppError(exception: ApiException): AppError {
        return when (exception) {
            is ApiException.NetworkError -> AppError.NetworkError
            is ApiException.TimeoutError -> AppError.NetworkError
            is ApiException.UnauthorizedError -> AppError.UnauthorizedError
            is ApiException.ValidationError -> AppError.ValidationError(exception.field, exception.message)
            is ApiException.ServerError -> {
                if (exception.code?.startsWith("5") == true) {
                    AppError.ServerError
                } else {
                    AppError.ApiError(exception.code ?: "UNKNOWN", exception.message)
                }
            }
            is ApiException.HttpError -> {
                when (exception.code) {
                    401 -> AppError.UnauthorizedError
                    in 400..499 -> AppError.ApiError(exception.code.toString(), exception.errorBody ?: "Client error")
                    in 500..599 -> AppError.ServerError
                    else -> AppError.ApiError(exception.code.toString(), exception.errorBody ?: "HTTP error")
                }
            }
            is ApiException.UnknownError -> AppError.NetworkError
        }
    }
}