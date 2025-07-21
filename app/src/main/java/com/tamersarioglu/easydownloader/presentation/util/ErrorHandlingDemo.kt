package com.tamersarioglu.easydownloader.presentation.util

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamersarioglu.easydownloader.domain.model.AppError
import com.tamersarioglu.easydownloader.presentation.components.ErrorHandlingWrapper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Demo ViewModel showing comprehensive error handling usage
 */
class ErrorHandlingDemoViewModel : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorState = MutableStateFlow(ErrorState())
    val errorState: StateFlow<ErrorState> = _errorState.asStateFlow()
    
    private val _data = MutableStateFlow<String?>(null)
    val data: StateFlow<String?> = _data.asStateFlow()
    
    private val retryManager = RetryManager()

    /**
     * Example of comprehensive error handling with all utilities
     */
    fun loadDataWithErrorHandling() {
        executeWithComprehensiveErrorHandling(
            loadingState = _isLoading,
            errorStateFlow = _errorState,
            retryManager = retryManager,
            operation = { simulateApiCall() },
            onSuccess = { result ->
                _data.value = result
            },
            errorContext = ErrorContext.GENERAL,
            maxRetries = 3
        )
    }

    /**
     * Example of simple error handling
     */
    fun loadDataSimple() {
        executeWithErrorState(
            errorStateFlow = _errorState,
            loadingState = _isLoading,
            operation = { simulateApiCall() },
            onSuccess = { result ->
                _data.value = result
            },
            errorContext = ErrorContext.GENERAL
        )
    }

    /**
     * Example of retry-specific error handling
     */
    fun loadDataWithRetry() {
        // For this demo, we'll use the comprehensive error handling instead
        // since executeWithRetryAndErrorHandling expects MutableStateFlow<String?>
        executeWithComprehensiveErrorHandling(
            loadingState = _isLoading,
            errorStateFlow = _errorState,
            retryManager = retryManager,
            operation = { simulateApiCall() },
            onSuccess = { result ->
                _data.value = result
            },
            errorContext = ErrorContext.GENERAL,
            maxRetries = 3
        )
    }

    /**
     * Simulates various API call scenarios for demonstration
     */
    private suspend fun simulateApiCall(): Result<String> {
        delay(2000) // Simulate network delay
        
        return when ((1..5).random()) {
            1 -> Result.failure(AppError.NetworkError)
            2 -> Result.failure(AppError.ServerError)
            3 -> Result.failure(AppError.UnauthorizedError)
            4 -> Result.failure(AppError.ApiError("RATE_LIMITED", "Too many requests"))
            else -> Result.success("Data loaded successfully!")
        }
    }

    fun clearError() {
        _errorState.clearError()
    }

    fun retry() {
        loadDataWithErrorHandling()
    }
}

/**
 * Demo Composable showing comprehensive error handling UI
 */
@Composable
fun ErrorHandlingDemoScreen(
    viewModel: ErrorHandlingDemoViewModel
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    val data by viewModel.data.collectAsState()

    ErrorHandlingWrapper(
        isLoading = isLoading,
        errorState = errorState,
        onRetry = { viewModel.retry() },
        onDismissError = { viewModel.clearError() },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Error Handling Demo")
            
            Spacer(modifier = Modifier.height(16.dp))
            
            data?.let { 
                Text("Data: $it")
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Button(
                onClick = { viewModel.loadDataWithErrorHandling() }
            ) {
                Text("Load Data (Comprehensive)")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { viewModel.loadDataSimple() }
            ) {
                Text("Load Data (Simple)")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { viewModel.loadDataWithRetry() }
            ) {
                Text("Load Data (With Retry)")
            }
        }
    }
}