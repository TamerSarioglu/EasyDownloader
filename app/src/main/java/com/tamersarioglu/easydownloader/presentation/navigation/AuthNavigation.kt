package com.tamersarioglu.easydownloader.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tamersarioglu.easydownloader.presentation.auth.AuthViewModel
import com.tamersarioglu.easydownloader.presentation.auth.LoginScreen
import com.tamersarioglu.easydownloader.presentation.auth.RegistrationScreen

/**
 * Navigation routes for authentication screens
 */
object AuthRoutes {
    const val REGISTRATION = "registration"
    const val LOGIN = "login"
}

/**
 * Navigation graph for authentication screens (login and registration).
 * 
 * This composable handles navigation between login and registration screens
 * while sharing the same AuthViewModel instance to maintain state consistency.
 * 
 * Requirements: 1.1, 2.1, 7.5 (navigation between authentication screens)
 */
@Composable
fun AuthNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    NavHost(
        navController = navController,
        startDestination = AuthRoutes.REGISTRATION,
        modifier = modifier
    ) {
        composable(AuthRoutes.REGISTRATION) {
            RegistrationScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(AuthRoutes.LOGIN) {
                        // Clear the back stack to prevent going back to registration
                        // when user presses back from login
                        popUpTo(AuthRoutes.REGISTRATION) { inclusive = false }
                    }
                }
            )
        }
        
        composable(AuthRoutes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegistration = {
                    navController.navigate(AuthRoutes.REGISTRATION) {
                        // Clear the back stack to prevent going back to login
                        // when user presses back from registration
                        popUpTo(AuthRoutes.LOGIN) { inclusive = false }
                    }
                }
            )
        }
    }
}