package com.tamersarioglu.easydownloader.domain.usecase

import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.domain.repository.VideoDownloaderRepository
import com.tamersarioglu.easydownloader.domain.usecase.base.BaseUseCase
import com.tamersarioglu.easydownloader.domain.usecase.video.SubmitVideoParams
import java.util.regex.Pattern
import javax.inject.Inject

class SubmitVideoUseCase @Inject constructor(
    private val repository: VideoDownloaderRepository
) : BaseUseCase<SubmitVideoParams, String>() {
    
    override suspend fun execute(parameters: SubmitVideoParams): Result<String> {
        val validationResult = validateUrl(parameters.url)
        if (validationResult.isFailure) {
            return validationResult
        }
        
        return repository.submitVideo(parameters.url.trim())
    }
    
    suspend operator fun invoke(url: String): Result<String> {
        return invoke(SubmitVideoParams(url))
    }
    
    private fun validateUrl(url: String): Result<String> {
        val trimmedUrl = url.trim()
        
        if (trimmedUrl.isEmpty()) {
            return Result.failure(
                AppError.ValidationError("url", "Please enter a video URL")
            )
        }
        
        if (!isValidUrlFormat(trimmedUrl)) {
            return Result.failure(
                AppError.ValidationError("url", "Please enter a valid URL")
            )
        }
        
        if (!isSupportedDomain(trimmedUrl)) {
            return Result.failure(
                AppError.ValidationError(
                    "url", 
                    "URL must be from supported platforms: ${SUPPORTED_DOMAINS.joinToString(", ")}"
                )
            )
        }
        
        return Result.success(trimmedUrl)
    }
    
    private fun isValidUrlFormat(url: String): Boolean {
        return try {
            val pattern = Pattern.compile(URL_PATTERN, Pattern.CASE_INSENSITIVE)
            pattern.matcher(url).matches()
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isSupportedDomain(url: String): Boolean {
        val lowerCaseUrl = url.lowercase()
        return SUPPORTED_DOMAINS.any { domain ->
            lowerCaseUrl.contains(domain.lowercase())
        }
    }
    
    companion object {
        private const val URL_PATTERN = "^(https?://)?(www\\.)?[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.[a-zA-Z]{2,}(/.*)?$"
        
        private val SUPPORTED_DOMAINS = listOf(
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