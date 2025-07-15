package com.tamersarioglu.easydownloader.data.remote.api

import com.tamersarioglu.easydownloader.data.remote.dto.ErrorResponse
import com.tamersarioglu.easydownloader.data.remote.dto.ValidationErrorResponse
import kotlinx.serialization.json.Json
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Utility class for handling API errors and converting them to appropriate exceptions
 */
object ApiErrorHandler {
    
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    
    /**
     * Handles API call execution and converts errors to ApiResult
     */
    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): ApiResult<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ApiResult.Success(body)
                } else {
                    ApiResult.Error(ApiException.UnknownError)
                }
            } else {
                val error = parseErrorResponse(response)
                ApiResult.Error(error)
            }
        } catch (exception: Exception) {
            val apiException = when (exception) {
                is UnknownHostException -> ApiException.NetworkError
                is SocketTimeoutException -> ApiException.TimeoutError
                is IOException -> ApiException.NetworkError
                else -> ApiException.UnknownError
            }
            ApiResult.Error(apiException)
        }
    }
    
    /**
     * Parses error response body and converts to appropriate ApiException
     */
    private fun <T> parseErrorResponse(response: Response<T>): ApiException {
        val errorBody = response.errorBody()?.string()
        
        return when (response.code()) {
            401 -> ApiException.UnauthorizedError
            in 400..499 -> parseClientError(errorBody)
            in 500..599 -> parseServerError(errorBody)
            else -> ApiException.HttpError(response.code(), errorBody)
        }
    }
    
    /**
     * Parses client error responses (4xx)
     */
    private fun parseClientError(errorBody: String?): ApiException {
        if (errorBody.isNullOrBlank()) {
            return ApiException.HttpError(400, "Bad Request")
        }
        
        return try {
            // Try to parse as validation error first
            val validationError = json.decodeFromString<ValidationErrorResponse>(errorBody)
            val firstError = validationError.validationErrors?.entries?.firstOrNull()
            if (firstError != null) {
                ApiException.ValidationError(
                    field = firstError.key,
                    message = firstError.value.firstOrNull() ?: "Validation failed"
                )
            } else {
                ApiException.ServerError(validationError.message, null)
            }
        } catch (e: Exception) {
            try {
                // Try to parse as general error
                val errorResponse = json.decodeFromString<ErrorResponse>(errorBody)
                ApiException.ServerError(errorResponse.message, errorResponse.code)
            } catch (e: Exception) {
                ApiException.HttpError(400, errorBody)
            }
        }
    }
    
    /**
     * Parses server error responses (5xx)
     */
    private fun parseServerError(errorBody: String?): ApiException {
        if (errorBody.isNullOrBlank()) {
            return ApiException.ServerError("Internal server error", "500")
        }
        
        return try {
            val errorResponse = json.decodeFromString<ErrorResponse>(errorBody)
            ApiException.ServerError(errorResponse.message, errorResponse.code)
        } catch (e: Exception) {
            ApiException.ServerError("Server error occurred", "500")
        }
    }
}