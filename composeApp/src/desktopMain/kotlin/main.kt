import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ru.workinprogress.mani.App

fun main() = application {
    Window(
        state = rememberWindowState(
            size = DpSize(1024.dp, 720.dp)
        ),
        onCloseRequest = ::exitApplication,
        resizable = true,
        title = "mani",
    ) {
        App()
    }
}