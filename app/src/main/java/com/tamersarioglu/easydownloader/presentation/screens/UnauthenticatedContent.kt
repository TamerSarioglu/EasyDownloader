package com.tamersarioglu.easydownloader.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tamersarioglu.easydownloader.presentation.auth.AuthViewModel
import com.tamersarioglu.easydownloader.presentation.navigation.AuthNavigation

/**
 * Content shown when user is not authenticated.
 * Displays the authentication navigation with login and registration screens.
 * 
 * Requirements: 1.1, 2.1 (authentication screens)
 */
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