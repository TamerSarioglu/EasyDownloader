package com.tamersarioglu.easydownloader.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.tamersarioglu.easydownloader.presentation.auth.AuthViewModel
import com.tamersarioglu.easydownloader.presentation.screens.AuthenticatedContent
import com.tamersarioglu.easydownloader.presentation.screens.UnauthenticatedContent

/**
 * Main app composable that handles authentication state persistence and navigation.
 * 
 * This composable:
 * - Checks for existing valid tokens on app startup
 * - Implements automatic login for returning users
 * - Handles token expiration and logout scenarios
 * - Shows appropriate content based on authentication state
 * 
 * Requirements: 2.5, 2.6, 5.2, 5.3
 */
@Composable
fun EasyDownloaderApp(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authUiState by authViewModel.uiState.collectAsState()
    
    // Handle authentication state persistence on app startup
    LaunchedEffect(Unit) {
        // The AuthViewModel automatically checks authentication status in its init block
        // This LaunchedEffect ensures the check happens when the composable is first created
        // The ViewModel's checkAuthenticationStatus() is already called in init, so no additional call needed
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        when {
            // Show loading while checking authentication status
            authUiState.isLoading -> {
                LoadingScreen(modifier = Modifier.padding(innerPadding))
            }
            
            // User is authenticated - show main app content
            authUiState.isLoggedIn -> {
                AuthenticatedContent(
                    modifier = modifier.padding(innerPadding),
                    onLogout = { authViewModel.logout() }
                )
            }
            
            // User is not authenticated - show login/registration screens
            else -> {
                UnauthenticatedContent(
                    modifier = modifier.padding(innerPadding),
                    authViewModel = authViewModel
                )
            }
        }
    }
}

/**
 * Loading screen shown while checking authentication status on app startup
 */
@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EasyDownloaderAppPreview() {
    EasyDownloaderApp()
}