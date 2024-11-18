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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import kotlinx.collections.immutable.toImmutableList
import ru.workinprogress.mani.emptyImmutableList
import kotlin.math.roundToInt

data class Action(
    val name: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

class MainAppBarState {
    val showBack = mutableStateOf(false)
    val title = mutableStateOf(" ")
    val contextTitle = mutableStateOf("")
    val contextMode get() = contextModeState.value
    val actions get() = actionsState.value
    val empty
        get() = title.value.isEmpty()
                && actions.isEmpty()
                && !contextMode

    private val contextModeState = mutableStateOf(false)
    private val actionsState = mutableStateOf(emptyImmutableList<Action>())
    private val backUpActions = mutableStateOf(emptyImmutableList<Action>())

    fun showAction(action: Action) {
        this.actionsState.value = this.actionsState.value.plus(action).toImmutableList()
    }

    fun removeAction(action: Action) {
        this.actionsState.value = this.actionsState.value.minus(action).toImmutableList()
    }

    fun showContextMenu(actions: List<Action>) {
        backUpActions.value = this.actionsState.value.toImmutableList()
        this.actionsState.value = actions.toImmutableList()
        this.contextModeState.value = true
    }

    fun closeContextMenu() {
        if (backUpActions.value.isNotEmpty()) {
            this.actionsState.value = backUpActions.value.toImmutableList()
            backUpActions.value = emptyImmutableList()
        }
        contextModeState.value = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManiAppBar(
    appbarState: MainAppBarState = remember { MainAppBarState() },
    onBack: () -> Unit
) {
    AnimatedVisibility(
        appbarState.empty.not(),
        exit = fadeOut() + slideOut(targetOffset = {
            IntOffset(
                0,
                -(it.height / 2f).roundToInt()
            )
        }),
        enter = fadeIn() + slideIn(initialOffset = {
            IntOffset(
                0,
                -(it.height / 2f).roundToInt()
            )
        })
    ) {
        TopAppBar(
            modifier = Modifier,
            title = { Text(if (appbarState.contextMode) appbarState.contextTitle.value else appbarState.title.value) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = if (appbarState.contextMode) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surfaceContainer,
                titleContentColor = if (appbarState.contextMode) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface,
            ),
            actions = {
                AnimatedVisibility(
                    appbarState.actions.isNotEmpty(),
                    enter = fadeIn() + expandIn(expandFrom = Alignment.CenterStart),
                    exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.CenterStart)
                ) {
                    appbarState.actions.forEach { action ->
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
                    appbarState.contextMode,
                    enter = fadeIn() + expandIn(expandFrom = Alignment.CenterEnd),
                    exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.CenterEnd)
                ) {
                    IconButton(
                        onClick = {
                            appbarState.closeContextMenu()
                        }) {
                        Icon(
                            imageVector = Icons.Default.Close, contentDescription = "back"
                        )
                    }
                }
            }
        )
    }
}
