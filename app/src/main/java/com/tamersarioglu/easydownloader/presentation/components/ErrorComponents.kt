package com.tamersarioglu.easydownloader.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.presentation.util.ErrorHandlingUtils
import com.tamersarioglu.easydownloader.presentation.util.ErrorSeverity
import com.tamersarioglu.easydownloader.presentation.util.ErrorState
import com.tamersarioglu.easydownloader.ui.theme.EasyDownloaderTheme

/**
 * Inline error message component for displaying errors within forms or content areas
 */
@Composable
fun InlineErrorMessage(
    error: String,
    modifier: Modifier = Modifier,
    severity: ErrorSeverity = ErrorSeverity.ERROR,
    onRetry: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (severity) {
                ErrorSeverity.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
                ErrorSeverity.ERROR -> MaterialTheme.colorScheme.errorContainer
                ErrorSeverity.CRITICAL -> MaterialTheme.colorScheme.errorContainer
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (severity) {
                        ErrorSeverity.WARNING -> Icons.Default.Warning
                        ErrorSeverity.ERROR -> Icons.Default.Error
                        ErrorSeverity.CRITICAL -> Icons.Default.Error
                    },
                    contentDescription = "Error",
                    tint = when (severity) {
                        ErrorSeverity.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
                        ErrorSeverity.ERROR -> MaterialTheme.colorScheme.error
                        ErrorSeverity.CRITICAL -> MaterialTheme.colorScheme.error
                    },
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = error,
                    color = when (severity) {
                        ErrorSeverity.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
                        ErrorSeverity.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                        ErrorSeverity.CRITICAL -> MaterialTheme.colorScheme.onErrorContainer
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (onRetry != null || onDismiss != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    onDismiss?.let {
                        TextButton(onClick = it) {
                            Text("Dismiss")
                        }
                    }
                    if (onRetry != null && onDismiss != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    onRetry?.let {
                        TextButton(onClick = it) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Retry",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Full-screen error component for displaying errors that prevent content from loading
 */
@Composable
fun FullScreenError(
    error: Throwable,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null
) {
    val errorInfo = ErrorHandlingUtils.createErrorMessageWithAction(error)
    val severity = ErrorHandlingUtils.getErrorSeverity(error)
    val isNetworkError = ErrorHandlingUtils.isNetworkError(error)
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = if (isNetworkError) Icons.Default.CloudOff else Icons.Default.Error,
                contentDescription = "Error",
                tint = when (severity) {
                    ErrorSeverity.WARNING -> MaterialTheme.colorScheme.tertiary
                    ErrorSeverity.ERROR -> MaterialTheme.colorScheme.error
                    ErrorSeverity.CRITICAL -> MaterialTheme.colorScheme.error
                },
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (isNetworkError) "Connection Problem" else "Something went wrong",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = errorInfo.message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (errorInfo.canRetry && onRetry != null) {
                    Button(
                        onClick = onRetry
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(errorInfo.actionText)
                        }
                    }
                }
                
                onNavigateBack?.let {
                    OutlinedButton(onClick = it) {
                        Text("Go Back")
                    }
                }
            }
        }
    }
}

/**
 * Loading state with error fallback component
 */
@Composable
fun LoadingWithError(
    isLoading: Boolean,
    error: String?,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            error != null -> {
                InlineErrorMessage(
                    error = error,
                    onRetry = onRetry,
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> content()
        }
    }
}

/**
 * Retry button component with loading state
 */
@Composable
fun RetryButton(
    onRetry: () -> Unit,
    isRetrying: Boolean = false,
    modifier: Modifier = Modifier,
    text: String = "Retry"
) {
    Button(
        onClick = onRetry,
        enabled = !isRetrying,
        modifier = modifier
    ) {
        if (isRetrying) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retrying...")
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text)
            }
        }
    }
}

/**
 * Network status indicator component
 */
