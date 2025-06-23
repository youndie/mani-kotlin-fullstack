import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToNavigation
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.skiko.wasm.onWasmReady
import ru.workinprogress.mani.App
import web.history.history

@OptIn(ExperimentalComposeUiApi::class, ExperimentalBrowserHistoryApi::class)
fun main() {
	onWasmReady {
		CanvasBasedWindow("Mani") {
			LaunchedEffect("") {
				document.getElementById("progressBar")?.let { element ->
					document.body?.removeChild(element)
				}
			}

			App(
				onBackClicked = { history.back() },
				onNavHostReady = { window.bindToNavigation(it) }
			)
		}
	}
}