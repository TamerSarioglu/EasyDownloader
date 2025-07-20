package com.tamersarioglu.easydownloader.data.local

import org.junit.Test
import org.junit.Assert.*


class TokenManagerTest {
    
    @Test
    fun `TokenManager class should exist and be properly structured`() {
        val tokenManagerClass = TokenManager::class.java
        
        assertNotNull(tokenManagerClass)
        
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
