package com.tamersarioglu.easydownloader.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tamersarioglu.easydownloader.ui.theme.EasyDownloaderTheme

/**
 * Registration screen with Compose that provides user registration functionality.
 * 
 * Features:
 * - Registration form with username and password fields
 * - Input validation with real-time feedback
 * - Loading states and error message display
 * - Navigation to login screen
 * 
 * Requirements: 1.1, 1.2, 1.4, 1.5, 1.6, 7.1, 7.2, 7.4
 */
@Composable
fun RegistrationScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    val authUiState by authViewModel.uiState.collectAsState()
    val registrationForm by authViewModel.registrationForm.collectAsState()
    val focusManager = LocalFocusManager.current
    
    var passwordVisible by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Title
            Text(
                text = "EasyDownloader",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Create your account",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // Registration Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Username Field
                    OutlinedTextField(
                        value = registrationForm.username,
                        onValueChange = { authViewModel.updateRegistrationUsername(it) },
                        label = { Text("Username") },
                        placeholder = { Text("Enter your username") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !authUiState.isLoading,
                        isError = registrationForm.usernameError != null,
                        supportingText = {
                            when {
                                registrationForm.usernameError != null -> {
                                    Text(
                                        text = registrationForm.usernameError!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                registrationForm.username.isNotEmpty() && registrationForm.username.length < 3 -> {
                                    Text(
                                        text = "Username must be at least 3 characters",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                registrationForm.username.length >= 3 -> {
                                    Text(
                                        text = "✓ Username looks good",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true
                    )
                    
                    // Password Field
                    OutlinedTextField(
                        value = registrationForm.password,
                        onValueChange = { authViewModel.updateRegistrationPassword(it) },
                        label = { Text("Password") },
                        placeholder = { Text("Enter your password") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !authUiState.isLoading,
                        isError = registrationForm.passwordError != null,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible }
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        supportingText = {
                            when {
                                registrationForm.passwordError != null -> {
                                    Text(
                                        text = registrationForm.passwordError!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                registrationForm.password.isNotEmpty() && registrationForm.password.length < 6 -> {
                                    Text(
                                        text = "Password must be at least 6 characters",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                registrationForm.password.length >= 6 -> {
                                    Text(
                                        text = "✓ Password strength is good",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { 
                                focusManager.clearFocus()
                                if (authViewModel.isRegistrationFormValid()) {
                                    authViewModel.register()
                                }
                            }
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Register Button
                    Button(
                        onClick = { authViewModel.register() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !authUiState.isLoading && authViewModel.isRegistrationFormValid()
                    ) {
                        if (authUiState.isLoading) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.width(16.dp).height(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Creating Account...")
                            }
                        } else {
                            Text("Create Account")
                        }
                    }
                    
                    // Error Message
                    authUiState.error?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Navigation to Login
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = onNavigateToLogin,
                    enabled = !authUiState.isLoading
                ) {
                    Text("Sign In")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrationScreenPreview() {
    EasyDownloaderTheme {
        // Note: This preview won't work properly without a real ViewModel
        // but it shows the UI structure
    }
}