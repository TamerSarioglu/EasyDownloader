package com.tamersarioglu.easydownloader.presentation.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.tamersarioglu.easydownloader.presentation.auth.AuthViewModel
import com.tamersarioglu.easydownloader.presentation.auth.LoginScreen
import com.tamersarioglu.easydownloader.presentation.auth.RegistrationScreen
import com.tamersarioglu.easydownloader.presentation.video_list.VideoListScreen
import com.tamersarioglu.easydownloader.presentation.video_list.VideoListViewModel
import com.tamersarioglu.easydownloader.presentation.video_submission.VideoSubmissionScreen
import com.tamersarioglu.easydownloader.presentation.video_submission.VideoSubmissionViewModel

/**
 * Authentication navigation graph
 */
fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    onAuthenticationSuccess: () -> Unit
) {
    composable(Routes.REGISTRATION) {
        RegistrationScreen(
            authViewModel = authViewModel,
            onNavigateToLogin = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.REGISTRATION) { inclusive = false }
                }
            },
            onRegistrationSuccess = onAuthenticationSuccess
        )
    }
    
    composable(Routes.LOGIN) {
        LoginScreen(
            authViewModel = authViewModel,
            onNavigateToRegistration = {
                navController.navigate(Routes.REGISTRATION) {
                    popUpTo(Routes.LOGIN) { inclusive = false }
                }
            },
            onLoginSuccess = onAuthenticationSuccess
        )
    }
}

/**
 * Main application navigation graph
 */
fun NavGraphBuilder.mainGraph(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    composable(Routes.VIDEO_SUBMISSION) {
        val viewModel: VideoSubmissionViewModel = hiltViewModel()
        VideoSubmissionScreen(
            viewModel = viewModel,
            onNavigateToVideoList = {
                navController.navigate(Routes.VIDEO_LIST)
            },
            onNavigateToProfile = {
                navController.navigate(Routes.PROFILE)
            }
        )
    }
    
    composable(Routes.VIDEO_LIST) {
        val viewModel: VideoListViewModel = hiltViewModel()
        VideoListScreen(
            viewModel = viewModel,
            onNavigateToSubmission = {
                navController.navigate(Routes.VIDEO_SUBMISSION) {
                    popUpTo(Routes.VIDEO_SUBMISSION) { inclusive = true }
                }
            },
            onNavigateToProfile = {
                navController.navigate(Routes.PROFILE)
            }
        )
    }
    
    composable(Routes.PROFILE) {
        ProfileScreen(
            onLogout = onLogout,
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }
}