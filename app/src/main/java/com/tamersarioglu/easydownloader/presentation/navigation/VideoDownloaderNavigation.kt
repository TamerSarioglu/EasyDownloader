package com.tamersarioglu.easydownloader.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.tamersarioglu.easydownloader.presentation.auth.AuthViewModel

@Composable
fun VideoDownloaderNavigation(
        modifier: Modifier = Modifier,
        navController: NavHostController = rememberNavController(),
        authViewModel: AuthViewModel = hiltViewModel()
) {
    val authUiState by authViewModel.uiState.collectAsState()

    // Determine start destination based on authentication state
    val startDestination =
            if (authUiState.isLoggedIn) {
                Routes.MAIN_GRAPH
            } else {
                Routes.AUTH_GRAPH
            }

    // Handle authentication state changes for navigation
    LaunchedEffect(authUiState.isLoggedIn) {
        if (authUiState.isLoggedIn) {
            // User just logged in - navigate to main graph and clear auth back stack
            if (navController.currentDestination?.route?.contains(Routes.AUTH_GRAPH) == true) {
                navController.navigate(Routes.MAIN_GRAPH) {
                    popUpTo(Routes.AUTH_GRAPH) { inclusive = true }
                }
            }
        } else {
            // User logged out or token expired - navigate to auth graph and clear main back stack
            if (navController.currentDestination?.route?.contains(Routes.MAIN_GRAPH) == true) {
                navController.navigate(Routes.AUTH_GRAPH) {
                    popUpTo(Routes.MAIN_GRAPH) { inclusive = true }
                }
            }
        }
    }

    NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
    ) {
        // Authentication navigation graph
        navigation(startDestination = Routes.REGISTRATION, route = Routes.AUTH_GRAPH) {
            authGraph(
                    navController = navController,
                    authViewModel = authViewModel,
                    onAuthenticationSuccess = {
                        // Authentication success is handled by LaunchedEffect above
                        // This ensures consistent navigation behavior
                    }
            )
        }

        // Main application navigation graph
        navigation(startDestination = Routes.VIDEO_SUBMISSION, route = Routes.MAIN_GRAPH) {
            mainGraph(
                    navController = navController,
                    onLogout = {
                        authViewModel.logout()
                        // Logout navigation is handled by LaunchedEffect above
                        // This ensures proper back stack management
                    }
            )
        }
    }
}