@Composable
fun NetworkStatusIndicator(
    isOffline: Boolean,
    modifier: Modifier = Modifier
) {
    if (isOffline) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = "Offline",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "You're offline. Some features may not be available.",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Comprehensive error handling component that combines error display with network status
 */
@Composable
fun ErrorHandlingContainer(
    errorState: ErrorState,
    isOffline: Boolean = false,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        // Network status indicator
        NetworkStatusIndicator(
            isOffline = isOffline,
            modifier = Modifier.padding(bottom = if (isOffline) 8.dp else 0.dp)
        )
        
        // Error message
        if (errorState.hasError && errorState.message != null) {
            InlineErrorMessage(
                error = errorState.message,
                severity = errorState.severity,
                onRetry = if (errorState.canRetry) onRetry else null,
                onDismiss = onDismiss,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // Main content
        content()
    }
}

/**
 * Smart error boundary that automatically handles different error states
 */
@Composable
fun ErrorBoundary(
    isLoading: Boolean,
    error: String?,
    isOffline: Boolean = false,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null,
    loadingContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        when {
            isLoading -> {
                loadingContent?.invoke() ?: Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            error != null -> {
                Column {
                    NetworkStatusIndicator(
                        isOffline = isOffline,
                        modifier = Modifier.padding(bottom = if (isOffline) 8.dp else 0.dp)
                    )
                    
                    InlineErrorMessage(
                        error = error,
                        onRetry = onRetry,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            else -> {
                Column {
                    NetworkStatusIndicator(
                        isOffline = isOffline,
                        modifier = Modifier.padding(bottom = if (isOffline) 8.dp else 0.dp)
                    )
                    content()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InlineErrorMessagePreview() {
    EasyDownloaderTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InlineErrorMessage(
                error = "Network connection failed. Please check your internet connection.",
                severity = ErrorSeverity.WARNING,
                onRetry = {},
                onDismiss = {}
            )
            
            InlineErrorMessage(
                error = "Server error occurred. Please try again later.",
                severity = ErrorSeverity.ERROR,
                onRetry = {}
            )
            
            InlineErrorMessage(
                error = "Authentication failed. Please log in again.",
                severity = ErrorSeverity.CRITICAL
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FullScreenErrorPreview() {
    EasyDownloaderTheme {
        FullScreenError(
            error = AppError.NetworkError,
            onRetry = {},
            onNavigateBack = {}
        )
    }
}

/**
 * Comprehensive error handling wrapper that provides all error handling features
 */
@Composable
fun ErrorHandlingWrapper(
    isLoading: Boolean,
    errorState: ErrorState,
    isOffline: Boolean = false,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    onDismissError: (() -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null,
    loadingContent: (@Composable () -> Unit)? = null,
    errorContent: (@Composable (ErrorState) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        when {
            isLoading -> {
                loadingContent?.invoke() ?: Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            errorState.hasError -> {
                Column {
                    NetworkStatusIndicator(
                        isOffline = isOffline,
                        modifier = Modifier.padding(bottom = if (isOffline) 8.dp else 0.dp)
                    )
                    
                    errorContent?.invoke(errorState) ?: InlineErrorMessage(
                        error = errorState.message ?: "An error occurred",
                        severity = errorState.severity,
                        onRetry = if (errorState.canRetry) onRetry else null,
                        onDismiss = onDismissError,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            else -> {
                Column {
                    NetworkStatusIndicator(
                        isOffline = isOffline,
                        modifier = Modifier.padding(bottom = if (isOffline) 8.dp else 0.dp)
                    )
                    content()
                }
            }
        }
    }
}

/**
 * Error dialog component for critical errors
 */
@Composable
fun ErrorDialog(
    error: Throwable,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null
) {
    val errorInfo = ErrorHandlingUtils.createErrorMessageWithAction(error)
    val title = ErrorHandlingUtils.getErrorTitle(error)
    
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = errorInfo.message,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            if (errorInfo.canRetry && onRetry != null) {
                Button(onClick = onRetry) {
                    Text(errorInfo.actionText)
                }
            } else {
                Button(onClick = onDismiss) {
                    Text("OK")
                }
            }
        },
        dismissButton = {
            if (onNavigateBack != null) {
                TextButton(onClick = onNavigateBack) {
                    Text("Go Back")
                }
            } else if (errorInfo.canRetry && onRetry != null) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun RetryButtonPreview() {
    EasyDownloaderTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RetryButton(onRetry = {})
            RetryButton(onRetry = {}, isRetrying = true)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorHandlingWrapperPreview() {
    EasyDownloaderTheme {
        ErrorHandlingWrapper(
            isLoading = false,
            errorState = ErrorState.fromThrowable(AppError.NetworkError),
            isOffline = true,
            onRetry = {}
        ) {
            Text("Main content goes here")
        }
    }
}