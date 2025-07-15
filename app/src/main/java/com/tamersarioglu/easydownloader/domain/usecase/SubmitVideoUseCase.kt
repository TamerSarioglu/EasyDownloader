package com.tamersarioglu.easydownloader.domain.usecase

import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.domain.repository.VideoDownloaderRepository
import com.tamersarioglu.easydownloader.domain.usecase.base.BaseUseCase
import com.tamersarioglu.easydownloader.domain.usecase.video.SubmitVideoParams
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * Use case for submitting video URLs for downloading with URL validation.
 * 
 * This use case handles the business logic for video submission, including:
 * - URL format validation
 * - Supported domain checking
 * - Calling the repository to submit the video
 * - Proper error handling and user feedback
 * 
 * Requirements covered: 3.2, 3.4, 3.6
 */
class SubmitVideoUseCase @Inject constructor(
    private val repository: VideoDownloaderRepository
) : BaseUseCase<SubmitVideoParams, String>() {
    
    /**
     * Submits a video URL for downloading with validation.
     * 
     * @param parameters SubmitVideoParams containing the video URL
     * @return Result<String> containing the video ID on success, or error on failure
     */
    override suspend fun execute(parameters: SubmitVideoParams): Result<String> {
        // Validate URL format and domain
        val validationResult = validateUrl(parameters.url)
        if (validationResult.isFailure) {
            return validationResult
        }
        
        // Call repository to submit video
        return repository.submitVideo(parameters.url.trim())
    }
    
    /**
     * Convenience method for direct parameter passing (maintains backward compatibility).
     */
    suspend operator fun invoke(url: String): Result<String> {
        return invoke(SubmitVideoParams(url))
    }
    
    /**
     * Validates the video URL format and checks if it's from a supported domain.
     * 
     * @param url The URL to validate
     * @return Result<String> with validation error if invalid, or success if valid
     */
    private fun validateUrl(url: String): Result<String> {
        val trimmedUrl = url.trim()
        
        // Check if URL is empty
        if (trimmedUrl.isEmpty()) {
            return Result.failure(
                AppError.ValidationError("url", "Please enter a video URL")
            )
        }
        
        // Check basic URL format
        if (!isValidUrlFormat(trimmedUrl)) {
            return Result.failure(
                AppError.ValidationError("url", "Please enter a valid URL")
            )
        }
        
        // Check if URL is from a supported domain
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
    
    /**
     * Checks if the URL has a valid format.
     * 
     * @param url The URL to check
     * @return true if the URL format is valid, false otherwise
     */
    private fun isValidUrlFormat(url: String): Boolean {
        return try {
            val pattern = Pattern.compile(URL_PATTERN, Pattern.CASE_INSENSITIVE)
            pattern.matcher(url).matches()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Checks if the URL is from a supported domain.
     * 
     * @param url The URL to check
     * @return true if the domain is supported, false otherwise
     */
    private fun isSupportedDomain(url: String): Boolean {
        val lowerCaseUrl = url.lowercase()
        return SUPPORTED_DOMAINS.any { domain ->
            lowerCaseUrl.contains(domain.lowercase())
        }
    }
    
    companion object {
        // Basic URL pattern for validation
        private const val URL_PATTERN = "^(https?://)?(www\\.)?[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.[a-zA-Z]{2,}(/.*)?$"
        
        // List of supported domains (based on common video platforms)
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