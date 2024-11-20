package ru.workinprogress.feature.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.module.rememberKoinModules
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.workinprogress.feature.auth.domain.AuthUseCase
import ru.workinprogress.feature.auth.domain.LoginUseCase
import ru.workinprogress.feature.auth.domain.SignupUseCase
import ru.workinprogress.mani.components.Action
import ru.workinprogress.mani.components.MainAppBarState


@Composable
@Preview
fun AuthComponentImpl(
    modifier: Modifier = Modifier,
    state: AuthComponentUiState = remember { AuthComponentUiState("Auth", "", "", "OK") },
    onUsernameChanged: (String) -> Unit = {},
    onPasswordChanged: (String) -> Unit = {},
    onButtonClicked: () -> Unit = {},
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Card {
            Column(
                verticalArrangement = Arrangement.Center, modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 16.dp).testTag("appname")
                )
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(state.username,
                    onUsernameChanged,
                    Modifier.testTag("username"),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
                    ),
                    label = { Text("Username") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(state.password,
                    onPasswordChanged,
                    Modifier.testTag("password"),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        onButtonClicked()
                    }),
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text("Password") })
                Spacer(Modifier.height(24.dp))
                Button(
                    {
                        onButtonClicked()
                    }, modifier = Modifier.align(Alignment.CenterHorizontally).testTag("login")
                ) {
                    Text(state.buttonText)
                }
            }
        }
    }
}

data class AuthComponentUiState(
    val title: String,
    val username: String,
    val password: String,
    val buttonText: String,
)


@Composable
fun SignupComponent(onNavigateBack: () -> Unit, onSuccess: () -> Unit) {
    rememberKoinModules {
        listOf(module {
            singleOf(::SignupUseCase).bind<AuthUseCase>()

            viewModelOf(::AuthViewModel)
        })
    }

    val viewModel = koinViewModel<AuthViewModel>()
    val state = viewModel.observe.collectAsStateWithLifecycle()

    LaunchedEffect(state.value.success) {
        if (state.value.success) {
            onSuccess()
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {

                }

                Lifecycle.Event.ON_STOP -> {

                }

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)

        }
    }


    Column(
        modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        IconButton(
            onNavigateBack,
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 16.dp)
        ) {
            Icon(Icons.Default.Close, "Close")
        }
        AuthComponentImpl(
            Modifier.align(Alignment.CenterHorizontally),
            AuthComponentUiState("Sign up", state.value.username, state.value.password, "Create"),
            viewModel::onUsernameChanged,
            viewModel::onPasswordChanged,
            viewModel::onLoginClicked
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun LoginComponent(onSignupClicked: () -> Unit, onSuccess: () -> Unit) {
    rememberKoinModules {
        listOf(module {
            singleOf(::LoginUseCase).bind<AuthUseCase>()

            viewModelOf(::AuthViewModel)
        })
    }

    val viewModel = koinViewModel<AuthViewModel>()
    val state = viewModel.observe.collectAsStateWithLifecycle()

    LaunchedEffect(state.value.success) {
        if (state.value.success) {
            onSuccess()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 32.dp, bottom = 24.dp)
    ) {
        AuthComponentImpl(
            Modifier.weight(1f, true),
            AuthComponentUiState("Mani", state.value.username, state.value.password, "Login"),
            viewModel::onUsernameChanged,
            viewModel::onPasswordChanged,
            viewModel::onLoginClicked
        )
        TextButton(onSignupClicked) {
            Text("Sign up")
        }
    }
}