package ru.workinprogress.feature.main.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LocalPinnableContainer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import ru.workinprogress.feature.chart.ui.ChartComponent
import ru.workinprogress.feature.main.MainViewModel
import ru.workinprogress.feature.transaction.ui.component.TransactionItem
import ru.workinprogress.mani.components.Action
import ru.workinprogress.mani.components.MainAppBarState


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainComponent(appBarState: MainAppBarState, snackbarHostState: SnackbarHostState) {
    val viewModel = koinViewModel<MainViewModel>()
    val state: State<MainUiState> = viewModel.observe.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    connectToAppBarState(
        state.value,
        appBarState,
        viewModel::onShowDeleteDialogClicked,
        viewModel::onContextMenuClosed
    )

    LaunchedEffect(state.value.errorMessage) {
        state.value
            .errorMessage
            ?.let { string ->
                snackbarHostState.showSnackbar(string, null, false, SnackbarDuration.Short)
            }
    }

    if (state.value.showProfile) {
        Popup(
            alignment = Alignment.TopEnd,
            onDismissRequest = {
                viewModel.onProfileDismiss()
            },
        ) {
            Card(modifier = Modifier.padding(16.dp)) {
                Column(Modifier.width(IntrinsicSize.Min)) {
                    ListItem({ Text("Logout") }, modifier = Modifier.clickable {
                        viewModel.onLogoutClicked()
                    })
                }
            }

        }
    }



    DisposableEffect(Unit) {
        val profileAction = Action("Profile", Icons.Default.Person) {
            viewModel.onProfileClicked()
        }
        val observer = LifecycleEventObserver { lifecycleOwner, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    coroutineScope.launch {
                        appBarState.showAction(profileAction)
                    }
                }

                Lifecycle.Event.ON_STOP -> {
                    coroutineScope.launch {
                        appBarState.removeAction(profileAction)
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

    if (state.value.showDeleteDialog) {
        AlertDialog(
            title = { Text("Delete selected transactions?") },
            text = { Text("This action cannot be undone later") },
            onDismissRequest = viewModel::onDismissDeleteDialog,
            confirmButton = {
                TextButton(onClick = viewModel::onDeleteClicked) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDeleteDialog) {
                    Text("Cancel")
                }
            })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.height(1024.dp)) {
            item {
                val handle = LocalPinnableContainer.current?.pin()
                ChartComponent()
            }
            state.value.transactions.forEach { day ->
                stickyHeader(day.key.toString()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(vertical = 4.dp, horizontal = 6.dp)
                        ) {
                            Text(day.key.toString(), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                day.value.forEach { transaction ->
                    item {
                        TransactionItem(
                            transaction,
                            transaction in state.value.selectedTransactions,
                            appBarState.contextMode,
                            viewModel::onTransactionSelected
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(76.dp))
            }
        }
    }
}

@Composable
fun connectToAppBarState(
    mainUiState: MainUiState,
    appBarState: MainAppBarState,
    onDeleteClicked: () -> Unit,
    onContextMenuClosed: () -> Unit
) {
    LaunchedEffect(mainUiState.selectedTransactions) {
        val selected = mainUiState.selectedTransactions
        if (selected.isEmpty()) {
            appBarState.closeContextMenu()
        } else {
            appBarState.contextTitle.value = "${selected.size} transactions"
            appBarState.showContextMenu(listOf(Action("Delete", Icons.Default.Delete, onDeleteClicked)))
        }
    }

    LaunchedEffect(appBarState.contextMode) {
        if (!appBarState.contextMode) {
            onContextMenuClosed()
        }
    }
}
