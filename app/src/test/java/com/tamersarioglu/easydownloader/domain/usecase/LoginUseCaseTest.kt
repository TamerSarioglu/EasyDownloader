package com.tamersarioglu.easydownloader.domain.usecase

import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.domain.model.User
import com.tamersarioglu.easydownloader.domain.repository.VideoDownloaderRepository
import com.tamersarioglu.easydownloader.domain.usecase.auth.LoginParams
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class LoginUseCaseTest {
    
    @Mock
    private lateinit var repository: VideoDownloaderRepository
    private lateinit var loginUseCase: LoginUseCase
    
    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        loginUseCase = LoginUseCase(repository)
    }
    
    @Test
    fun `invoke with valid credentials should return success`() = runTest {

        val username = "testuser"
        val password = "password123"
        val expectedUser = User(username, "jwt_token")
        `when`(repository.login(username, password)).thenReturn(Result.success(expectedUser))
        
        val result = loginUseCase(username, password)
        
        assertTrue(result.isSuccess)
        assertEquals(expectedUser, result.getOrNull())
    }
    
    @Test
    fun `invoke with empty username should return validation error`() = runTest {

        val username = ""
        val password = "password123"
        

        val result = loginUseCase(username, password)
        

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as AppError.ValidationError
        assertEquals("username", error.field)
        assertEquals("Username cannot be empty", error.message)
    }
    
    @Test
    fun `invoke with whitespace-only username should return validation error`() = runTest {

        val username = "   "
        val password = "password123"
        

        val result = loginUseCase(username, password)
        

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as AppError.ValidationError
        assertEquals("username", error.field)
        assertEquals("Username cannot be empty", error.message)
    }
    
    @Test
    fun `invoke with short username should return validation error`() = runTest {

        val username = "ab"
        val password = "password123"
        

        val result = loginUseCase(username, password)
        

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as AppError.ValidationError
        assertEquals("username", error.field)
        assertEquals("Please enter a valid username", error.message)
    }
    
    @Test
    fun `invoke with empty password should return validation error`() = runTest {

        val username = "testuser"
        val password = ""
        

        val result = loginUseCase(username, password)
        

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as AppError.ValidationError
        assertEquals("password", error.field)
        assertEquals("Password cannot be empty", error.message)
    }
    
    @Test
    fun `invoke with short password should return validation error`() = runTest {

        val username = "testuser"
        val password = "12345" // 5 characters, less than minimum 6
        

        val result = loginUseCase(username, password)
        

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as AppError.ValidationError
        assertEquals("password", error.field)
        assertEquals("Please enter a valid password", error.message)
    }
    
    @Test
    fun `invoke with LoginParams should work correctly`() = runTest {

        val params = LoginParams("testuser", "password123")
        val expectedUser = User("testuser", "jwt_token")
        `when`(repository.login("testuser", "password123")).thenReturn(Result.success(expectedUser))
        

        val result = loginUseCase(params)
        

        assertTrue(result.isSuccess)
        assertEquals(expectedUser, result.getOrNull())
    }
    
    @Test
    fun `invoke should trim username whitespace`() = runTest {

        val username = "  testuser  "
        val password = "password123"
        val expectedUser = User("testuser", "jwt_token")
        `when`(repository.login("testuser", "password123")).thenReturn(Result.success(expectedUser))
        

        val result = loginUseCase(username, password)
        

        assertTrue(result.isSuccess)
        assertEquals(expectedUser, result.getOrNull())
    }
    
    @Test
    fun `invoke with repository error should return repository error`() = runTest {

        val username = "testuser"
        val password = "wrongpassword"
        val apiError = AppError.ApiError("INVALID_CREDENTIALS", "Invalid username or password")
        `when`(repository.login("testuser", "wrongpassword")).thenReturn(Result.failure(apiError))
        

        val result = loginUseCase(username, password)
        

        assertTrue(result.isFailure)
        assertEquals(apiError, result.exceptionOrNull())
    }
    
    @Test
    fun `invoke with network error should return network error`() = runTest {

        val username = "testuser"
        val password = "password123"
        val networkError = AppError.NetworkError
        `when`(repository.login("testuser", "password123")).thenReturn(Result.failure(networkError))
        

        val result = loginUseCase(username, password)
        

        assertTrue(result.isFailure)
        assertEquals(networkError, result.exceptionOrNull())
    }
    
    @Test
    fun `invoke with unauthorized error should return unauthorized error`() = runTest {

        val username = "testuser"
        val password = "password123"
        val unauthorizedError = AppError.UnauthorizedError
        `when`(repository.login("testuser", "password123")).thenReturn(Result.failure(unauthorizedError))
        

        val result = loginUseCase(username, password)
        

        assertTrue(result.isFailure)
        assertEquals(unauthorizedError, result.exceptionOrNull())
    }
}
