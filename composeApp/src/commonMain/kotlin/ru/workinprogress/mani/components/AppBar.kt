package ru.workinprogress.mani.components

import androidx.compose.animation.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import ru.workinprogress.mani.navigation.ManiScreen


data class Action(
    val name: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

class MainAppBarState {
    val showBack = mutableStateOf(false)
    val title = mutableStateOf("")
    val contextTitle = mutableStateOf("")
    val contextMode = mutableStateOf(false)
    val actions = mutableStateOf(listOf<Action>())
}

fun ManiScreen.title() = when (this) {
    ManiScreen.Main -> "Home"
    ManiScreen.Add -> "Add transaction"
    ManiScreen.Login -> ""
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManiAppBar(
    appbarState: MainAppBarState = remember { MainAppBarState() },
    onBack: () -> Unit
) {
    TopAppBar(
        title = { Text(if (appbarState.contextMode.value) appbarState.contextTitle.value else appbarState.title.value) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        actions = {
            AnimatedVisibility(
                appbarState.actions.value.isNotEmpty(),
                enter = fadeIn() + expandIn(expandFrom = Alignment.CenterStart),
                exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.CenterStart)
            ) {
                appbarState.actions.value.forEach { action ->
                    IconButton(onClick = {
                        action.onClick()
                    }) {
                        Icon(imageVector = action.icon, contentDescription = action.name)
                    }
                }
            }
        },
        navigationIcon = {
            AnimatedVisibility(
                appbarState.showBack.value,
                enter = fadeIn() + expandIn(expandFrom = Alignment.CenterEnd),
                exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.CenterEnd)
            ) {
                IconButton(
                    onClick = {
                        onBack()
                    }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back"
                    )
                }
            }
            AnimatedVisibility(
                appbarState.contextMode.value,
                enter = fadeIn() + expandIn(expandFrom = Alignment.CenterEnd),
                exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.CenterEnd)
            ) {
                IconButton(
                    onClick = {
                        appbarState.contextMode.value = false
                    }) {
                    Icon(
                        imageVector = Icons.Default.Close, contentDescription = "back"
                    )
                }
            }
        }
    )
}
