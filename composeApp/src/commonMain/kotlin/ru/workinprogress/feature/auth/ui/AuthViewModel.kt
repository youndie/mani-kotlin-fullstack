package ru.workinprogress.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.workinprogress.feature.auth.LoginParams
import ru.workinprogress.feature.auth.domain.AuthUseCase
import ru.workinprogress.useCase.UseCase


class AuthViewModel(private val authUseCase: AuthUseCase) : ViewModel() {

    private val state = MutableStateFlow(AuthUiState())
    val observe = state.asStateFlow()

    fun onUsernameChanged(username: String) {
        state.update {
            it.copy(username = username, errorMessage = null)
        }
    }

    fun onPasswordChanged(password: String) {
        state.update {
            it.copy(password = password, errorMessage = null)
        }
    }

    fun onLoginClicked() {
        viewModelScope.launch {
            state.update {
                it.copy(loading = true, errorMessage = null)
            }

            val result = authUseCase.invoke(LoginParams(state.value.username, state.value.password))

            when (result) {
                is UseCase.Result.Success -> {
                    state.update {
                        it.copy(success = true)
                    }
                }

                is UseCase.Result.Error -> {
                    state.update {
                        it.copy(loading = false, errorMessage = result.throwable.message.orEmpty())
                    }
                }
            }
        }
    }
}

