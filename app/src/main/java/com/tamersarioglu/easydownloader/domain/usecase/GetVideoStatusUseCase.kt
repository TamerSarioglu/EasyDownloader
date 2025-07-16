package com.tamersarioglu.easydownloader.domain.usecase

import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.domain.model.VideoItem
import com.tamersarioglu.easydownloader.domain.repository.VideoDownloaderRepository
import com.tamersarioglu.easydownloader.domain.usecase.base.BaseUseCase
import com.tamersarioglu.easydownloader.domain.usecase.video.GetVideoStatusParams
import javax.inject.Inject

class GetVideoStatusUseCase @Inject constructor(
    private val repository: VideoDownloaderRepository
) : BaseUseCase<GetVideoStatusParams, VideoItem>() {
    
    override suspend fun execute(parameters: GetVideoStatusParams): Result<VideoItem> {
        val validationResult = validateVideoId(parameters.videoId)
        if (validationResult.isFailure) {
            return validationResult
        }
        
        return repository.getVideoStatus(parameters.videoId.trim())
    }
    
    suspend operator fun invoke(videoId: String): Result<VideoItem> {
        return invoke(GetVideoStatusParams(videoId))
    }
    
    private fun validateVideoId(videoId: String): Result<VideoItem> {
        val trimmedVideoId = videoId.trim()
        
        if (trimmedVideoId.isEmpty()) {
            return Result.failure(
                AppError.ValidationError("videoId", "Video ID cannot be empty")
            )
        }
        
        if (trimmedVideoId.length < MIN_VIDEO_ID_LENGTH) {
            return Result.failure(
                AppError.ValidationError("videoId", "Invalid video ID format")
            )
        }
        
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