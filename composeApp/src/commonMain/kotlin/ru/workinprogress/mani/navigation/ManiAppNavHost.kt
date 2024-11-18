package ru.workinprogress.mani.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.koin.compose.koinInject
import ru.workinprogress.feature.auth.data.TokenRepository
import ru.workinprogress.feature.auth.ui.LoginComponent
import ru.workinprogress.feature.main.ui.MainComponent
import ru.workinprogress.feature.transaction.ui.component.AddTransactionComponent
import ru.workinprogress.mani.components.MainAppBarState

@Composable
@NonRestartableComposable
fun ManiAppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    appBarState: MainAppBarState,
    snackbarHostState: SnackbarHostState,
) {
    val tokenRepository = koinInject<TokenRepository>()

    val tokenState = tokenRepository.observeToken().collectAsStateWithLifecycle()
    val isAuth = derivedStateOf { tokenState.value.refreshToken?.isNotEmpty() == true }

    NavHost(
        navController = navController,
        startDestination = if (isAuth.value) ManiScreen.Main.name else ManiScreen.Login.name,
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).then(modifier)
    ) {
        composable(ManiScreen.Main.name) {
            MainComponent(appBarState, snackbarHostState)
        }
        composable(ManiScreen.Add.name) {
            AddTransactionComponent {
                navController.popBackStack()
            }
        }
        composable(ManiScreen.Login.name) {
            LoginComponent {
                navController.navigateAndClean(ManiScreen.Main.name)
            }
        }
    }
}