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
                navController.navigate(Routes.LOGIN) {
                    launchSingleTop = true
                }
            },
            onRegistrationSuccess = onAuthenticationSuccess
        )
    }
    
    composable(Routes.LOGIN) {
        LoginScreen(
            onNavigateToRegistration = {
                if (navController.previousBackStackEntry?.destination?.route == Routes.REGISTRATION) {
                    navController.popBackStack()
                } else {
                    navController.navigate(Routes.REGISTRATION) {
                        launchSingleTop = true
                    }
                }
            },
            onLoginSuccess = onAuthenticationSuccess
        )
    }
}