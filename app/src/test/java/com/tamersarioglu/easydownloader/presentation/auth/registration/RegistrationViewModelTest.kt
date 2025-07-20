package com.tamersarioglu.easydownloader.presentation.auth.registration

import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.domain.model.User
import com.tamersarioglu.easydownloader.domain.usecase.RegisterUseCase
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
class RegistrationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var registerUseCase: RegisterUseCase
    private lateinit var viewModel: RegistrationViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        registerUseCase = mock()
        viewModel = RegistrationViewModel(registerUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `register success updates state correctly`() = runTest {
        // Given
        val username = "testuser"
        val password = "password123"
        val user = User(id = "1", username = username)
        
        viewModel.updateUsername(username)
        viewModel.updatePassword(password)
        
        whenever(registerUseCase(username, password)).thenReturn(Result.success(user))
        
        // When
        viewModel.register()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.first()
        assertTrue(uiState.isRegistrationSuccessful)
        assertFalse(uiState.isLoading)
        assertEquals(username, uiState.username)
        assertNull(uiState.error)
    }
    
    @Test
    fun `register failure with network error updates state correctly`() = runTest {
        // Given
        val username = "testuser"
        val password = "password123"
        val error = AppError.NetworkError("Network error")
        
        viewModel.updateUsername(username)
        viewModel.updatePassword(password)
        
        whenever(registerUseCase(username, password)).thenReturn(Result.failure(error))
        
        // When
        viewModel.register()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.isRegistrationSuccessful)
        assertFalse(uiState.isLoading)
        assertTrue(uiState.error?.contains("Network error") == true)
    }
    
    @Test
    fun `register failure with validation error updates form state correctly`() = runTest {
        // Given
        val username = "testuser"
        val password = "password123"
        val error = AppError.ValidationError(field = "username", message = "Username already exists")
        
        viewModel.updateUsername(username)
        viewModel.updatePassword(password)
        
        whenever(registerUseCase(username, password)).thenReturn(Result.failure(error))
        
        // When
        viewModel.register()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val formState = viewModel.formState.first()
        assertEquals("Username already exists", formState.usernameError)
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
    fun `isFormValid returns false for short username`() {
        // Given
        viewModel.updateUsername("ab") // Less than 3 characters
        viewModel.updatePassword("validpassword")
        
        // When/Then
        assertFalse(viewModel.isFormValid())
    }
    
    @Test
    fun `isFormValid returns false for short password`() {
        // Given
        viewModel.updateUsername("validuser")
        viewModel.updatePassword("12345") // Less than 6 characters
        
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
        
        whenever(registerUseCase(username, password)).thenReturn(Result.success(user))
        
        // When
        viewModel.register()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertTrue(viewModel.uiState.first().isRegistrationSuccessful)
        
        // When
        viewModel.resetSuccessState()
        
        // Then
        assertFalse(viewModel.uiState.first().isRegistrationSuccessful)
    }
}