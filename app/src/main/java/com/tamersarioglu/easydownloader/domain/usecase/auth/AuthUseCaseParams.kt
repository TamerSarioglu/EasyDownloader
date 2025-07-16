package com.tamersarioglu.easydownloader.domain.usecase.auth

data class RegisterParams(
    val username: String,
    val password: String
)

data class LoginParams(
    val username: String,
    val password: String
)