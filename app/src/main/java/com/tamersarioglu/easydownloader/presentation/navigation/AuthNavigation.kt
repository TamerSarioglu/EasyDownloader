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

object AuthRoutes {
    const val REGISTRATION = "registration"
    const val LOGIN = "login"
}

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
                        // Don't create multiple instances of login screen
                        launchSingleTop = true
                    }
                }
            )
        }
        
        composable(AuthRoutes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegistration = {
                    // Navigate back to registration or pop back stack if coming from registration
                    if (navController.previousBackStackEntry?.destination?.route == AuthRoutes.REGISTRATION) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(AuthRoutes.REGISTRATION) {
                            // Don't create multiple instances of registration screen
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}