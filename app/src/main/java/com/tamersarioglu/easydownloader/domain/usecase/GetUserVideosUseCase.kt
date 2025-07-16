package com.tamersarioglu.easydownloader.domain.usecase

import com.tamersarioglu.easydownloader.domain.model.VideoItem
import com.tamersarioglu.easydownloader.domain.repository.VideoDownloaderRepository
import com.tamersarioglu.easydownloader.domain.usecase.base.BaseUseCase
import com.tamersarioglu.easydownloader.domain.usecase.video.GetUserVideosParams
import javax.inject.Inject

class GetUserVideosUseCase @Inject constructor(
    private val repository: VideoDownloaderRepository
) : BaseUseCase<GetUserVideosParams, List<VideoItem>>() {
    
    override suspend fun execute(parameters: GetUserVideosParams): Result<List<VideoItem>> {
        return repository.getUserVideos()
    }
    
    suspend operator fun invoke(): Result<List<VideoItem>> {
        return invoke(GetUserVideosParams)
    }
}