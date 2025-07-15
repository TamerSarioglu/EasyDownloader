package com.tamersarioglu.easydownloader.presentation.video_list

import com.tamersarioglu.easydownloader.domain.model.VideoItem

/**
 * UI state for the video list screen
 */
data class VideoListUiState(
    val isLoading: Boolean = false,
    val videos: List<VideoItem> = emptyList(),
    val error: String? = null,
    val isRefreshing: Boolean = false
)