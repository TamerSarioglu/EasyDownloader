package com.tamersarioglu.easydownloader.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tamersarioglu.easydownloader.presentation.video_list.VideoListScreen
import com.tamersarioglu.easydownloader.presentation.video_list.VideoListViewModel
import com.tamersarioglu.easydownloader.presentation.video_submission.VideoSubmissionScreen
import com.tamersarioglu.easydownloader.presentation.video_submission.VideoSubmissionViewModel

@Composable
fun MainNavigation(
    navController: NavHostController,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.VIDEO_SUBMISSION,
        modifier = modifier
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
}

