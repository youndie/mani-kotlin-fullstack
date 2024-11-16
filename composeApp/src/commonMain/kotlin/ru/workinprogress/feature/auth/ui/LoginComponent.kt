package ru.workinprogress.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import org.koin.compose.module.rememberKoinModules
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.workinprogress.feature.auth.domain.LoginUseCase


@OptIn(KoinExperimentalAPI::class)
@Composable
fun LoginComponent(onSuccess: () -> Unit) {
    rememberKoinModules {
        listOf(
            module {
                singleOf(::LoginUseCase)

                viewModelOf(::LoginViewModel)
            }
        )
    }

    val viewModel = koinViewModel<LoginViewModel>()
    val state = viewModel.observe.collectAsStateWithLifecycle()

    LaunchedEffect(state.value.success) {
        if (state.value.success) {
            onSuccess()
        }
    }

    Box(contentAlignment = Alignment.Center) {
        Card {
            Column(
                verticalArrangement = spacedBy(8.dp, Alignment.CenterVertically),
                modifier = Modifier.padding(24.dp)
            ) {
                Text("Login", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    state.value.username,
                    viewModel::onUsernameChanged,
                    label = { Text("Username") }
                )
                OutlinedTextField(
                    state.value.password,
                    viewModel::onPasswordChanged,
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text("Password") })
                Spacer(Modifier.height(4.dp))
                Button(
                    { viewModel.onLoginClicked() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Login")
                }
            }
        }
    }
}