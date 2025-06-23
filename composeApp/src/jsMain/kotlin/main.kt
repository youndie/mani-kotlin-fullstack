import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToNavigation
import kotlinx.browser.document
import kotlinx.browser.window
import ru.workinprogress.mani.App

@OptIn(ExperimentalComposeUiApi::class, ExperimentalBrowserHistoryApi::class)
fun main() {
	CanvasBasedWindow("Mani") {
		LaunchedEffect("") {
			document.getElementById("progressBar")?.let { element ->
				document.body?.removeChild(element)
			}
		}

		App(
			onNavHostReady = { window.bindToNavigation(it) }
		)
	}
}