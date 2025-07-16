package com.tamersarioglu.easydownloader.presentation.video_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.domain.model.VideoItem
import com.tamersarioglu.easydownloader.domain.model.VideoStatus
import com.tamersarioglu.easydownloader.domain.usecase.GetUserVideosUseCase
import com.tamersarioglu.easydownloader.domain.usecase.GetVideoStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoListViewModel @Inject constructor(
    private val getUserVideosUseCase: GetUserVideosUseCase,
    private val getVideoStatusUseCase: GetVideoStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VideoListUiState())
    val uiState: StateFlow<VideoListUiState> = _uiState.asStateFlow()

    init {
        loadVideos()
    }

    fun loadVideos() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            try {
                val result = getUserVideosUseCase()

                result.fold(
                    onSuccess = { videos ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            videos = videos,
                            error = null
                        )
                    },
                    onFailure = { error ->
                        handleError(error)
                    }
                )
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun refreshVideos() {
        _uiState.value = _uiState.value.copy(
            isRefreshing = true,
            error = null
        )

        viewModelScope.launch {
            try {
                val result = getUserVideosUseCase()

                result.fold(
                    onSuccess = { videos ->
                        _uiState.value = _uiState.value.copy(
                            isRefreshing = false,
                            videos = videos,
                            error = null
                        )
                    },
                    onFailure = { error ->
                        handleError(error)
                    }
                )
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun retryLoading() {
        clearError()
        loadVideos()
    }

    fun updateVideoStatus(videoId: String) {
        viewModelScope.launch {
            try {
                val result = getVideoStatusUseCase(videoId)

                result.fold(
                    onSuccess = { updatedVideoItem ->
                        val currentVideos = _uiState.value.videos.toMutableList()
                        val videoIndex = currentVideos.indexOfFirst { it.id == videoId }

                        if (videoIndex != -1) {
                            currentVideos[videoIndex] = updatedVideoItem
                            _uiState.value = _uiState.value.copy(videos = currentVideos)
                        }
                    },
                    onFailure = { _ ->
                        // Silently handle individual video status update failures
                        // to avoid disrupting the overall UI experience
                    }
                )
            } catch (e: Exception) {
                // Silently handle exceptions for individual video status updates
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun getVideosByStatus(status: VideoStatus): List<VideoItem> {
        return _uiState.value.videos.filter { it.status == status }
    }

    fun hasPendingVideos(): Boolean {
        return _uiState.value.videos.any { it.status == VideoStatus.PENDING }
    }

    fun getVideoCountByStatus(status: VideoStatus): Int {
        return _uiState.value.videos.count { it.status == status }
    }

    private fun handleError(error: Throwable) {
        val errorMessage = when (error) {
            is AppError.NetworkError -> {
                "Network error. Please check your connection and try again."
            }
            is AppError.ServerError -> {
                "Server is temporarily unavailable. Please try again later."
            }
            is AppError.UnauthorizedError -> {
                "Authentication failed. Please login again."
            }
            is AppError.ApiError -> {
                error.message.ifEmpty { "An error occurred while loading videos." }
            }
            else -> {
                "An unexpected error occurred. Please try again."
            }
        }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isRefreshing = false,
            error = errorMessage
        )
    }

    fun isEmptyState(): Boolean {
        return _uiState.value.videos.isEmpty() &&
                !_uiState.value.isLoading &&
                !_uiState.value.isRefreshing &&
                _uiState.value.error == null
    }

    fun refreshPendingVideos() {
        val pendingVideos = getVideosByStatus(VideoStatus.PENDING)

        pendingVideos.forEach { video ->
            updateVideoStatus(video.id)
        }
    }
}