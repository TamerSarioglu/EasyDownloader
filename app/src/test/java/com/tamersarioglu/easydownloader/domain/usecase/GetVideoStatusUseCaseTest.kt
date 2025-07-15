package com.tamersarioglu.easydownloader.domain.usecase

import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.domain.model.VideoItem
import com.tamersarioglu.easydownloader.domain.model.VideoStatus
import com.tamersarioglu.easydownloader.domain.repository.VideoDownloaderRepository
import com.tamersarioglu.easydownloader.domain.usecase.video.GetVideoStatusParams
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class GetVideoStatusUseCaseTest {
    
    @Mock
    private lateinit var repository: VideoDownloaderRepository
    private lateinit var getVideoStatusUseCase: GetVideoStatusUseCase
    
    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        getVideoStatusUseCase = GetVideoStatusUseCase(repository)
    }
    
    @Test
    fun `invoke with valid video ID should return video status`() = runTest {
        // Given
        val videoId = "video_123"
        val expectedVideo = VideoItem(videoId, "https://youtube.com/watch?v=test", VideoStatus.COMPLETE, "2023-01-01")
        `when`(repository.getVideoStatus(videoId)).thenReturn(Result.success(expectedVideo))
        
        // When
        val result = getVideoStatusUseCase(videoId)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedVideo, result.getOrNull())
    }
    
    @Test
    fun `invoke with pending video should return pending status`() = runTest {
        // Given
        val videoId = "video_pending"
        val expectedVideo = VideoItem(videoId, "https://youtube.com/watch?v=pending", VideoStatus.PENDING, "2023-01-01")
        `when`(repository.getVideoStatus(videoId)).thenReturn(Result.success(expectedVideo))
        
        // When
        val result = getVideoStatusUseCase(videoId)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(VideoStatus.PENDING, result.getOrNull()?.status)
    }
    
    @Test
    fun `invoke with failed video should return failed status with error message`() = runTest {
        // Given
        val videoId = "video_failed"
        val expectedVideo = VideoItem(videoId, "https://youtube.com/watch?v=failed", VideoStatus.FAILED, "2023-01-01", "Download failed")
        `when`(repository.getVideoStatus(videoId)).thenReturn(Result.success(expectedVideo))
        
        // When
        val result = getVideoStatusUseCase(videoId)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(VideoStatus.FAILED, result.getOrNull()?.status)
        assertEquals("Download failed", result.getOrNull()?.errorMessage)
    }
    
    @Test
    fun `invoke with empty video ID should return validation error`() = runTest {
        // Given
        val videoId = ""
        
        // When
        val result = getVideoStatusUseCase(videoId)
        
        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as AppError.ValidationError
        assertEquals("videoId", error.field)
        assertEquals("Video ID cannot be empty", error.message)
    }
    
    @Test
    fun `invoke with whitespace-only video ID should return validation error`() = runTest {
        // Given
        val videoId = "   "
        
        // When
        val result = getVideoStatusUseCase(videoId)
        
        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as AppError.ValidationError
        assertEquals("videoId", error.field)
        assertEquals("Video ID cannot be empty", error.message)
    }
    
    @Test
    fun `invoke with short video ID should return validation error`() = runTest {
        // Given
        val videoId = "short" // Less than 8 characters
        
        // When
        val result = getVideoStatusUseCase(videoId)
        
        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as AppError.ValidationError
        assertEquals("videoId", error.field)
        assertEquals("Invalid video ID format", error.message)
    }
    
    @Test
    fun `invoke with invalid video ID format should return validation error`() = runTest {
        // Given
        val videoId = "invalid@video#id!" // Contains invalid characters
        
        // When
        val result = getVideoStatusUseCase(videoId)
        
        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as AppError.ValidationError
        assertEquals("videoId", error.field)
        assertEquals("Invalid video ID format", error.message)
    }
    
    @Test
    fun `invoke with GetVideoStatusParams should work correctly`() = runTest {
        // Given
        val params = GetVideoStatusParams("video_123")
        val expectedVideo = VideoItem("video_123", "https://youtube.com/watch?v=test", VideoStatus.COMPLETE, "2023-01-01")
        `when`(repository.getVideoStatus("video_123")).thenReturn(Result.success(expectedVideo))
        
        // When
        val result = getVideoStatusUseCase(params)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedVideo, result.getOrNull())
    }
    
    @Test
    fun `invoke should trim video ID whitespace`() = runTest {
        // Given
        val videoId = "  video_123  "
        val trimmedVideoId = "video_123"
        val expectedVideo = VideoItem(trimmedVideoId, "https://youtube.com/watch?v=test", VideoStatus.COMPLETE, "2023-01-01")
        `when`(repository.getVideoStatus(trimmedVideoId)).thenReturn(Result.success(expectedVideo))
        
        // When
        val result = getVideoStatusUseCase(videoId)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedVideo, result.getOrNull())
    }
    
    @Test
    fun `invoke with repository error should return repository error`() = runTest {
        // Given
        val videoId = "video_123"
        val apiError = AppError.ApiError("VIDEO_NOT_FOUND", "Video not found")
        `when`(repository.getVideoStatus(videoId)).thenReturn(Result.failure(apiError))
        
        // When
        val result = getVideoStatusUseCase(videoId)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(apiError, result.exceptionOrNull())
    }
    
    @Test
    fun `invoke with network error should return network error`() = runTest {
        // Given
        val videoId = "video_123"
        val networkError = AppError.NetworkError
        `when`(repository.getVideoStatus(videoId)).thenReturn(Result.failure(networkError))
        
        // When
        val result = getVideoStatusUseCase(videoId)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(networkError, result.exceptionOrNull())
    }
    
    @Test
    fun `invoke with unauthorized error should return unauthorized error`() = runTest {
        // Given
        val videoId = "video_123"
        val unauthorizedError = AppError.UnauthorizedError
        `when`(repository.getVideoStatus(videoId)).thenReturn(Result.failure(unauthorizedError))
        
        // When
        val result = getVideoStatusUseCase(videoId)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(unauthorizedError, result.exceptionOrNull())
    }
    
    @Test
    fun `invoke with valid alphanumeric and hyphen video IDs should pass validation`() = runTest {
        // Test various valid video ID formats
        val validVideoIds = listOf(
            "video_123",
            "video-456",
            "VIDEO789",
            "abc123def",
            "test_video_id_123",
            "12345678", // minimum length
            "a1b2c3d4e5f6g7h8i9j0" // longer ID
        )
        
        validVideoIds.forEach { videoId ->
            // Given
            val expectedVideo = VideoItem(videoId, "https://youtube.com/watch?v=test", VideoStatus.COMPLETE, "2023-01-01")
            `when`(repository.getVideoStatus(videoId)).thenReturn(Result.success(expectedVideo))
            
            // When
            val result = getVideoStatusUseCase(videoId)
            
            // Then
            assertTrue("Video ID $videoId should be valid", result.isSuccess)
        }
    }
}