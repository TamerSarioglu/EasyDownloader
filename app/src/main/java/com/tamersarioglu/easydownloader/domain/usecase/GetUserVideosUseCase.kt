package com.tamersarioglu.easydownloader.domain.usecase

import com.tamersarioglu.easydownloader.domain.model.VideoItem
import com.tamersarioglu.easydownloader.domain.repository.VideoDownloaderRepository
import com.tamersarioglu.easydownloader.domain.usecase.base.BaseUseCase
import com.tamersarioglu.easydownloader.domain.usecase.video.GetUserVideosParams
import javax.inject.Inject

/**
 * Use case for fetching the list of videos submitted by the authenticated user.
 * 
 * This use case handles the business logic for retrieving user videos, including:
 * - Calling the repository to fetch user's video list
 * - Proper error handling for authentication and network issues
 * - Returning an empty list when user has no videos
 * 
 * Requirements covered: 4.2, 4.4, 4.5
 */
class GetUserVideosUseCase @Inject constructor(
    private val repository: VideoDownloaderRepository
) : BaseUseCase<GetUserVideosParams, List<VideoItem>>() {
    
    /**
     * Retrieves all videos submitted by the authenticated user.
     * 
     * @param parameters GetUserVideosParams (no actual parameters needed)
     * @return Result<List<VideoItem>> containing the list of user's videos on success,
     *         or error information on failure
     */
    override suspend fun execute(parameters: GetUserVideosParams): Result<List<VideoItem>> {
        // Call repository to get user videos
        return repository.getUserVideos()
    }
    
    /**
     * Convenience method for calling without parameters.
     */
    suspend operator fun invoke(): Result<List<VideoItem>> {
        return invoke(GetUserVideosParams)
    }
}