package com.tamersarioglu.easydownloader.domain.repository

import com.tamersarioglu.easydownloader.domain.model.User
import com.tamersarioglu.easydownloader.domain.model.VideoItem

interface VideoDownloaderRepository {
    
    suspend fun register(username: String, password: String): Result<User>
    
    suspend fun login(username: String, password: String): Result<User>
    
    suspend fun submitVideo(url: String): Result<String>
    
    suspend fun getUserVideos(): Result<List<VideoItem>>
    
    suspend fun getVideoStatus(videoId: String): Result<VideoItem>
    
    suspend fun saveAuthToken(token: String)
    
    suspend fun getAuthToken(): String?
    
    suspend fun clearAuthToken()
    
    suspend fun isAuthenticated(): Boolean
}