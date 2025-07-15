package com.tamersarioglu.easydownloader.domain.model

/**
 * Domain model representing a video item with its download status
 */
data class VideoItem(
    val id: String,
    val originalUrl: String,
    val status: VideoStatus,
    val createdAt: String,
    val errorMessage: String? = null
)