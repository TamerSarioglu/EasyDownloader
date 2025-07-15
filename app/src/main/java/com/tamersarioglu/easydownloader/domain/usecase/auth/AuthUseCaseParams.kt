package com.tamersarioglu.easydownloader.domain.usecase.auth

/**
 * Parameter classes for authentication use cases.
 * 
 * These data classes provide type-safe parameter passing and make
 * the use cases more testable and maintainable.
 */

/**
 * Parameters for user registration.
 * 
 * @param username The desired username for registration
 * @param password The desired password for registration
 */
data class RegisterParams(
    val username: String,
    val password: String
)

/**
 * Parameters for user login.
 * 
 * @param username The user's username for authentication
 * @param password The user's password for authentication
 */
data class LoginParams(
    val username: String,
    val password: String
)