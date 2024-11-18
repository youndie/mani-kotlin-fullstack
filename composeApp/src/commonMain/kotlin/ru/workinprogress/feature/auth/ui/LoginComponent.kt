package ru.workinprogress.feature.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI


@OptIn(KoinExperimentalAPI::class)
@Composable
fun LoginComponent(onSuccess: () -> Unit) {

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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    label = { Text("Username") }
                )
                OutlinedTextField(
                    state.value.password,
                    viewModel::onPasswordChanged,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        viewModel.onLoginClicked()
                    }),
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