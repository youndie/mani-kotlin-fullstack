import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import ru.workinprogress.appframe.AppFrame
import ru.workinprogress.mani.App
import ru.workinprogress.mani.theme.AppTheme

@OptIn(ExperimentalFoundationApi::class)
fun main() = application {
	AppFrame(
		::exitApplication,
		title = "Mani",
		appThemeApplier = { AppTheme { it() } }) {
		App(modifier = Modifier.fillMaxSize().padding(top = 32.dp))
	}
}