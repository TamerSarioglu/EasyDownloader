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
                        popUpTo(AuthRoutes.LOGIN) { inclusive = false }
                    }
                }
            )
        }
    }
}