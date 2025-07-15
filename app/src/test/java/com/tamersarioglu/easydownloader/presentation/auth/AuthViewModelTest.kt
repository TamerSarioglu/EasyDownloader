package com.tamersarioglu.easydownloader.presentation.auth

import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.domain.model.VideoItem
import com.tamersarioglu.easydownloader.domain.repository.VideoDownloaderRepository
import com.tamersarioglu.easydownloader.domain.usecase.LoginUseCase
import com.tamersarioglu.easydownloader.domain.usecase.RegisterUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

/**
 * Unit tests for AuthViewModel focusing on authentication state persistence functionality.
 * 
 * Tests cover:
 * - Checking for existing valid tokens on app startup
 * - Implementing automatic login for returning users  
 * - Handling token expiration and logout scenarios
 * 
 * Requirements: 2.5, 2.6, 5.2, 5.3
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var authViewModel: AuthViewModel
    
    @Mock
    private lateinit var mockRegisterUseCase: RegisterUseCase
    
    @Mock
    private lateinit var mockLoginUseCase: LoginUseCase
    
    @Mock
    private lateinit var mockRepository: VideoDownloaderRepository
    
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `authentication state persistence is implemented in AuthViewModel`() = runBlocking {
        // Given - Mock repository to return not authenticated
        `when`(mockRepository.isAuthenticated()).thenReturn(false)

        // When - Create AuthViewModel (this triggers checkAuthenticationStatus in init)
        authViewModel = AuthViewModel(mockRegisterUseCase, mockLoginUseCase, mockRepository)
        
        // Advance the test dispatcher to execute all pending coroutines
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Verify that the authentication check methods exist and work
        val uiState = authViewModel.uiState.value
        assertFalse("User should be logged out when not authenticated", uiState.isLoggedIn)
        
        // Test that the logout method exists and works
        authViewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val logoutState = authViewModel.uiState.value
        assertFalse("User should remain logged out after logout", logoutState.isLoggedIn)
        
        // Test that authentication state check methods exist
        val isAuthenticated = authViewModel.isCurrentlyAuthenticated()
        assertFalse("isCurrentlyAuthenticated should return false", isAuthenticated)
        
        // Test that refresh method exists
        authViewModel.refreshAuthenticationState()
        
        // Test that token expiration handler exists
        authViewModel.handleTokenExpiredDuringUsage()
    }

    @Test
    fun `authentication state persistence handles authenticated user`() = runBlocking {
        // Given - Mock repository to return authenticated with valid token
        `when`(mockRepository.isAuthenticated()).thenReturn(true)
        `when`(mockRepository.getAuthToken()).thenReturn("valid_token")
        `when`(mockRepository.getUserVideos()).thenReturn(Result.success(emptyList<VideoItem>()))

        // When - Create AuthViewModel
        authViewModel = AuthViewModel(mockRegisterUseCase, mockLoginUseCase, mockRepository)
        
        // Advance the test dispatcher to execute all pending coroutines
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - User should be logged in
        val uiState = authViewModel.uiState.value
        assertTrue("User should be logged in with valid token", uiState.isLoggedIn)
        
        val isAuthenticated = authViewModel.isCurrentlyAuthenticated()
        assertTrue("isCurrentlyAuthenticated should return true", isAuthenticated)
    }

    @Test
    fun `authentication state persistence handles token expiration`() = runBlocking {
        // Given - Mock repository to return authenticated but with expired token
        `when`(mockRepository.isAuthenticated()).thenReturn(true)
        `when`(mockRepository.getAuthToken()).thenReturn("expired_token")
        `when`(mockRepository.getUserVideos()).thenReturn(Result.failure(AppError.UnauthorizedError))

        // When - Create AuthViewModel
        authViewModel = AuthViewModel(mockRegisterUseCase, mockLoginUseCase, mockRepository)
        
        // Advance the test dispatcher to execute all pending coroutines
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - User should be logged out due to expired token
        val uiState = authViewModel.uiState.value
        assertFalse("User should be logged out with expired token", uiState.isLoggedIn)
    }
}