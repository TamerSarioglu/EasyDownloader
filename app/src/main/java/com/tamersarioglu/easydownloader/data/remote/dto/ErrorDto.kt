package com.tamersarioglu.easydownloader.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String,
    val code: String? = null,
    val details: Map<String, String>? = null
)

@Serializable
data class ValidationErrorResponse(
    val error: String,
    val message: String,
    val validationErrors: Map<String, List<String>>? = null
)