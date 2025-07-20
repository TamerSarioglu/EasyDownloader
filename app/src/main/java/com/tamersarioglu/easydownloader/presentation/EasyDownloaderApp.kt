package com.tamersarioglu.easydownloader.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.tamersarioglu.easydownloader.presentation.auth.AuthStateViewModel
import com.tamersarioglu.easydownloader.presentation.navigation.VideoDownloaderNavigation

@Composable
fun EasyDownloaderApp(
    modifier: Modifier = Modifier,
    authStateViewModel: AuthStateViewModel = hiltViewModel()
) {
    val authState by authStateViewModel.authState.collectAsState()
    val navController = rememberNavController()
    
    LaunchedEffect(Unit) {

    }
    
    when {
        authState.isLoading -> {
            LoadingScreen(modifier = modifier)
        }
        
        else -> {
            VideoDownloaderNavigation(
                modifier = modifier,
                navController = navController,
                authStateViewModel = authStateViewModel
            )
        }
    }
}

@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EasyDownloaderAppPreview() {
    EasyDownloaderApp()
}