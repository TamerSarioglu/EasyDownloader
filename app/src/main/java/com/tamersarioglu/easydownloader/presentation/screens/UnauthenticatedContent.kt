package com.tamersarioglu.easydownloader.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tamersarioglu.easydownloader.presentation.auth.AuthViewModel
import com.tamersarioglu.easydownloader.presentation.navigation.AuthNavigation

@Composable
fun UnauthenticatedContent(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel
) {
    AuthNavigation(
        modifier = modifier,
        authViewModel = authViewModel
    )
}