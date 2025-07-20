package com.tamersarioglu.easydownloader.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tamersarioglu.easydownloader.presentation.video_detail.VideoDetailScreen
import com.tamersarioglu.easydownloader.presentation.video_list.VideoListScreen
import com.tamersarioglu.easydownloader.presentation.video_submission.VideoSubmissionScreen
import com.tamersarioglu.easydownloader.presentation.settings.SettingsScreen

fun NavGraphBuilder.mainGraph(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    composable(Routes.VIDEO_SUBMISSION) {
        VideoSubmissionScreen(
            onNavigateToVideoList = {
                navController.navigate(Routes.VIDEO_LIST) {
                    launchSingleTop = true
                }
            },
            onNavigateToProfile = {
                navController.navigate(Routes.SETTINGS) {
                    launchSingleTop = true
                }
            }
        )
    }
    
    composable(Routes.VIDEO_LIST) {
        VideoListScreen(
            onNavigateToSubmission = {
                navController.navigate(Routes.VIDEO_SUBMISSION) {
                    launchSingleTop = true
                }
            },
            onNavigateToProfile = {
                navController.navigate(Routes.SETTINGS) {
                    launchSingleTop = true
                }
            }
        )
    }
    
    composable(
        route = Routes.VIDEO_DETAIL,
        arguments = listOf(
            navArgument("videoId") {
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        val videoId = backStackEntry.arguments?.getString("videoId") ?: ""
        VideoDetailScreen(
            videoId = videoId,
            onNavigateBack = {
                if (!navController.popBackStack()) {
                    navController.navigate(Routes.VIDEO_LIST) {
                        popUpTo(Routes.MAIN_GRAPH) { inclusive = false }
                    }
                }
            }
        )
    }
    
    composable(Routes.SETTINGS) {
        SettingsScreen(
            onNavigateBack = {
                if (!navController.popBackStack()) {
                    navController.navigate(Routes.VIDEO_LIST) {
                        popUpTo(Routes.MAIN_GRAPH) { inclusive = false }
                    }
                }
            },
            onLogout = onLogout
        )
    }
    
    composable(Routes.PROFILE) {
        SettingsScreen(
            onNavigateBack = {
                if (!navController.popBackStack()) {
                    navController.navigate(Routes.VIDEO_LIST) {
                        popUpTo(Routes.MAIN_GRAPH) { inclusive = false }
                    }
                }
            },
            onLogout = onLogout
        )
    }
}