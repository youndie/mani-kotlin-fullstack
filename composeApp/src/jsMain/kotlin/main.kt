import androidx.compose.runtime.*
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

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    onWasmReady {
        CanvasBasedWindow("Mani") {
            val navController = rememberNavController()
            BrowserNavigator(navController)

            LaunchedEffect("") {
                document.getElementById("progressBar")?.let { element ->
                    document.body?.removeChild(element)
                }
            }

            App(
                onBackClicked = { history.back() },
                navController = navController
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

    val pendingForward = remember { mutableStateOf(false) }

    LaunchedEffect(currentScreen) {
        if (pendingForward.value) {
            history.go(1)
            pendingForward.value = false
            return@LaunchedEffect
        }

        when (currentScreen) {
            ManiScreen.Transaction -> {
                history.pushState(
                    "/${currentScreen.name.lowercase()}/${backStackEntry?.toRoute<TransactionRoute>()?.id}/",
                    "",
                    "app#${currentScreen.name.lowercase()}/${backStackEntry?.toRoute<TransactionRoute>()?.id}/"
                )
            }

            ManiScreen.Main -> {
                history.replaceState("/" + currentScreen.name.lowercase(), "", "app#" + currentScreen.name.lowercase())
            }

            ManiScreen.Add, ManiScreen.Login, ManiScreen.Signup -> {
                history.pushState("/" + currentScreen.name.lowercase(), "", "app#" + currentScreen.name.lowercase())
            }

            else -> {
            }
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
                        "/${screen.name.lowercase()}/${entry.toRoute<TransactionRoute>()?.id}/"
                    }

                    else -> {
                        "/${screen.name.lowercase()}"
                    }
                }
            }

            if (popState in history) {
                navController.popBackStack()
            } else {
                pendingForward.value = true

                val screen = ManiScreen.entries.firstOrNull { screen ->
                    screen.name.equals(popState.split("/")[1], true)
                }
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
}