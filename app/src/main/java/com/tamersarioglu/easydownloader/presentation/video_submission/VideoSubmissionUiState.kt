package com.tamersarioglu.easydownloader.presentation.video_submission

/**
 * UI state for the video submission screen
 */
data class VideoSubmissionUiState(
    val isLoading: Boolean = false,
    val url: String = "",
    val isUrlValid: Boolean = false,
    val submissionResult: String? = null,
    val error: String? = null
)