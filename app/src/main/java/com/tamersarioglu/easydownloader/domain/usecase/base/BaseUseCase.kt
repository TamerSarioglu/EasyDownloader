package com.tamersarioglu.easydownloader.domain.usecase.base

import com.tamersarioglu.easydownloader.domain.model.AppError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Base class for use cases providing common functionality.
 * 
 * This abstract class provides:
 * - Consistent error handling across all use cases
 * - Proper coroutine context switching
 * - Common validation utilities
 */
abstract class BaseUseCase<in P, R>(
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    
    /**
     * Executes the use case with proper error handling and context switching.
     * 
     * @param parameters The input parameters for the use case
     * @return Result<R> containing the result on success or error on failure
     */
    suspend operator fun invoke(parameters: P): Result<R> {
        return try {
            withContext(coroutineDispatcher) {
                execute(parameters)
            }
        } catch (e: Exception) {
            Result.failure(handleException(e))
        }
    }
    
    /**
     * Abstract method to be implemented by concrete use cases.
     * 
     * @param parameters The input parameters
     * @return Result<R> containing the result
     */
    @Throws(Exception::class)
    protected abstract suspend fun execute(parameters: P): Result<R>
    
    /**
     * Handles exceptions and converts them to appropriate AppError types.
     * 
     * @param exception The exception to handle
     * @return AppError representing the exception
     */
    protected open fun handleException(exception: Exception): AppError {
        return when (exception) {
            is AppError -> exception
            else -> AppError.NetworkError
        }
    }
}