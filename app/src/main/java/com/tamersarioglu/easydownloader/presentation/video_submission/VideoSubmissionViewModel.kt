package com.tamersarioglu.easydownloader.presentation.video_submission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.domain.usecase.SubmitVideoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class VideoSubmissionViewModel
@Inject
constructor(private val submitVideoUseCase: SubmitVideoUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(VideoSubmissionUiState())
    val uiState: StateFlow<VideoSubmissionUiState> = _uiState.asStateFlow()

    fun onUrlChanged(url: String) {
        _uiState.value =
                _uiState.value.copy(
                        url = url,
                        isUrlValid = isValidUrl(url),
                        error = null,
                        submissionResult = null
                )
    }

    fun submitVideo() {
        val currentUrl = _uiState.value.url.trim()

        if (currentUrl.isEmpty() || !_uiState.value.isUrlValid) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid video URL")
            return
        }

        _uiState.value =
                _uiState.value.copy(isLoading = true, error = null, submissionResult = null)

        viewModelScope.launch {
            submitVideoUseCase(currentUrl)
                    .onSuccess { videoId ->
                        _uiState.value =
                                _uiState.value.copy(
                                        isLoading = false,
                                        submissionResult =
                                                "Video submitted successfully! Video ID: $videoId",
                                        url = "",
                                        isUrlValid = false
                                )
                    }
                    .onFailure { error ->
                        _uiState.value =
                                _uiState.value.copy(
                                        isLoading = false,
                                        error = getErrorMessage(error)
                                )
                    }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, submissionResult = null)
    }

    private fun isValidUrl(url: String): Boolean {
        val trimmedUrl = url.trim()

        if (trimmedUrl.isEmpty()) {
            return false
        }

        if (!isValidUrlFormat(trimmedUrl)) {
            return false
        }

        return isSupportedDomain(trimmedUrl)
    }

    private fun isValidUrlFormat(url: String): Boolean {
        return try {
            val urlPattern =
                    "^(https?://)?(www\\.)?[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.[a-zA-Z]{2,}(/.*)?$"
            url.matches(urlPattern.toRegex(RegexOption.IGNORE_CASE))
        } catch (e: Exception) {
            false
        }
    }

    private fun isSupportedDomain(url: String): Boolean {
        val lowerCaseUrl = url.lowercase()
        return SUPPORTED_DOMAINS.any { domain -> lowerCaseUrl.contains(domain.lowercase()) }
    }

    private fun getErrorMessage(error: Throwable): String {
        return when (error) {
            is AppError.ValidationError -> {
                if (error.field == "url" && error.message.contains("supported platforms")) {
                    "URL must be from supported platforms: ${SUPPORTED_DOMAINS.joinToString(", ")}"
                } else {
                    error.message
                }
            }
            is AppError.NetworkError -> "Network error. Please check your connection and try again."
            is AppError.ServerError -> "Server error. Please try again later."
            is AppError.UnauthorizedError -> "Authentication error. Please log in again."
            is AppError.ApiError -> error.message
            else -> "An unexpected error occurred. Please try again."
        }
    }

    fun getSupportedDomains(): List<String> = SUPPORTED_DOMAINS

    companion object {
        private val SUPPORTED_DOMAINS =
                listOf(
                        "youtube.com",
                        "youtu.be",
                        "instagram.com",
                        "tiktok.com",
                        "twitter.com",
                        "x.com",
                        "facebook.com",
                        "vimeo.com",
                        "dailymotion.com"
                )
    }
}
