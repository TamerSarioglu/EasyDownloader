package com.tamersarioglu.easydownloader.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VideoSubmissionRequest(
    val url: String
)

@Serializable
data class VideoSubmissionResponse(
    val videoId: String,
    val status: String
)

@Serializable
data class VideoSummaryDto(
    val id: String,
    val originalUrl: String,
    val status: String,
    val createdAt: String,
    val errorMessage: String? = null
)

@Serializable
data class VideoListResponse(
    val videos: List<VideoSummaryDto>
)

@Serializable
data class VideoStatusResponse(
    val videoId: String,
    val status: String,
    val errorMessage: String? = null,
    val downloadUrl: String? = null
)