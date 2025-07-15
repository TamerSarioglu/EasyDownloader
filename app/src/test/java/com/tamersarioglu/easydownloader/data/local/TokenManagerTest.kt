package com.tamersarioglu.easydownloader.data.local

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for TokenManager
 * Tests token storage, retrieval, and clearing functionality
 * 
 * Note: These are basic structural tests. Full integration tests would require
 * a test DataStore instance, which is more complex to set up.
 */
class TokenManagerTest {
    
    @Test
    fun `TokenManager class should exist and be properly structured`() {
        // This test verifies that the TokenManager class exists and has the expected structure
        val tokenManagerClass = TokenManager::class.java
        
        // Verify class exists
        assertNotNull(tokenManagerClass)
        
        // Verify it has the expected methods
        val methods = tokenManagerClass.declaredMethods.map { it.name }
        assertTrue("saveAuthToken method should exist", methods.contains("saveAuthToken"))
        assertTrue("getAuthToken method should exist", methods.contains("getAuthToken"))
        assertTrue("getAuthTokenFlow method should exist", methods.contains("getAuthTokenFlow"))
        assertTrue("clearAuthToken method should exist", methods.contains("clearAuthToken"))
        assertTrue("isAuthenticated method should exist", methods.contains("isAuthenticated"))
    }
    
    @Test
    fun `TokenManager should have proper annotations`() {
        val tokenManagerClass = TokenManager::class.java
        
        // Verify @Singleton annotation exists
        val annotations = tokenManagerClass.annotations.map { it.annotationClass.simpleName }
        assertTrue("TokenManager should have @Singleton annotation", annotations.contains("Singleton"))
    }
    
    @Test
    fun `TokenManager constructor should accept DataStore parameter`() {
        val tokenManagerClass = TokenManager::class.java
        val constructors = tokenManagerClass.constructors
        
        assertTrue("TokenManager should have at least one constructor", constructors.isNotEmpty())
        
        val constructor = constructors[0]
        val parameterTypes = constructor.parameterTypes
        
        assertTrue("Constructor should have at least one parameter", parameterTypes.isNotEmpty())
        assertTrue("Constructor should accept DataStore parameter", 
            parameterTypes.any { it.simpleName.contains("DataStore") })
    }
}