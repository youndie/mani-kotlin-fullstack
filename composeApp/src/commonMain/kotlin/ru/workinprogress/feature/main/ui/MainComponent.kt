package ru.workinprogress.feature.main.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LocalPinnableContainer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.launch
import mani.composeapp.generated.resources.Res
import mani.composeapp.generated.resources.transactions
import org.jetbrains.compose.resources.getPluralString
import org.koin.compose.module.rememberKoinModules
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.workinprogress.feature.chart.ui.ChartComponent
import ru.workinprogress.feature.main.MainViewModel
import ru.workinprogress.feature.transaction.ui.component.TransactionsDay
import ru.workinprogress.feature.transaction.ui.model.TransactionUiItem
import ru.workinprogress.mani.components.Action
import ru.workinprogress.mani.components.MainAppBarState


@Composable
private fun ColumnScope.FutureInfoShimmer() {
    val shimmer = rememberShimmer(ShimmerBounds.Window)
    val modifier = Modifier.shimmer(shimmer)
        .background(
            MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = MaterialTheme.shapes.extraSmall
        )

    Text("    ", modifier, style = MaterialTheme.typography.labelMedium)
    Text("               ", modifier, style = MaterialTheme.typography.labelMedium)
    Text("                      ", modifier, style = MaterialTheme.typography.labelMedium)
    Text("            ", modifier, style = MaterialTheme.typography.labelMedium)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainComponent(
    appBarState: MainAppBarState,
    snackbarHostState: SnackbarHostState,
    onTransactionClicked: (String) -> Unit,
    onHistoryClicked: () -> Unit,
) {
    rememberKoinModules {
        listOf(module {
            viewModelOf(::MainViewModel)
        })
    }

    val viewModel = koinViewModel<MainViewModel>()
    val state: State<MainUiState> = viewModel.observe.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    connectToAppBarState(
        state.value.selectedTransactions,
        appBarState,
        viewModel::onShowDeleteDialogClicked,
        viewModel::onContextMenuClosed
    )

    LaunchedEffect(state.value.errorMessage) {
        state.value.errorMessage?.let { string ->
            snackbarHostState.showSnackbar(
                string, null, false, SnackbarDuration.Short
            )
        }
    }

    DisposableEffect(Unit) {
        val profileAction = Action("Profile", Icons.Default.Person) {
            viewModel.onProfileClicked()
        }

        val historyAction = Action("History", Icons.AutoMirrored.Default.List) {
            onHistoryClicked()
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    coroutineScope.launch {
                        appBarState.showAction(profileAction)
                        appBarState.showAction(historyAction)
                    }
                }

                Lifecycle.Event.ON_STOP -> {
                    coroutineScope.launch {
                        appBarState.removeAction(profileAction)
                        appBarState.removeAction(historyAction)
                    }
                }

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AnimatedVisibility(state.value.showProfile) {
        Popup(
            alignment = Alignment.TopEnd,
            onDismissRequest = {
                viewModel.onProfileDismiss()
            },
        ) {
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors()
                    .copy(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                Column(Modifier.width(IntrinsicSize.Min)) {
                    ListItem(
                        { Text("Logout") }, trailingContent = {
                            Icon(
                                Icons.Default.KeyboardArrowRight, "Logout"
                            )
                        }, colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        ), modifier = Modifier.testTag("logout").clickable {
                            viewModel.onLogoutClicked()
                        })
                }
            }

        }
    }

    TransactionDeleteDialog(
        state.value.showDeleteDialog, viewModel::onDeleteClicked, viewModel::onDismissDeleteDialog
    )

    LazyColumn(modifier = Modifier.fillMaxHeight().testTag("transactions")) {
        item {
            val handle = LocalPinnableContainer.current?.pin()
            ChartComponent()
        }

        item {
            Column(
                Modifier.padding(
                    start = 24.dp, top = 12.dp, bottom = 16.dp, end = 24.dp
                ), verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (state.value.loading) {
                    FutureInfoShimmer()
                } else {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.secondary) {
                        Text(
                            state.value.futureInformation, style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }

        state.value.transactions.forEach { day ->
            val (date, list) = day
            TransactionsDay(
                date = date,
                list = list,
                selectedTransactions = state.value.selectedTransactions,
                contextMode = appBarState.contextMode,
                loadingMode = state.value.loading,
                onSelected = viewModel::onTransactionSelected,
                onClick = { onTransactionClicked(it.id) })
        }

        item {
            Spacer(Modifier.height(76.dp))
        }
    }
}

@Composable
fun TransactionDeleteDialog(
    showDeleteDialog: Boolean, onDelete: () -> Unit, onDismiss: () -> Unit
) {
    if (showDeleteDialog) {
        AlertDialog(
            title = { Text("Delete selected transactions?") },
            text = { Text("This action cannot be undone later") },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onDelete) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            })
    }
}

@Composable
fun connectToAppBarState(
    selected: ImmutableList<TransactionUiItem>,
    appBarState: MainAppBarState,
    onDeleteClicked: () -> Unit,
    onContextMenuClosed: () -> Unit
) {
    val actions = remember {
        listOf(Action("Delete", Icons.Default.Delete, onDeleteClicked)).toImmutableSet()
    }

    LaunchedEffect(selected) {
        if (selected.isEmpty()) {
            appBarState.closeContextMenu()
        } else {
            appBarState.contextTitle.value = getPluralString(Res.plurals.transactions, selected.size, selected.size)
            appBarState.showContextMenu(actions)
        }
    }

    LaunchedEffect(appBarState.contextMode) {
        if (!appBarState.contextMode) {
            onContextMenuClosed()
        }
    }
}
