package com.tamersarioglu.easydownloader.data.remote.interceptor

import com.tamersarioglu.easydownloader.data.remote.config.NetworkConfig
import okhttp3.Interceptor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test


class AuthInterceptorTest {
    
    @Test
    fun `AuthInterceptor class should exist and implement Interceptor`() {
        val authInterceptorClass = AuthInterceptor::class.java
        
        assertNotNull(authInterceptorClass)
        
        val interfaces = authInterceptorClass.interfaces
        assertTrue("AuthInterceptor should implement Interceptor interface", 
            interfaces.any { it == Interceptor::class.java })
    }
    
    @Test
    fun `AuthInterceptor should have proper annotations`() {
        val authInterceptorClass = AuthInterceptor::class.java
        val annotations = authInterceptorClass.annotations.map { it.annotationClass.simpleName }
        assertTrue("AuthInterceptor should have @Singleton annotation", annotations.contains("Singleton"))
    }
    
    @Test
    fun `AuthInterceptor constructor should accept TokenManager parameter`() {
        val authInterceptorClass = AuthInterceptor::class.java
        val constructors = authInterceptorClass.constructors
        
        assertTrue("AuthInterceptor should have at least one constructor", constructors.isNotEmpty())
        
        val constructor = constructors[0]
        val parameterTypes = constructor.parameterTypes
        
        assertTrue("Constructor should have at least one parameter", parameterTypes.isNotEmpty())
        assertTrue("Constructor should accept TokenManager parameter", 
            parameterTypes.any { it.simpleName == "TokenManager" })
    }
    
    @Test
    fun `AuthInterceptor should have intercept method`() {
        val authInterceptorClass = AuthInterceptor::class.java
        val methods = authInterceptorClass.declaredMethods.map { it.name }
        
        assertTrue("AuthInterceptor should have intercept method", methods.contains("intercept"))
    }
    
    @Test
    fun `NetworkConfig should have proper excluded endpoints`() {
        val excludedEndpoints = NetworkConfig.EXCLUDED_AUTH_ENDPOINTS
        
        assertNotNull("Excluded endpoints should not be null", excludedEndpoints)
        assertTrue("Should exclude register endpoint", excludedEndpoints.contains("register"))
        assertTrue("Should exclude login endpoint", excludedEndpoints.contains("login"))
    }
    
    @Test
    fun `NetworkConfig should have proper header constants`() {
        assertEquals("Authorization header should be correct", "Authorization", NetworkConfig.AUTHORIZATION_HEADER)
        assertEquals("Bearer prefix should be correct", "Bearer ", NetworkConfig.BEARER_PREFIX)
    }
}
