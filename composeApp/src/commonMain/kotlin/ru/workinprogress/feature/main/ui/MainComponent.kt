package ru.workinprogress.feature.main.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LocalPinnableContainer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ru.workinprogress.feature.chart.ui.ChartComponent
import ru.workinprogress.feature.main.MainViewModel
import ru.workinprogress.feature.transaction.ui.component.TransactionItem
import ru.workinprogress.mani.components.Action
import ru.workinprogress.mani.components.MainAppBarState


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainComponent(appBarState: MainAppBarState) {
    val viewModel = koinViewModel<MainViewModel>()
    val state: State<MainUiState> = viewModel.observe.collectAsStateWithLifecycle()

    connectToAppBarState(
        state.value,
        appBarState,
        viewModel::onShowDeleteDialogClicked,
        viewModel::onContextMenuClosed
    )

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
                            appBarState.contextMode.value,
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
        appBarState.contextMode.value = selected.isNotEmpty()
        appBarState.actions.value = if (selected.isEmpty()) {
            emptyList()
        } else {
            listOf(Action("Delete", Icons.Default.Delete, onDeleteClicked))
        }
        if (selected.isNotEmpty()) {
            appBarState.contextTitle.value = "${selected.size} transactions"
        }
    }

    LaunchedEffect(appBarState.contextMode.value) {
        if (!appBarState.contextMode.value) {
            onContextMenuClosed()
        }
    }
}
