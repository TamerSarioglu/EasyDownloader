package com.tamersarioglu.easydownloader.domain.usecase

import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.domain.model.VideoItem
import com.tamersarioglu.easydownloader.domain.repository.VideoDownloaderRepository
import com.tamersarioglu.easydownloader.domain.usecase.base.BaseUseCase
import com.tamersarioglu.easydownloader.domain.usecase.video.GetVideoStatusParams
import javax.inject.Inject

/**
 * Use case for checking the status of a specific video.
 * 
 * This use case handles the business logic for video status checking, including:
 * - Video ID validation
 * - Calling the repository to get video status
 * - Proper error handling for authentication and network issues
 * 
 * Requirements covered: 4.2, 4.4, 4.5
 */
class GetVideoStatusUseCase @Inject constructor(
    private val repository: VideoDownloaderRepository
) : BaseUseCase<GetVideoStatusParams, VideoItem>() {
    
    /**
     * Retrieves the current status of a specific video.
     * 
     * @param parameters GetVideoStatusParams containing the video ID
     * @return Result<VideoItem> containing the video with updated status on success,
     *         or error information on failure
     */
    override suspend fun execute(parameters: GetVideoStatusParams): Result<VideoItem> {
        // Validate video ID
        val validationResult = validateVideoId(parameters.videoId)
        if (validationResult.isFailure) {
            return validationResult
        }
        
        // Call repository to get video status
        return repository.getVideoStatus(parameters.videoId.trim())
    }
    
    /**
     * Convenience method for direct parameter passing (maintains backward compatibility).
     */
    suspend operator fun invoke(videoId: String): Result<VideoItem> {
        return invoke(GetVideoStatusParams(videoId))
    }
    
    /**
     * Validates the video ID.
     * 
     * @param videoId The video ID to validate
     * @return Result<VideoItem> with validation error if invalid, or success if valid
     */
    private fun validateVideoId(videoId: String): Result<VideoItem> {
        val trimmedVideoId = videoId.trim()
        
        // Check if video ID is empty
        if (trimmedVideoId.isEmpty()) {
            return Result.failure(
                AppError.ValidationError("videoId", "Video ID cannot be empty")
            )
        }
        
        // Check if video ID has minimum length (assuming UUIDs or similar)
        if (trimmedVideoId.length < MIN_VIDEO_ID_LENGTH) {
            return Result.failure(
                AppError.ValidationError("videoId", "Invalid video ID format")
            )
        }
        
        // Check if video ID contains only valid characters (alphanumeric and hyphens)
        if (!trimmedVideoId.matches(VIDEO_ID_PATTERN.toRegex())) {
            return Result.failure(
                AppError.ValidationError("videoId", "Invalid video ID format")
            )
        }
        
        return Result.success(VideoItem("", "", com.tamersarioglu.easydownloader.domain.model.VideoStatus.PENDING, ""))
    }
    
    companion object {
        private const val MIN_VIDEO_ID_LENGTH = 8
        private const val VIDEO_ID_PATTERN = "^[a-zA-Z0-9-_]+$"
    }
}