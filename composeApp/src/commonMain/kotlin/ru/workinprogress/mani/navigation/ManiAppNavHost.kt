package ru.workinprogress.mani.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ru.workinprogress.feature.auth.ui.LoginComponent
import ru.workinprogress.feature.main.ui.MainComponent
import ru.workinprogress.feature.transaction.ui.component.AddTransactionComponent
import ru.workinprogress.mani.components.MainAppBarState

@Composable
@NonRestartableComposable
fun ManiAppNavHost(
    appBarState: MainAppBarState,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = ManiScreen.Login.name,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .then(modifier)
    ) {
        composable(ManiScreen.Main.name) {
            MainComponent(appBarState)
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