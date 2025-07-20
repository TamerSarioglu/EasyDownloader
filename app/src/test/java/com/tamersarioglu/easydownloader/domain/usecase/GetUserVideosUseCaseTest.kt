package com.tamersarioglu.easydownloader.domain.usecase

import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.domain.model.VideoItem
import com.tamersarioglu.easydownloader.domain.model.VideoStatus
import com.tamersarioglu.easydownloader.domain.repository.VideoDownloaderRepository
import com.tamersarioglu.easydownloader.domain.usecase.video.GetUserVideosParams
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class GetUserVideosUseCaseTest {
    
    @Mock
    private lateinit var repository: VideoDownloaderRepository
    private lateinit var getUserVideosUseCase: GetUserVideosUseCase
    
    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        getUserVideosUseCase = GetUserVideosUseCase(repository)
    }
    
    @Test
    fun `invoke should return list of videos on success`() = runTest {

        val expectedVideos = listOf(
            VideoItem("1", "https://youtube.com/watch?v=1", VideoStatus.COMPLETE, "2023-01-01"),
            VideoItem("2", "https://instagram.com/p/2", VideoStatus.PENDING, "2023-01-02"),
            VideoItem("3", "https://tiktok.com/video/3", VideoStatus.FAILED, "2023-01-03", "Download failed")
        )
        `when`(repository.getUserVideos()).thenReturn(Result.success(expectedVideos))
        

        val result = getUserVideosUseCase()
        

        assertTrue(result.isSuccess)
        assertEquals(expectedVideos, result.getOrNull())
    }
    
    @Test
    fun `invoke should return empty list when user has no videos`() = runTest {

        val expectedVideos = emptyList<VideoItem>()
        `when`(repository.getUserVideos()).thenReturn(Result.success(expectedVideos))
        

        val result = getUserVideosUseCase()
        

        assertTrue(result.isSuccess)
        assertEquals(expectedVideos, result.getOrNull())
        assertTrue(result.getOrNull()!!.isEmpty())
    }
    
    @Test
    fun `invoke with GetUserVideosParams should work correctly`() = runTest {

        val params = GetUserVideosParams
        val expectedVideos = listOf(
            VideoItem("1", "https://youtube.com/watch?v=1", VideoStatus.COMPLETE, "2023-01-01")
        )
        `when`(repository.getUserVideos()).thenReturn(Result.success(expectedVideos))
        

        val result = getUserVideosUseCase(params)
        

        assertTrue(result.isSuccess)
        assertEquals(expectedVideos, result.getOrNull())
    }
    
    @Test
    fun `invoke with network error should return network error`() = runTest {

        val networkError = AppError.NetworkError
        `when`(repository.getUserVideos()).thenReturn(Result.failure(networkError))
        

        val result = getUserVideosUseCase()
        

        assertTrue(result.isFailure)
        assertEquals(networkError, result.exceptionOrNull())
    }
    
    @Test
    fun `invoke with unauthorized error should return unauthorized error`() = runTest {

        val unauthorizedError = AppError.UnauthorizedError
        `when`(repository.getUserVideos()).thenReturn(Result.failure(unauthorizedError))
        

        val result = getUserVideosUseCase()
        

        assertTrue(result.isFailure)
        assertEquals(unauthorizedError, result.exceptionOrNull())
    }
    
    @Test
    fun `invoke with server error should return server error`() = runTest {

        val serverError = AppError.ServerError
        `when`(repository.getUserVideos()).thenReturn(Result.failure(serverError))
        

        val result = getUserVideosUseCase()
        

        assertTrue(result.isFailure)
        assertEquals(serverError, result.exceptionOrNull())
    }
    
    @Test
    fun `invoke with API error should return API error`() = runTest {

        val apiError = AppError.ApiError("USER_NOT_FOUND", "User not found")
        `when`(repository.getUserVideos()).thenReturn(Result.failure(apiError))
        

        val result = getUserVideosUseCase()
        

        assertTrue(result.isFailure)
        assertEquals(apiError, result.exceptionOrNull())
    }
    
    @Test
    fun `invoke should handle videos with different statuses correctly`() = runTest {

        val expectedVideos = listOf(
            VideoItem("1", "https://youtube.com/watch?v=1", VideoStatus.PENDING, "2023-01-01"),
            VideoItem("2", "https://youtube.com/watch?v=2", VideoStatus.COMPLETE, "2023-01-02"),
            VideoItem("3", "https://youtube.com/watch?v=3", VideoStatus.FAILED, "2023-01-03", "Network timeout")
        )
        `when`(repository.getUserVideos()).thenReturn(Result.success(expectedVideos))
        

        val result = getUserVideosUseCase()
        

        assertTrue(result.isSuccess)
        val videos = result.getOrNull()!!
        assertEquals(3, videos.size)
        assertEquals(VideoStatus.PENDING, videos[0].status)
        assertEquals(VideoStatus.COMPLETE, videos[1].status)
        assertEquals(VideoStatus.FAILED, videos[2].status)
        assertEquals("Network timeout", videos[2].errorMessage)
    }
}
