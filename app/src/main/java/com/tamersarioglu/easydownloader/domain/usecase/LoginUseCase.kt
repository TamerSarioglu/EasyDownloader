package com.tamersarioglu.easydownloader.domain.usecase

import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.domain.model.User
import com.tamersarioglu.easydownloader.domain.repository.VideoDownloaderRepository
import com.tamersarioglu.easydownloader.domain.usecase.auth.LoginParams
import com.tamersarioglu.easydownloader.domain.usecase.base.BaseUseCase
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: VideoDownloaderRepository
) : BaseUseCase<LoginParams, User>() {
    
    override suspend fun execute(parameters: LoginParams): Result<User> {
        val validationResult = validateCredentials(parameters.username, parameters.password)
        if (validationResult.isFailure) {
            return validationResult
        }
        
        return repository.login(parameters.username.trim(), parameters.password)
    }
    
    suspend operator fun invoke(username: String, password: String): Result<User> {
        return invoke(LoginParams(username, password))
    }
    
    private fun validateCredentials(username: String, password: String): Result<User> {
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
        
        return Result.success(User("", ""))
    }
    
    companion object {
        private const val MIN_USERNAME_LENGTH = 3
        private const val MIN_PASSWORD_LENGTH = 6
    }
}