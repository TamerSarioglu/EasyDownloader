package com.tamersarioglu.easydownloader.domain.usecase

import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.domain.model.User
import com.tamersarioglu.easydownloader.domain.repository.VideoDownloaderRepository
import com.tamersarioglu.easydownloader.domain.usecase.auth.RegisterParams
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class RegisterUseCaseTest {
    
    @Mock
    private lateinit var repository: VideoDownloaderRepository
    private lateinit var registerUseCase: RegisterUseCase
    
    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        registerUseCase = RegisterUseCase(repository)
    }
    
    @Test
    fun `invoke with valid credentials should return success`() = runTest {
        // Given
        val username = "testuser"
        val password = "password123"
        val expectedUser = User(username, "jwt_token")
        `when`(repository.register(username, password)).thenReturn(Result.success(expectedUser))
        
        // When
        val result = registerUseCase(username, password)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedUser, result.getOrNull())
    }
    
    @Test
    fun `invoke with empty username should return validation error`() = runTest {
        // Given
        val username = ""
        val password = "password123"
        
        // When
        val result = registerUseCase(username, password)
        
        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as AppError.ValidationError
        assertEquals("username", error.field)
        assertEquals("Username cannot be empty", error.message)
    }
    
    @Test
    fun `invoke with whitespace-only username should return validation error`() = runTest {
        // Given
        val username = "   "
        val password = "password123"
        
        // When
        val result = registerUseCase(username, password)
        
        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as AppError.ValidationError
        assertEquals("username", error.field)
        assertEquals("Username cannot be empty", error.message)
    }
    
    @Test
    fun `invoke with short username should return validation error`() = runTest {
        // Given
        val username = "ab"
        val password = "password123"
        
        // When
        val result = registerUseCase(username, password)
        
        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as AppError.ValidationError
        assertEquals("username", error.field)
        assertEquals("Username must be at least 3 characters", error.message)
    }
    
    @Test
    fun `invoke with long username should return validation error`() = runTest {
        // Given
        val username = "a".repeat(31) // 31 characters, exceeds max of 30
        val password = "password123"
        
        // When
        val result = registerUseCase(username, password)
        
        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as AppError.ValidationError
        assertEquals("username", error.field)
        assertEquals("Username cannot exceed 30 characters", error.message)
    }
    
    @Test
    fun `invoke with invalid username format should return validation error`() = runTest {
        // Given
        val username = "test@user" // Contains invalid character
        val password = "password123"
        
        // When
        val result = registerUseCase(username, password)
        
        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as AppError.ValidationError
        assertEquals("username", error.field)
        assertEquals("Username can only contain letters, numbers, and underscores", error.message)
    }
    
    @Test
    fun `invoke with empty password should return validation error`() = runTest {
        // Given
        val username = "testuser"
        val password = ""
        
        // When
        val result = registerUseCase(username, password)
        
        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as AppError.ValidationError
        assertEquals("password", error.field)
        assertEquals("Password cannot be empty", error.message)
    }
    
    @Test
    fun `invoke with short password should return validation error`() = runTest {
        // Given
        val username = "testuser"
        val password = "12345" // 5 characters, less than minimum 6
        
        // When
        val result = registerUseCase(username, password)
        
        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as AppError.ValidationError
        assertEquals("password", error.field)
        assertEquals("Password must be at least 6 characters", error.message)
    }
    
    @Test
    fun `invoke with long password should return validation error`() = runTest {
        // Given
        val username = "testuser"
        val password = "a".repeat(129) // 129 characters, exceeds max of 128
        
        // When
        val result = registerUseCase(username, password)
        
        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as AppError.ValidationError
        assertEquals("password", error.field)
        assertEquals("Password cannot exceed 128 characters", error.message)
    }
    
    @Test
    fun `invoke with password without letters should return validation error`() = runTest {
        // Given
        val username = "testuser"
        val password = "123456" // Only numbers
        
        // When
        val result = registerUseCase(username, password)
        
        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as AppError.ValidationError
        assertEquals("password", error.field)
        assertEquals("Password must contain at least one letter and one number", error.message)
    }
    
    @Test
    fun `invoke with password without numbers should return validation error`() = runTest {
        // Given
        val username = "testuser"
        val password = "password" // Only letters
        
        // When
        val result = registerUseCase(username, password)
        
        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as AppError.ValidationError
        assertEquals("password", error.field)
        assertEquals("Password must contain at least one letter and one number", error.message)
    }
    
    @Test
    fun `invoke with RegisterParams should work correctly`() = runTest {
        // Given
        val params = RegisterParams("testuser", "password123")
        val expectedUser = User("testuser", "jwt_token")
        `when`(repository.register("testuser", "password123")).thenReturn(Result.success(expectedUser))
        
        // When
        val result = registerUseCase(params)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedUser, result.getOrNull())
    }
    
    @Test
    fun `invoke should trim username whitespace`() = runTest {
        // Given
        val username = "  testuser  "
        val password = "password123"
        val expectedUser = User("testuser", "jwt_token")
        `when`(repository.register("testuser", "password123")).thenReturn(Result.success(expectedUser))
        
        // When
        val result = registerUseCase(username, password)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedUser, result.getOrNull())
    }
    
    @Test
    fun `invoke with repository error should return repository error`() = runTest {
        // Given
        val username = "testuser"
        val password = "password123"
        val apiError = AppError.ApiError("USER_EXISTS", "Username already exists")
        `when`(repository.register("testuser", "password123")).thenReturn(Result.failure(apiError))
        
        // When
        val result = registerUseCase(username, password)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(apiError, result.exceptionOrNull())
    }
}