import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.skiko.wasm.onWasmReady
import ru.workinprogress.mani.App
import ru.workinprogress.mani.navigation.ManiScreen
import ru.workinprogress.mani.navigation.TransactionRoute
import web.history.history
import web.location.location

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    onWasmReady {
        CanvasBasedWindow("Mani") {
            val navController = rememberNavController()
            BrowserNavigator(navController)

            LaunchedEffect("") {
                document.getElementById("preloader")?.let { element ->
                    document.body?.removeChild(element)
                }

            }

            LaunchedEffect("") {
                window.onpopstate = {
                    val popState = (it.state as? String).orEmpty()

                    val history = navController.currentBackStack.value.map { entry ->
                        val screen = try {
                            ManiScreen.valueOf(entry.destination.route ?: ManiScreen.Preload.name)
                        } catch (e: Exception) {
                            ManiScreen.Transaction
                        }
                        when (screen) {
                            ManiScreen.Transaction -> {
                                "/transaction/${entry.toRoute<TransactionRoute>()?.id}/"
                            }

                            else -> {
                                "/" + screen.name.lowercase()
                            }
                        }
                    }

                    if (popState in history) {
                        navController.popBackStack()
                    } else {
                        val screen = ManiScreen.entries.firstOrNull { screen ->
                            screen.name.equals(popState.split("/")[1], true)
                        }
                        console.log(screen)
                        console.log(popState)
                        if (screen == ManiScreen.Transaction) {
                            val id = popState.split("/")[2]
                            console.log(id)

                            navController.navigate(TransactionRoute(id))
                        } else {
                            navController.navigate(screen?.name ?: ManiScreen.Main.name)
                        }
                    }
                }
            }

            App(
                onBackClicked = { history.back() },
                navController = navController,
                route = remember { location.href }
            )
        }
    }
}

@Composable
private fun BrowserNavigator(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()

    val currentScreen = try {
        ManiScreen.valueOf(backStackEntry?.destination?.route ?: ManiScreen.Preload.name)
    } catch (e: Exception) {
        ManiScreen.Transaction
    }

    LaunchedEffect(currentScreen) {
        when (currentScreen) {
            ManiScreen.Transaction -> {
                history.pushState(
                    "/transaction/${backStackEntry?.toRoute<TransactionRoute>()?.id}/",
                    "",
                    "/transaction/${backStackEntry?.toRoute<TransactionRoute>()?.id}/"
                )
            }

            ManiScreen.Main -> {
                history.replaceState("/" + currentScreen.name.lowercase(), "", "/" + currentScreen.name.lowercase())
            }

            ManiScreen.Add -> {
                history.pushState("/" + currentScreen.name.lowercase(), "", "/" + currentScreen.name.lowercase())
            }

            ManiScreen.Login -> {
                history.replaceState("/" + currentScreen.name.lowercase(), "", "/" + currentScreen.name.lowercase())
            }

            ManiScreen.Signup -> {
                history.pushState("/" + currentScreen.name.lowercase(), "", "/" + currentScreen.name.lowercase())
            }

            else -> {
            }
        }
    }
}