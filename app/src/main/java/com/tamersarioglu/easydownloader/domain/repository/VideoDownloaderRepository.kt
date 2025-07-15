package com.tamersarioglu.easydownloader.domain.repository

import com.tamersarioglu.easydownloader.domain.model.User
import com.tamersarioglu.easydownloader.domain.model.VideoItem

/**
 * Repository interface for video downloader operations.
 * 
 * This interface defines all the data operations required by the application,
 * including user authentication, video submission, and video management.
 * All methods return Result<T> to provide consistent error handling across the app.
 */
interface VideoDownloaderRepository {
    
    /**
     * Registers a new user with the provided credentials.
     * 
     * @param username The desired username (minimum 3 characters)
     * @param password The desired password (minimum 6 characters)
     * @return Result<User> containing the registered user with token on success,
     *         or error information on failure
     * 
     * Expected behavior:
     * - Validates input parameters before making API call
     * - Returns ValidationError for invalid username/password format
     * - Returns ApiError if username already exists
     * - Returns NetworkError for connectivity issues
     * - Automatically stores the JWT token on successful registration
     */
    suspend fun register(username: String, password: String): Result<User>
    
    /**
     * Authenticates an existing user with the provided credentials.
     * 
     * @param username The user's username
     * @param password The user's password
     * @return Result<User> containing the authenticated user with token on success,
     *         or error information on failure
     * 
     * Expected behavior:
     * - Returns ApiError for invalid credentials
     * - Returns NetworkError for connectivity issues
     * - Returns ServerError for backend unavailability
     * - Automatically stores the JWT token on successful login
     */
    suspend fun login(username: String, password: String): Result<User>
    
    /**
     * Submits a video URL for downloading.
     * 
     * @param url The video URL to download (must be from supported domains)
     * @return Result<String> containing the video ID on successful submission,
     *         or error information on failure
     * 
     * Expected behavior:
     * - Validates URL format before making API call
     * - Returns ValidationError for invalid URL format
     * - Returns ApiError with supported domains list for unsupported domains
     * - Returns UnauthorizedError if JWT token is invalid/expired
     * - Returns NetworkError for connectivity issues
     */
    suspend fun submitVideo(url: String): Result<String>
    
    /**
     * Retrieves all videos submitted by the authenticated user.
     * 
     * @return Result<List<VideoItem>> containing the list of user's videos,
     *         or error information on failure
     * 
     * Expected behavior:
     * - Returns empty list if user has no videos
     * - Returns UnauthorizedError if JWT token is invalid/expired
     * - Returns NetworkError for connectivity issues
     * - Returns ServerError for backend unavailability
     * - Videos are ordered by creation date (newest first)
     */
    suspend fun getUserVideos(): Result<List<VideoItem>>
    
    /**
     * Retrieves the current status of a specific video.
     * 
     * @param videoId The ID of the video to check
     * @return Result<VideoItem> containing the video with updated status,
     *         or error information on failure
     * 
     * Expected behavior:
     * - Returns ApiError if video ID doesn't exist or doesn't belong to user
     * - Returns UnauthorizedError if JWT token is invalid/expired
     * - Returns NetworkError for connectivity issues
     * - Includes download URL if status is COMPLETE
     * - Includes error message if status is FAILED
     */
    suspend fun getVideoStatus(videoId: String): Result<VideoItem>
    
    /**
     * Securely stores the JWT authentication token.
     * 
     * @param token The JWT token to store
     * 
     * Expected behavior:
     * - Stores token using encrypted storage mechanism
     * - Overwrites any existing token
     * - Token persists across app restarts
     */
    suspend fun saveAuthToken(token: String)
    
    /**
     * Retrieves the stored JWT authentication token.
     * 
     * @return The stored JWT token, or null if no token is stored
     * 
     * Expected behavior:
     * - Returns null if no token has been stored
     * - Returns null if token storage is corrupted
     * - Does not validate token expiration (handled by API calls)
     */
    suspend fun getAuthToken(): String?
    
    /**
     * Clears the stored JWT authentication token.
     * 
     * Expected behavior:
     * - Removes token from secure storage
     * - Safe to call even if no token is stored
     * - Used during logout process
     */
    suspend fun clearAuthToken()
    
    /**
     * Checks if a valid authentication token is stored.
     * 
     * @return true if a token is stored, false otherwise
     * 
     * Expected behavior:
     * - Returns true if token exists in storage
     * - Returns false if no token or storage is corrupted
     * - Does not validate token with server (used for initial app state)
     */
    suspend fun isAuthenticated(): Boolean
}