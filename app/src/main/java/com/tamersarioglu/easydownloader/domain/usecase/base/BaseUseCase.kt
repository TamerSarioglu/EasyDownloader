package com.tamersarioglu.easydownloader.domain.usecase.base

import com.tamersarioglu.easydownloader.domain.model.AppError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BaseUseCase<in P, R>(
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    
    suspend operator fun invoke(parameters: P): Result<R> {
        return try {
            withContext(coroutineDispatcher) {
                execute(parameters)
            }
        } catch (e: Exception) {
            Result.failure(handleException(e))
        }
    }
    
    @Throws(Exception::class)
    protected abstract suspend fun execute(parameters: P): Result<R>
    
    protected open fun handleException(exception: Exception): AppError {
        return when (exception) {
            is AppError -> exception
            else -> AppError.NetworkError
        }
    }
}