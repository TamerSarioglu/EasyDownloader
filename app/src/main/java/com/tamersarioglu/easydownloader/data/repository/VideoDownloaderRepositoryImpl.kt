package com.tamersarioglu.easydownloader.data.repository

import com.tamersarioglu.easydownloader.data.local.TokenManager
import com.tamersarioglu.easydownloader.data.mapper.createLoginRequest
import com.tamersarioglu.easydownloader.data.mapper.createRegisterRequest
import com.tamersarioglu.easydownloader.data.mapper.createVideoSubmissionRequest
import com.tamersarioglu.easydownloader.data.mapper.toDomainModel
import com.tamersarioglu.easydownloader.data.remote.api.ApiErrorHandler
import com.tamersarioglu.easydownloader.data.remote.api.ApiErrorHandler.safeApiCall
import com.tamersarioglu.easydownloader.data.remote.api.ApiException
import com.tamersarioglu.easydownloader.data.remote.api.ApiResult
import com.tamersarioglu.easydownloader.data.remote.api.VideoDownloaderApiService
import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.domain.model.User
import com.tamersarioglu.easydownloader.domain.model.VideoItem
import com.tamersarioglu.easydownloader.domain.repository.VideoDownloaderRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoDownloaderRepositoryImpl @Inject constructor(
    private val apiService: VideoDownloaderApiService,
    private val tokenManager: TokenManager
) : VideoDownloaderRepository {

    override suspend fun register(username: String, password: String): Result<User> {
        return try {
            val request = createRegisterRequest(username, password)
            
            val apiResult = safeApiCall {
                apiService.register(request)
            }
            
            when (apiResult) {
                is ApiResult.Success -> {
                    val user = apiResult.data.toDomainModel()
                    saveAuthToken(user.token)
                    Result.success(user)
                }
                is ApiResult.Error -> {
                    Result.failure(mapApiExceptionToAppError(apiResult.exception))
                }
            }
        } catch (e: IllegalArgumentException) {
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
            val request = createLoginRequest(username, password)
            
            val apiResult = safeApiCall {
                apiService.login(request)
            }
            
            when (apiResult) {
                is ApiResult.Success -> {
                    val user = apiResult.data.toDomainModel()
                    saveAuthToken(user.token)
                    Result.success(user)
                }
                is ApiResult.Error -> {
                    Result.failure(mapApiExceptionToAppError(apiResult.exception))
                }
            }
        } catch (e: IllegalArgumentException) {
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
            val request = createVideoSubmissionRequest(url)
            
            val apiResult = safeApiCall {
                apiService.submitVideo(request)
            }
            
            when (apiResult) {
                is ApiResult.Success -> {
                    Result.success(apiResult.data.videoId)
                }
                is ApiResult.Error -> {
                    Result.failure(mapApiExceptionToAppError(apiResult.exception))
                }
            }
        } catch (e: IllegalArgumentException) {
            Result.failure(AppError.ValidationError("url", e.message ?: "Invalid URL"))
        } catch (e: Exception) {
            Result.failure(AppError.NetworkError)
        }
    }

    override suspend fun getUserVideos(): Result<List<VideoItem>> {
        return try {
            val apiResult = safeApiCall {
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
            require(videoId.isNotBlank()) { "Video ID cannot be blank" }
            
            val apiResult = safeApiCall {
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