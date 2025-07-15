package com.tamersarioglu.easydownloader.domain.usecase.video

/**
 * Parameter classes for video management use cases.
 * 
 * These data classes provide type-safe parameter passing and make
 * the use cases more testable and maintainable.
 */

/**
 * Parameters for video submission.
 * 
 * @param url The video URL to submit for downloading
 */
data class SubmitVideoParams(
    val url: String
)

/**
 * Parameters for getting video status.
 * 
 * @param videoId The ID of the video to check status for
 */
data class GetVideoStatusParams(
    val videoId: String
)

/**
 * Parameters for getting user videos (no parameters needed, but keeping for consistency).
 */
object GetUserVideosParams