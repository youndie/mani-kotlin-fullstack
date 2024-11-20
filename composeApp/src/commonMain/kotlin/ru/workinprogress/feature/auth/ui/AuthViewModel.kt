package ru.workinprogress.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.workinprogress.feature.auth.LoginParams
import ru.workinprogress.feature.auth.domain.AuthUseCase
import ru.workinprogress.feature.auth.domain.LoginUseCase
import ru.workinprogress.useCase.UseCase


class AuthViewModel(private val authUseCase: AuthUseCase) : ViewModel() {

    private val state = MutableStateFlow(AuthUiState())
    val observe = state.asStateFlow()

    fun onUsernameChanged(username: String) {
        state.update {
            it.copy(username = username)
        }
    }

    fun onPasswordChanged(password: String) {
        state.update {
            it.copy(password = password)
        }
    }

    fun onLoginClicked() {
        viewModelScope.launch {
            val result = authUseCase.invoke(LoginParams(state.value.username, state.value.password))

            if (result is UseCase.Result.Success) {
                state.update {
                    it.copy(success = true)
                }
            }
        }
    }
}