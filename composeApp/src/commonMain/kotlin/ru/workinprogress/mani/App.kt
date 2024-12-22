package ru.workinprogress.mani

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.core.module.Module
import ru.workinprogress.feature.transaction.Transaction
import ru.workinprogress.feature.transaction.ui.model.stringResource
import ru.workinprogress.mani.components.MainAppBarState
import ru.workinprogress.mani.components.ManiAppBar
import ru.workinprogress.mani.navigation.ManiAppNavHost
import ru.workinprogress.mani.navigation.ManiScreen
import ru.workinprogress.mani.navigation.title
import ru.workinprogress.mani.theme.AppTheme
import kotlin.math.roundToInt

@Composable
@Preview
fun App(
    modifier: Modifier = Modifier,
    platformModules: List<Module> = emptyList(),
    navController: NavHostController = rememberNavController(),
    onBackClicked: () -> Unit = {
        navController.popBackStack()
    }
) {
    KoinApplication({
        modules(appModules + platformModules)
    }) {
        val keyboardController = LocalSoftwareKeyboardController.current

        AppTheme {
            ManiApp(
                modifier, navController,
                onBackClicked = {
                    keyboardController?.hide()
                    onBackClicked()
                })
        }
    }
}

@Composable
fun ManiApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    appBarState: MainAppBarState = remember { MainAppBarState() },
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBackClicked: () -> Unit
) {
    val backStackEntry by navController.currentBackStackEntryAsState()

    val currentScreen = try {
        ManiScreen.valueOf(backStackEntry?.destination?.route ?: ManiScreen.Preload.name)
    } catch (e: Exception) {
        ManiScreen.Transaction
    }

    val labels = Transaction.Period.entries.map { period -> stringResource(period.stringResource) }

    LaunchedEffect(backStackEntry) {
        appBarState.showBack.value = navController.previousBackStackEntry != null
        appBarState.closeContextMenu()
    }

    LaunchedEffect(currentScreen) {
        appBarState.title.value = currentScreen.title()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(modifier = Modifier.animateContentSize()) {
                ManiAppBar(appBarState) {
                    onBackClicked()
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(snackBarHostState) },
        floatingActionButton = {
            AnimatedVisibility(
                currentScreen == ManiScreen.Main, exit = fadeOut() + slideOut(
                    targetOffset = {
                        IntOffset(
                            0, (it.height / 2f).roundToInt()
                        )
                    }), enter = fadeIn() + slideIn(
                    initialOffset = {
                        IntOffset(
                            0, (it.height / 2f).roundToInt()
                        )
                    })
            ) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(ManiScreen.Add.name)
                    },
                    modifier = Modifier.navigationBarsPadding().testTag("fab")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add",
                    )
                }
            }
        }) { padding ->
        ManiAppNavHost(
            modifier = Modifier.consumeWindowInsets(padding).padding(top = padding.calculateTopPadding()),
            navController = navController,
            appBarState = appBarState,
            snackbarHostState = snackBarHostState,
            onBackClicked
        )
    }
}



