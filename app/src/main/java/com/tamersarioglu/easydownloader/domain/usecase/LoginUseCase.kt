package com.tamersarioglu.easydownloader.domain.usecase

import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.domain.model.User
import com.tamersarioglu.easydownloader.domain.repository.VideoDownloaderRepository
import com.tamersarioglu.easydownloader.domain.usecase.auth.LoginParams
import com.tamersarioglu.easydownloader.domain.usecase.base.BaseUseCase
import javax.inject.Inject

/**
 * Use case for user login with credential validation.
 * 
 * This use case handles the business logic for user authentication, including:
 * - Input validation for username and password
 * - Calling the repository to authenticate the user
 * - Proper error handling and user feedback
 * 
 * Requirements covered: 2.2, 2.4
 */
class LoginUseCase @Inject constructor(
    private val repository: VideoDownloaderRepository
) : BaseUseCase<LoginParams, User>() {
    
    /**
     * Authenticates a user with credential validation.
     * 
     * @param parameters LoginParams containing username and password
     * @return Result<User> containing the authenticated user on success, or error on failure
     */
    override suspend fun execute(parameters: LoginParams): Result<User> {
        // Validate input parameters
        val validationResult = validateCredentials(parameters.username, parameters.password)
        if (validationResult.isFailure) {
            return validationResult
        }
        
        // Call repository to authenticate user
        return repository.login(parameters.username.trim(), parameters.password)
    }
    
    /**
     * Convenience method for direct parameter passing (maintains backward compatibility).
     */
    suspend operator fun invoke(username: String, password: String): Result<User> {
        return invoke(LoginParams(username, password))
    }
    
    /**
     * Validates the login credentials.
     * 
     * @param username The username to validate
     * @param password The password to validate
     * @return Result<User> with validation error if invalid, or success if valid
     */
    private fun validateCredentials(username: String, password: String): Result<User> {
        // Validate username
        val trimmedUsername = username.trim()
        when {
            trimmedUsername.isEmpty() -> {
                return Result.failure(
                    AppError.ValidationError("username", "Username cannot be empty")
                )
            }
            trimmedUsername.length < MIN_USERNAME_LENGTH -> {
                return Result.failure(
                    AppError.ValidationError(
                        "username", 
                        "Please enter a valid username"
                    )
                )
            }
        }
        
        // Validate password
        when {
            password.isEmpty() -> {
                return Result.failure(
                    AppError.ValidationError("password", "Password cannot be empty")
                )
            }
            password.length < MIN_PASSWORD_LENGTH -> {
                return Result.failure(
                    AppError.ValidationError(
                        "password", 
                        "Please enter a valid password"
                    )
                )
            }
        }
        
        // If we reach here, validation passed
        return Result.success(User("", "")) // Dummy user for validation success
    }
    
    companion object {
        private const val MIN_USERNAME_LENGTH = 3
        private const val MIN_PASSWORD_LENGTH = 6
    }
}