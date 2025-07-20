package com.tamersarioglu.easydownloader.presentation.auth

import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.domain.model.VideoItem
import com.tamersarioglu.easydownloader.domain.repository.VideoDownloaderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class AuthStateViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: VideoDownloaderRepository
    private lateinit var viewModel: AuthStateViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        
        // Default setup for repository
        whenever(repository.isAuthenticated()).thenReturn(false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init checks authentication status`() = runTest {
        // Given
        whenever(repository.isAuthenticated()).thenReturn(false)
        
        // When
        viewModel = AuthStateViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val authState = viewModel.authState.first()
        assertFalse(authState.isAuthenticated)
        assertFalse(authState.isLoading)
    }
    
    @Test
    fun `validateTokenWithServer success updates state correctly`() = runTest {
        // Given
        whenever(repository.isAuthenticated()).thenReturn(true)
        whenever(repository.getAuthToken()).thenReturn("valid-token")
        whenever(repository.getUserVideos()).thenReturn(Result.success(emptyList<VideoItem>()))
        
        // When
        viewModel = AuthStateViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val authState = viewModel.authState.first()
        assertTrue(authState.isAuthenticated)
        assertFalse(authState.isLoading)
    }
    
    @Test
    fun `validateTokenWithServer failure with unauthorized error updates state correctly`() = runTest {
        // Given
        whenever(repository.isAuthenticated()).thenReturn(true)
        whenever(repository.getAuthToken()).thenReturn("invalid-token")
        whenever(repository.getUserVideos()).thenReturn(Result.failure(AppError.UnauthorizedError("Token expired")))
        
        // When
        viewModel = AuthStateViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val authState = viewModel.authState.first()
        assertFalse(authState.isAuthenticated)
        assertFalse(authState.isLoading)
        verify(repository).clearAuthToken()
    }
    
    @Test
    fun `validateTokenWithServer failure with network error assumes authenticated`() = runTest {
        // Given
        whenever(repository.isAuthenticated()).thenReturn(true)
        whenever(repository.getAuthToken()).thenReturn("valid-token")
        whenever(repository.getUserVideos()).thenReturn(Result.failure(AppError.NetworkError("Network error")))
        
        // When
        viewModel = AuthStateViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val authState = viewModel.authState.first()
        assertTrue(authState.isAuthenticated)
        assertFalse(authState.isLoading)
    }
    
    @Test
    fun `logout clears auth token and updates state`() = runTest {
        // Given
        whenever(repository.isAuthenticated()).thenReturn(true)
        whenever(repository.getAuthToken()).thenReturn("valid-token")
        whenever(repository.getUserVideos()).thenReturn(Result.success(emptyList<VideoItem>()))
        
        viewModel = AuthStateViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Initial state should be authenticated
        assertTrue(viewModel.authState.first().isAuthenticated)
        
        // When
        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val authState = viewModel.authState.first()
        assertFalse(authState.isAuthenticated)
        verify(repository).clearAuthToken()
    }
    
    @Test
    fun `onAuthenticationSuccess updates state correctly`() = runTest {
        // Given
        whenever(repository.isAuthenticated()).thenReturn(false)
        
        viewModel = AuthStateViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Initial state should be not authenticated
        assertFalse(viewModel.authState.first().isAuthenticated)
        
        // When
        viewModel.onAuthenticationSuccess()
        
        // Then
        val authState = viewModel.authState.first()
        assertTrue(authState.isAuthenticated)
        assertFalse(authState.isLoading)
    }
}