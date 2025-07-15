package com.tamersarioglu.easydownloader.domain.usecase

import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.domain.repository.VideoDownloaderRepository
import com.tamersarioglu.easydownloader.domain.usecase.video.SubmitVideoParams
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class SubmitVideoUseCaseTest {
    
    @Mock
    private lateinit var repository: VideoDownloaderRepository
    private lateinit var submitVideoUseCase: SubmitVideoUseCase
    
    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        submitVideoUseCase = SubmitVideoUseCase(repository)
    }
    
    @Test
    fun `invoke with valid YouTube URL should return success`() = runTest {
        // Given
        val url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        val expectedVideoId = "video_123"
        `when`(repository.submitVideo(url)).thenReturn(Result.success(expectedVideoId))
        
        // When
        val result = submitVideoUseCase(url)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedVideoId, result.getOrNull())
    }
    
    @Test
    fun `invoke with valid Instagram URL should return success`() = runTest {
        // Given
        val url = "https://www.instagram.com/p/ABC123/"
        val expectedVideoId = "video_456"
        `when`(repository.submitVideo(url)).thenReturn(Result.success(expectedVideoId))
        
        // When
        val result = submitVideoUseCase(url)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedVideoId, result.getOrNull())
    }
    
    @Test
    fun `invoke with valid TikTok URL should return success`() = runTest {
        // Given
        val url = "https://www.tiktok.com/@user/video/123456789"
        val expectedVideoId = "video_789"
        `when`(repository.submitVideo(url)).thenReturn(Result.success(expectedVideoId))
        
        // When
        val result = submitVideoUseCase(url)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedVideoId, result.getOrNull())
    }
    
    @Test
    fun `invoke with empty URL should return validation error`() = runTest {
        // Given
        val url = ""
        
        // When
        val result = submitVideoUseCase(url)
        
        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as AppError.ValidationError
        assertEquals("url", error.field)
        assertEquals("Please enter a video URL", error.message)
    }
    
    @Test
    fun `invoke with whitespace-only URL should return validation error`() = runTest {
        // Given
        val url = "   "
        
        // When
        val result = submitVideoUseCase(url)
        
        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as AppError.ValidationError
        assertEquals("url", error.field)
        assertEquals("Please enter a video URL", error.message)
    }
    
    @Test
    fun `invoke with invalid URL format should return validation error`() = runTest {
        // Given
        val url = "not-a-valid-url"
        
        // When
        val result = submitVideoUseCase(url)
        
        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as AppError.ValidationError
        assertEquals("url", error.field)
        assertEquals("Please enter a valid URL", error.message)
    }
    
    @Test
    fun `invoke with unsupported domain should return validation error`() = runTest {
        // Given
        val url = "https://www.unsupported-site.com/video/123"
        
        // When
        val result = submitVideoUseCase(url)
        
        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as AppError.ValidationError
        assertEquals("url", error.field)
        assertTrue(error.message.contains("URL must be from supported platforms"))
    }
    
    @Test
    fun `invoke with SubmitVideoParams should work correctly`() = runTest {
        // Given
        val params = SubmitVideoParams("https://www.youtube.com/watch?v=test")
        val expectedVideoId = "video_test"
        `when`(repository.submitVideo("https://www.youtube.com/watch?v=test")).thenReturn(Result.success(expectedVideoId))
        
        // When
        val result = submitVideoUseCase(params)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedVideoId, result.getOrNull())
    }
    
    @Test
    fun `invoke should trim URL whitespace`() = runTest {
        // Given
        val url = "  https://www.youtube.com/watch?v=test  "
        val trimmedUrl = "https://www.youtube.com/watch?v=test"
        val expectedVideoId = "video_test"
        `when`(repository.submitVideo(trimmedUrl)).thenReturn(Result.success(expectedVideoId))
        
        // When
        val result = submitVideoUseCase(url)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedVideoId, result.getOrNull())
    }
    
    @Test
    fun `invoke with repository error should return repository error`() = runTest {
        // Given
        val url = "https://www.youtube.com/watch?v=test"
        val apiError = AppError.ApiError("UNSUPPORTED_URL", "This URL is not supported")
        `when`(repository.submitVideo(url)).thenReturn(Result.failure(apiError))
        
        // When
        val result = submitVideoUseCase(url)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(apiError, result.exceptionOrNull())
    }
    
    @Test
    fun `invoke with network error should return network error`() = runTest {
        // Given
        val url = "https://www.youtube.com/watch?v=test"
        val networkError = AppError.NetworkError
        `when`(repository.submitVideo(url)).thenReturn(Result.failure(networkError))
        
        // When
        val result = submitVideoUseCase(url)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(networkError, result.exceptionOrNull())
    }
    
    @Test
    fun `invoke with unauthorized error should return unauthorized error`() = runTest {
        // Given
        val url = "https://www.youtube.com/watch?v=test"
        val unauthorizedError = AppError.UnauthorizedError
        `when`(repository.submitVideo(url)).thenReturn(Result.failure(unauthorizedError))
        
        // When
        val result = submitVideoUseCase(url)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(unauthorizedError, result.exceptionOrNull())
    }
    
    @Test
    fun `invoke with various supported domains should pass validation`() = runTest {
        // Test multiple supported domains
        val supportedUrls = listOf(
            "https://youtube.com/watch?v=test",
            "https://youtu.be/test",
            "https://www.instagram.com/p/test/",
            "https://www.tiktok.com/@user/video/test",
            "https://twitter.com/user/status/test",
            "https://x.com/user/status/test",
            "https://www.facebook.com/video/test",
            "https://vimeo.com/test",
            "https://www.dailymotion.com/video/test"
        )
        
        supportedUrls.forEach { url ->
            // Given
            val expectedVideoId = "video_test"
            `when`(repository.submitVideo(url)).thenReturn(Result.success(expectedVideoId))
            
            // When
            val result = submitVideoUseCase(url)
            
            // Then
            assertTrue("URL $url should be valid", result.isSuccess)
        }
    }
}