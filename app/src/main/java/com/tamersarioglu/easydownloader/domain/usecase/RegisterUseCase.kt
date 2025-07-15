package com.tamersarioglu.easydownloader.domain.usecase

import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.domain.model.User
import com.tamersarioglu.easydownloader.domain.repository.VideoDownloaderRepository
import com.tamersarioglu.easydownloader.domain.usecase.auth.RegisterParams
import com.tamersarioglu.easydownloader.domain.usecase.base.BaseUseCase
import javax.inject.Inject

/**
 * Use case for user registration with input validation.
 * 
 * This use case handles the business logic for user registration, including:
 * - Input validation for username and password
 * - Calling the repository to register the user
 * - Proper error handling and user feedback
 * 
 * Requirements covered: 1.2, 1.4, 1.5
 */
class RegisterUseCase @Inject constructor(
    private val repository: VideoDownloaderRepository
) : BaseUseCase<RegisterParams, User>() {
    
    /**
     * Registers a new user with input validation.
     * 
     * @param parameters RegisterParams containing username and password
     * @return Result<User> containing the registered user on success, or error on failure
     */
    override suspend fun execute(parameters: RegisterParams): Result<User> {
        // Validate input parameters
        val validationResult = validateInput(parameters.username, parameters.password)
        if (validationResult.isFailure) {
            return validationResult
        }
        
        // Call repository to register user
        return repository.register(parameters.username.trim(), parameters.password)
    }
    
    /**
     * Convenience method for direct parameter passing (maintains backward compatibility).
     */
    suspend operator fun invoke(username: String, password: String): Result<User> {
        return invoke(RegisterParams(username, password))
    }
    
    /**
     * Validates the registration input parameters.
     * 
     * @param username The username to validate
     * @param password The password to validate
     * @return Result<User> with validation error if invalid, or success if valid
     */
    private fun validateInput(username: String, password: String): Result<User> {
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
                        "Username must be at least $MIN_USERNAME_LENGTH characters"
                    )
                )
            }
            trimmedUsername.length > MAX_USERNAME_LENGTH -> {
                return Result.failure(
                    AppError.ValidationError(
                        "username", 
                        "Username cannot exceed $MAX_USERNAME_LENGTH characters"
                    )
                )
            }
            !isValidUsernameFormat(trimmedUsername) -> {
                return Result.failure(
                    AppError.ValidationError(
                        "username", 
                        "Username can only contain letters, numbers, and underscores"
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
                        "Password must be at least $MIN_PASSWORD_LENGTH characters"
                    )
                )
            }
            password.length > MAX_PASSWORD_LENGTH -> {
                return Result.failure(
                    AppError.ValidationError(
                        "password", 
                        "Password cannot exceed $MAX_PASSWORD_LENGTH characters"
                    )
                )
            }
            !isValidPasswordFormat(password) -> {
                return Result.failure(
                    AppError.ValidationError(
                        "password", 
                        "Password must contain at least one letter and one number"
                    )
                )
            }
        }
        
        // If we reach here, validation passed
        return Result.success(User("", "")) // Dummy user for validation success
    }
    
    /**
     * Validates username format (alphanumeric and underscores only).
     */
    private fun isValidUsernameFormat(username: String): Boolean {
        return username.matches(Regex("^[a-zA-Z0-9_]+$"))
    }
    
    /**
     * Validates password format (must contain at least one letter and one number).
     */
    private fun isValidPasswordFormat(password: String): Boolean {
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        return hasLetter && hasDigit
    }
    
    companion object {
        private const val MIN_USERNAME_LENGTH = 3
        private const val MAX_USERNAME_LENGTH = 30
        private const val MIN_PASSWORD_LENGTH = 6
        private const val MAX_PASSWORD_LENGTH = 128
    }
}