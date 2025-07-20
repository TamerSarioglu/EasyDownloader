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
import com.tamersarioglu.easydownloader.presentation.auth.AuthStateViewModel

@Composable
fun VideoDownloaderNavigation(
        modifier: Modifier = Modifier,
        navController: NavHostController = rememberNavController(),
        authStateViewModel: AuthStateViewModel = hiltViewModel()
) {
    val authState by authStateViewModel.authState.collectAsState()

    val startDestination =
            if (authState.isAuthenticated) {
                Routes.MAIN_GRAPH
            } else {
                Routes.AUTH_GRAPH
            }

    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            if (navController.currentDestination?.route?.contains(Routes.AUTH_GRAPH) == true) {
                navController.navigate(Routes.MAIN_GRAPH) {
                    popUpTo(Routes.AUTH_GRAPH) { inclusive = true }
                }
            }
        } else {
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
        navigation(startDestination = Routes.REGISTRATION, route = Routes.AUTH_GRAPH) {
            authGraph(
                    navController = navController,
                    onAuthenticationSuccess = {
                    }
            )
        }

        navigation(startDestination = Routes.VIDEO_SUBMISSION, route = Routes.MAIN_GRAPH) {
            mainGraph(
                    navController = navController,
                    onLogout = {
                        authStateViewModel.logout()
                    }
            )
        }
    }
}
