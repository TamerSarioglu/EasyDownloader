package com.tamersarioglu.easydownloader.domain.usecase

import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.domain.model.User
import com.tamersarioglu.easydownloader.domain.repository.VideoDownloaderRepository
import com.tamersarioglu.easydownloader.domain.usecase.auth.RegisterParams
import com.tamersarioglu.easydownloader.domain.usecase.base.BaseUseCase
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: VideoDownloaderRepository
) : BaseUseCase<RegisterParams, User>() {
    
    override suspend fun execute(parameters: RegisterParams): Result<User> {
        val validationResult = validateInput(parameters.username, parameters.password)
        if (validationResult.isFailure) {
            return validationResult
        }
        
        return repository.register(parameters.username.trim(), parameters.password)
    }
    
    suspend operator fun invoke(username: String, password: String): Result<User> {
        return invoke(RegisterParams(username, password))
    }
    
    private fun validateInput(username: String, password: String): Result<User> {
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
        
        return Result.success(User("", ""))
    }
    
    private fun isValidUsernameFormat(username: String): Boolean {
        return username.matches(Regex("^[a-zA-Z0-9_]+$"))
    }
    
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