package com.tamersarioglu.easydownloader.domain.model

data class VideoItem(
    val id: String,
    val originalUrl: String,
    val status: VideoStatus,
    val createdAt: String,
    val errorMessage: String? = null
)