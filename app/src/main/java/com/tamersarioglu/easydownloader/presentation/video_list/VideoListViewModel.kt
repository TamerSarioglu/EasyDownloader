package com.tamersarioglu.easydownloader.presentation.video_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.domain.model.VideoItem
import com.tamersarioglu.easydownloader.domain.model.VideoStatus
import com.tamersarioglu.easydownloader.domain.usecase.GetUserVideosUseCase
import com.tamersarioglu.easydownloader.domain.usecase.GetVideoStatusUseCase
import com.tamersarioglu.easydownloader.presentation.util.ErrorMapper
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
            getUserVideosUseCase().fold(
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
        }
    }

    fun refreshVideos() {
        _uiState.value = _uiState.value.copy(
            isRefreshing = true,
            error = null
        )

        viewModelScope.launch {
            getUserVideosUseCase().fold(
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
        }
    }

    fun retryLoading() {
        clearError()
        loadVideos()
    }

    fun updateVideoStatus(videoId: String) {
        viewModelScope.launch {
            getVideoStatusUseCase(videoId).fold(
                onSuccess = { updatedVideoItem ->
                    val currentVideos = _uiState.value.videos.toMutableList()
                    val videoIndex = currentVideos.indexOfFirst { it.id == videoId }
                    
                    if (videoIndex != -1) {
                        currentVideos[videoIndex] = updatedVideoItem
                        _uiState.value = _uiState.value.copy(videos = currentVideos)
                    }
                },
                onFailure = { _ ->
                }
            )
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
        if (error is AppError.UnauthorizedError) {
            resetState()
        }
        
        val errorMessage = mapErrorToMessage(error)

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isRefreshing = false,
            error = errorMessage
        )
    }
    
    private fun mapErrorToMessage(error: Throwable): String {
        return when (error) {
            is AppError.ApiError -> {
                when (error.code) {
                    "NO_VIDEOS_FOUND" -> "You don't have any videos yet."
                    "VIDEOS_UNAVAILABLE" -> "Videos are temporarily unavailable. Please try again later."
                    else -> error.message.ifEmpty { "An error occurred while loading videos." }
                }
            }
            is AppError.UnauthorizedError -> "Authentication failed. Please login again."
            else -> {
                ErrorMapper.mapErrorToMessage(error)
            }
        }
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

    fun resetState() {
        _uiState.value = VideoListUiState()
    }
}