package com.tamersarioglu.easydownloader.data.remote.api

import com.tamersarioglu.easydownloader.data.remote.dto.AuthResponse
import com.tamersarioglu.easydownloader.data.remote.dto.LoginRequest
import com.tamersarioglu.easydownloader.data.remote.dto.RegisterRequest
import com.tamersarioglu.easydownloader.data.remote.dto.VideoListResponse
import com.tamersarioglu.easydownloader.data.remote.dto.VideoStatusResponse
import com.tamersarioglu.easydownloader.data.remote.dto.VideoSubmissionRequest
import com.tamersarioglu.easydownloader.data.remote.dto.VideoSubmissionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * API service interface for the EasyDownloader backend
 * Handles authentication and video management operations
 */
interface VideoDownloaderApiService {
    
    /**
     * Register a new user account
     * @param request Registration request containing username and password
     * @return Authentication response with JWT token
     */
    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    /**
     * Login with existing user credentials
     * @param request Login request containing username and password
     * @return Authentication response with JWT token
     */
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    /**
     * Submit a video URL for downloading
     * @param request Video submission request containing the URL
     * @return Video submission response with video ID and initial status
     */
    @POST("videos")
    suspend fun submitVideo(@Body request: VideoSubmissionRequest): Response<VideoSubmissionResponse>
    
    /**
     * Get all videos submitted by the authenticated user
     * @return List of video summaries with status information
     */
    @GET("videos")
    suspend fun getUserVideos(): Response<VideoListResponse>
    
    /**
     * Get detailed status information for a specific video
     * @param videoId The ID of the video to check
     * @return Video status response with current status and download URL if available
     */
    @GET("videos/{videoId}/status")
    suspend fun getVideoStatus(@Path("videoId") videoId: String): Response<VideoStatusResponse>
}