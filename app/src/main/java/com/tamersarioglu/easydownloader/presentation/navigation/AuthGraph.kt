package com.tamersarioglu.easydownloader.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.tamersarioglu.easydownloader.presentation.auth.LoginScreen
import com.tamersarioglu.easydownloader.presentation.auth.RegistrationScreen

fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    onAuthenticationSuccess: () -> Unit
) {
    composable(Routes.REGISTRATION) {
        RegistrationScreen(
            onNavigateToLogin = {
                // Navigate to login with proper back stack management
                navController.navigate(Routes.LOGIN) {
                    // Don't create multiple instances of login screen
                    launchSingleTop = true
                }
            },
            onRegistrationSuccess = onAuthenticationSuccess
        )
    }
    
    composable(Routes.LOGIN) {
        LoginScreen(
            onNavigateToRegistration = {
                // Navigate back to registration or pop back stack if coming from registration
                if (navController.previousBackStackEntry?.destination?.route == Routes.REGISTRATION) {
                    navController.popBackStack()
                } else {
                    navController.navigate(Routes.REGISTRATION) {
                        // Don't create multiple instances of registration screen
                        launchSingleTop = true
                    }
                }
            },
            onLoginSuccess = onAuthenticationSuccess
        )
    }
}