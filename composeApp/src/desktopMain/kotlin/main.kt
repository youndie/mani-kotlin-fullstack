import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material.icons.sharp.Fullscreen
import androidx.compose.material.icons.sharp.FullscreenExit
import androidx.compose.material.icons.sharp.Minimize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.workinprogress.mani.App
import ru.workinprogress.mani.theme.AppTheme
import java.awt.Toolkit


@OptIn(ExperimentalFoundationApi::class)
fun main() = application {
	val state = rememberWindowState(
		size = DpSize(1024.dp, 720.dp)
	)

	val screenSize = Toolkit.getDefaultToolkit().screenSize
	val fullScreenSize = DpSize(screenSize.width.dp, screenSize.height.dp)

	var savedSize by remember { mutableStateOf(DpSize(0.dp, 0.dp)) }
	var savedPosition: WindowPosition by remember { mutableStateOf(WindowPosition(0.dp, 0.dp)) }
	val coroutineScope = rememberCoroutineScope()

	Window(
		state = state,
		onCloseRequest = ::exitApplication,
		resizable = true,
		undecorated = true,
		title = "mani",
	) {
		WindowDraggableArea {
			AppTheme {
				Surface(
					color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
					contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
				) {
					Box(modifier = Modifier.height(32.dp)) {
						Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
							IconButton({
								exitApplication()
							}) {
								Icon(Icons.Sharp.Close, modifier = Modifier.size(24.dp), contentDescription = "close")
							}

							if (state.placement == androidx.compose.ui.window.WindowPlacement.Fullscreen) {
								IconButton({
									state.placement = androidx.compose.ui.window.WindowPlacement.Floating
									state.position = savedPosition

									coroutineScope.launch {
										delay(10)
										state.size = savedSize
									}
								}) {
									Icon(
										Icons.Sharp.FullscreenExit,
										modifier = Modifier.size(24.dp),
										contentDescription = "fullscreen"
									)
								}
							} else {
								IconButton({
									state.isMinimized = true
								}) {
									Icon(
										Icons.Sharp.Minimize,
										modifier = Modifier.size(24.dp),
										contentDescription = "minimize"
									)
								}

								IconButton({
									savedSize = state.size
									savedPosition = state.position

									state.size = fullScreenSize

									coroutineScope.launch {
										delay(10)
										state.placement = androidx.compose.ui.window.WindowPlacement.Fullscreen
									}
								}) {
									Icon(
										Icons.Sharp.Fullscreen,
										modifier = Modifier.size(24.dp),
										contentDescription = "fullscreen"
									)
								}
							}

						}
						Text(
							"Mani",
							modifier = Modifier.fillMaxWidth().align(androidx.compose.ui.Alignment.Center),
							textAlign = androidx.compose.ui.text.style.TextAlign.Center
						)
					}
				}
			}
		}
		App(modifier = Modifier.fillMaxSize().padding(top = 32.dp))
	}
}