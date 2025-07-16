package com.tamersarioglu.easydownloader.data.remote.interceptor

import com.tamersarioglu.easydownloader.data.remote.config.NetworkConfig
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetryInterceptor @Inject constructor() : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        
        for (attempt in 1..NetworkConfig.MAX_RETRY_ATTEMPTS) {
            try {
                response?.close()
                response = chain.proceed(request)
                
                if (response.isSuccessful || response.code in 400..499) {
                    return response
                }
                
                if (attempt < NetworkConfig.MAX_RETRY_ATTEMPTS) {
                    response.close()
                    Thread.sleep(NetworkConfig.RETRY_DELAY_MS * attempt)
                }
                
            } catch (e: IOException) {
                exception = e
                
                if (shouldRetry(e) && attempt < NetworkConfig.MAX_RETRY_ATTEMPTS) {
                    Thread.sleep(NetworkConfig.RETRY_DELAY_MS * attempt)
                    continue
                } else {
                    throw e
                }
            }
        }
        
        return response ?: throw (exception ?: IOException("Unknown network error"))
    }
    
    private fun shouldRetry(exception: IOException): Boolean {
        return when (exception) {
            is SocketTimeoutException -> true
            is ConnectException -> true
            is UnknownHostException -> false
            else -> false
        }
    }
}