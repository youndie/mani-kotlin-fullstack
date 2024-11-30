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
                    navController.popBackStack()
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
        ManiScreen.Edit
    }

    LaunchedEffect(currentScreen) {
        when (currentScreen) {
            ManiScreen.Edit -> {
                history.pushState(
                    null,
                    "",
                    "/transaction/${backStackEntry?.toRoute<TransactionRoute>()?.id}/"
                )
            }

            ManiScreen.Main -> {
                history.replaceState(null, "", "/" + currentScreen.name.lowercase())
            }

            ManiScreen.Add -> {
                history.pushState(null, "", "/" + currentScreen.name.lowercase())
            }

            ManiScreen.Login -> {
                history.replaceState(null, "", "/" + currentScreen.name.lowercase())
            }

            ManiScreen.Signup -> {
                history.pushState(null, "", "/" + currentScreen.name.lowercase())
            }

            else -> {
            }
        }
    }
}