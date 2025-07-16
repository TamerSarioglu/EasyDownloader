package com.tamersarioglu.easydownloader.data.remote.interceptor

import com.tamersarioglu.easydownloader.data.local.TokenManager
import com.tamersarioglu.easydownloader.data.remote.config.NetworkConfig
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        val requiresAuth = NetworkConfig.EXCLUDED_AUTH_ENDPOINTS.none { endpoint ->
            originalRequest.url.pathSegments.contains(endpoint)
        }
        
        if (!requiresAuth) {
            return chain.proceed(originalRequest)
        }
        
        val token = runBlocking { tokenManager.getAuthToken() }
        
        return if (token != null) {
            val authenticatedRequest = originalRequest.newBuilder()
                .header(NetworkConfig.AUTHORIZATION_HEADER, "${NetworkConfig.BEARER_PREFIX}$token")
                .build()
            chain.proceed(authenticatedRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}