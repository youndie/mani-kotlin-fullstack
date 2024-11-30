import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import kotlinx.browser.document
import org.jetbrains.skiko.wasm.onWasmReady
import ru.workinprogress.mani.App

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    onWasmReady {
        CanvasBasedWindow("Mani") {
            LaunchedEffect("") {
                document.getElementById("preloader")?.let { element ->
                    document.body?.removeChild(element)
                }
            }
            App()
        }
    }
}