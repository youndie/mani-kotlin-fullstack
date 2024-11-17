package ru.workinprogress.mani

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import ru.workinprogress.mani.components.MainAppBarState
import ru.workinprogress.mani.components.ManiAppBar
import ru.workinprogress.mani.navigation.ManiAppNavHost
import ru.workinprogress.mani.navigation.ManiScreen
import ru.workinprogress.mani.theme.AppTheme
import kotlin.math.roundToInt

@Composable
fun ManiApp(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val appBarState = remember { MainAppBarState() }
    val currentScreen = ManiScreen.valueOf(
        backStackEntry?.destination?.route ?: ManiScreen.Main.name
    )

    LaunchedEffect(backStackEntry) {
        appBarState.showBack.value = navController.previousBackStackEntry != null
        appBarState.contextMode.value = false
    }

    LaunchedEffect(currentScreen) {
        appBarState.title.value = currentScreen.name
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Scaffold(
            topBar = {
                ManiAppBar(appBarState) {
                    navController.popBackStack()
                }
            },
            floatingActionButton = {
                AnimatedVisibility(
                    currentScreen == ManiScreen.Main,
                    exit = fadeOut() + slideOut(targetOffset = {
                        IntOffset(
                            0,
                            (it.height / 2f).roundToInt()
                        )
                    }),
                    enter = fadeIn() + slideIn(initialOffset = {
                        IntOffset(
                            0,
                            (it.height / 2f).roundToInt()
                        )
                    })
                ) {
                    FloatingActionButton(onClick = {
                        navController.navigate(ManiScreen.Add.name)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add",
                        )
                    }
                }
            }
        ) { padding ->
            ManiAppNavHost(appBarState, navController, Modifier.padding(padding))
        }
    }


}

@Composable
@Preview
fun App(platformModules: List<Module> = emptyList()) {
    KoinApplication(
        application = {
            modules(appModules + platformModules)
        }) {
        AppTheme(darkTheme = true) {
            ManiApp()
        }
    }
}


