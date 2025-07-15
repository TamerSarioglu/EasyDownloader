package com.tamersarioglu.easydownloader.data.remote.interceptor

import com.tamersarioglu.easydownloader.data.remote.config.NetworkConfig
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that implements retry logic for failed network requests
 * Retries requests on specific network failures with exponential backoff
 */
@Singleton
class RetryInterceptor @Inject constructor() : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        
        for (attempt in 1..NetworkConfig.MAX_RETRY_ATTEMPTS) {
            try {
                response?.close() // Close previous response if exists
                response = chain.proceed(request)
                
                // If successful or client error (4xx), don't retry
                if (response.isSuccessful || response.code in 400..499) {
                    return response
                }
                
                // Server errors (5xx) should be retried
                if (attempt < NetworkConfig.MAX_RETRY_ATTEMPTS) {
                    response.close()
                    Thread.sleep(NetworkConfig.RETRY_DELAY_MS * attempt) // Exponential backoff
                }
                
            } catch (e: IOException) {
                exception = e
                
                // Only retry on specific network exceptions
                if (shouldRetry(e) && attempt < NetworkConfig.MAX_RETRY_ATTEMPTS) {
                    Thread.sleep(NetworkConfig.RETRY_DELAY_MS * attempt) // Exponential backoff
                    continue
                } else {
                    throw e
                }
            }
        }
        
        // Return the last response or throw the last exception
        return response ?: throw (exception ?: IOException("Unknown network error"))
    }
    
    /**
     * Determines if a request should be retried based on the exception type
     */
    private fun shouldRetry(exception: IOException): Boolean {
        return when (exception) {
            is SocketTimeoutException -> true
            is java.net.ConnectException -> true
            is java.net.UnknownHostException -> false // Don't retry DNS failures
            else -> false
        }
    }
}