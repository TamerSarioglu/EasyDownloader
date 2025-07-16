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

interface VideoDownloaderApiService {
    
    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("videos")
    suspend fun submitVideo(@Body request: VideoSubmissionRequest): Response<VideoSubmissionResponse>
    
    @GET("videos")
    suspend fun getUserVideos(): Response<VideoListResponse>
    
    @GET("videos/{videoId}/status")
    suspend fun getVideoStatus(@Path("videoId") videoId: String): Response<VideoStatusResponse>
}