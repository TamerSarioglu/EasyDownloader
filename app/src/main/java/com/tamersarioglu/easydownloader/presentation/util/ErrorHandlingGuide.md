# Error Handling Guide

This guide explains how to use the comprehensive error handling utilities in the EasyDownloader app.

## Overview

The error handling system consists of several components that work together:

1. **ErrorMapper** - Maps different error types to user-friendly messages
2. **ErrorHandlingUtils** - Core utilities for error processing and retry logic
3. **RetryManager** - Manages retry operations with exponential backoff
4. **ErrorComponents** - Reusable UI components for displaying errors
5. **BaseViewModel** - Base class with built-in error handling
6. **ViewModelErrorExtensions** - Extension functions for ViewModels
7. **NetworkConnectivityManager** - Monitors network connectivity

## Quick Start

### 1. Using BaseViewModel

Extend `BaseViewModel` for automatic error handling:

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: MyRepository
) : BaseViewModel() {

    fun loadData() {
        executeOperation(
            operation = { repository.getData() },
            onSuccess = { data ->
                // Handle success
            },
            errorContext = ErrorContext.GENERAL
        )
    }
}
```

### 2. Using Error Components

Display errors in your UI:

```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    ErrorBoundary(
        isLoading = isLoading,
        error = error,
        onRetry = { viewModel.retry() }
    ) {
        // Your main content
    }
}
```

### 3. Using Comprehensive Error Handling

For advanced scenarios:

```kotlin
fun loadDataWithRetry() {
    executeWithComprehensiveErrorHandling(
        loadingState = _isLoading,
        errorStateFlow = _errorState,
        retryManager = retryManager,
        operation = { repository.getData() },
        onSuccess = { data -> handleSuccess(data) },
        errorContext = ErrorContext.GENERAL,
        maxRetries = 3
    )
}
```

## Error Types

### Domain Errors (AppError)

- `NetworkError` - Network connectivity issues
- `ServerError` - Server-side problems
- `ApiError` - API-specific errors with codes
- `UnauthorizedError` - Authentication failures
- `ValidationError` - Input validation failures

### Error Contexts

Use appropriate contexts for better error messages:

- `ErrorContext.LOGIN` - Login-specific errors
- `ErrorContext.REGISTRATION` - Registration-specific errors
- `ErrorContext.VIDEO_SUBMISSION` - Video submission errors
- `ErrorContext.VIDEO_LIST` - Video list errors
- `ErrorContext.AUTH` - General authentication errors
- `ErrorContext.GENERAL` - Default context

## UI Components

### InlineErrorMessage

For displaying errors within content:

```kotlin
InlineErrorMessage(
    error = "Network connection failed",
    severity = ErrorSeverity.WARNING,
    onRetry = { /* retry action */ },
    onDismiss = { /* dismiss action */ }
)
```

### FullScreenError

For critical errors that prevent content loading:

```kotlin
FullScreenError(
    error = AppError.NetworkError,
    onRetry = { /* retry action */ },
    onNavigateBack = { /* navigation action */ }
)
```

### ErrorHandlingWrapper

Comprehensive wrapper with all features:

```kotlin
ErrorHandlingWrapper(
    isLoading = isLoading,
    errorState = errorState,
    isOffline = isOffline,
    onRetry = { /* retry */ },
    onDismissError = { /* dismiss */ }
) {
    // Main content
}
```

## Retry Logic

### Automatic Retry

```kotlin
val result = ErrorHandlingUtils.executeWithRetry(
    maxAttempts = 3,
    operation = { apiCall() }
)
```

### Manual Retry with RetryManager

```kotlin
class MyViewModel : ViewModel() {
    private val retryManager = RetryManager(maxAttempts = 3)
    
    fun loadData() {
        viewModelScope.launch {
            val result = retryManager.executeWithRetry(
                operation = { repository.getData() }
            )
            // Handle result
        }
    }
}
```

## Network Connectivity

Monitor network state:

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val networkManager: NetworkConnectivityManager
) : ViewModel() {
    
    val isOffline = networkManager.networkConnectivityFlow()
        .map { !it }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)
}
```

## Best Practices

1. **Use appropriate error contexts** for better user messages
2. **Extend BaseViewModel** for consistent error handling
3. **Use ErrorHandlingWrapper** for comprehensive error UI
4. **Monitor network connectivity** for better UX
5. **Implement retry logic** for transient errors
6. **Handle unauthorized errors** by triggering logout
7. **Provide clear error messages** with actionable suggestions

## Error Severity Levels

- `WARNING` - User can continue (yellow/orange)
- `ERROR` - Operation failed but recoverable (red)
- `CRITICAL` - Requires immediate attention (red with emphasis)

## Common Patterns

### ViewModel with Error State

```kotlin
class MyViewModel : ViewModel() {
    private val _errorState = createErrorStateFlow()
    val errorState = _errorState.asStateFlow()
    
    fun performAction() {
        executeWithErrorState(
            errorStateFlow = _errorState,
            operation = { repository.performAction() },
            onSuccess = { /* handle success */ },
            errorContext = ErrorContext.GENERAL
        )
    }
}
```

### Handling Specific Errors

```kotlin
fun handleError(error: Throwable) {
    handleSpecificErrors(
        error = error,
        onUnauthorized = { navigateToLogin() },
        onNetworkError = { showOfflineMessage() },
        onValidationError = { validationError ->
            showFieldError(validationError.field, validationError.message)
        }
    )
}
```

This comprehensive error handling system ensures consistent, user-friendly error management throughout the application.