package com.tamersarioglu.easydownloader.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tamersarioglu.easydownloader.presentation.auth.AuthStateViewModel
import com.tamersarioglu.easydownloader.presentation.navigation.AuthNavigation

@Composable
fun UnauthenticatedContent(
    modifier: Modifier = Modifier,
    authStateViewModel: AuthStateViewModel
) {
    AuthNavigation(
        modifier = modifier
    )
}