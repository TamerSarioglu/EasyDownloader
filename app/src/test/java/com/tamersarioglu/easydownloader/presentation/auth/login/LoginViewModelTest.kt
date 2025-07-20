package com.tamersarioglu.easydownloader.presentation.auth.login

import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.domain.model.User
import com.tamersarioglu.easydownloader.domain.usecase.LoginUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var loginUseCase: LoginUseCase
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        loginUseCase = mock()
        viewModel = LoginViewModel(loginUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login success updates state correctly`() = runTest {
        // Given
        val username = "testuser"
        val password = "password123"
        val user = User(id = "1", username = username)
        
        viewModel.updateUsername(username)
        viewModel.updatePassword(password)
        
        whenever(loginUseCase(username, password)).thenReturn(Result.success(user))
        
        // When
        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.first()
        assertTrue(uiState.isLoginSuccessful)
        assertFalse(uiState.isLoading)
        assertEquals(username, uiState.username)
        assertNull(uiState.error)
    }
    
    @Test
    fun `login failure with network error updates state correctly`() = runTest {
        // Given
        val username = "testuser"
        val password = "password123"
        val error = AppError.NetworkError("Network error")
        
        viewModel.updateUsername(username)
        viewModel.updatePassword(password)
        
        whenever(loginUseCase(username, password)).thenReturn(Result.failure(error))
        
        // When
        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.isLoginSuccessful)
        assertFalse(uiState.isLoading)
        assertTrue(uiState.error?.contains("Network error") == true)
    }
    
    @Test
    fun `login failure with validation error updates form state correctly`() = runTest {
        // Given
        val username = "testuser"
        val password = "password123"
        val error = AppError.ValidationError(field = "username", message = "Invalid username")
        
        viewModel.updateUsername(username)
        viewModel.updatePassword(password)
        
        whenever(loginUseCase(username, password)).thenReturn(Result.failure(error))
        
        // When
        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val formState = viewModel.formState.first()
        assertEquals("Invalid username", formState.usernameError)
        assertNull(formState.passwordError)
    }
    
    @Test
    fun `isFormValid returns true for valid form`() {
        // Given
        viewModel.updateUsername("validuser")
        viewModel.updatePassword("validpassword")
        
        // When/Then
        assertTrue(viewModel.isFormValid())
    }
    
    @Test
    fun `isFormValid returns false for empty username`() {
        // Given
        viewModel.updateUsername("")
        viewModel.updatePassword("validpassword")
        
        // When/Then
        assertFalse(viewModel.isFormValid())
    }
    
    @Test
    fun `isFormValid returns false for empty password`() {
        // Given
        viewModel.updateUsername("validuser")
        viewModel.updatePassword("")
        
        // When/Then
        assertFalse(viewModel.isFormValid())
    }
    
    @Test
    fun `resetSuccessState clears success flag`() = runTest {
        // Given
        val username = "testuser"
        val password = "password123"
        val user = User(id = "1", username = username)
        
        viewModel.updateUsername(username)
        viewModel.updatePassword(password)
        
        whenever(loginUseCase(username, password)).thenReturn(Result.success(user))
        
        // When
        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertTrue(viewModel.uiState.first().isLoginSuccessful)
        
        // When
        viewModel.resetSuccessState()
        
        // Then
        assertFalse(viewModel.uiState.first().isLoginSuccessful)
    }
}