package ru.workinprogress.feature.auth.ui

data class AuthUiState(
    val username: String = "tester",
    val password: String = "qwerty123",
    val success: Boolean = false
)