package ru.workinprogress.feature.auth.ui

data class AuthUiState(
    val username: String = "tester",
    val password: String = "qwerty123",
    val loading: Boolean = false,
    val success: Boolean = false,
    val errorMessage: String? = null
)