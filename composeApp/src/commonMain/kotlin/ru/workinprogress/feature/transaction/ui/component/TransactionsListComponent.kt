package ru.workinprogress.feature.transaction.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.module.rememberKoinModules
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.workinprogress.feature.main.ui.TransactionDeleteDialog
import ru.workinprogress.feature.main.ui.connectToAppBarState
import ru.workinprogress.feature.transaction.ui.TransactionsViewModel
import ru.workinprogress.feature.transaction.ui.model.TransactionUiItem
import ru.workinprogress.mani.components.MainAppBarState

interface LoadingState {
    val loading: Boolean

    fun load(): LoadingState
}

interface ErrorState {
    val errorMessage: String?

    fun showError(message: String): ErrorState
}

interface DataState<T> {
    val data: T

    fun showData(data: T): DataState<T>
}

inline fun <reified T : LoadingState> MutableStateFlow<T>.showLoading() {
    value = value.load() as T
}

inline fun <reified T : ErrorState> MutableStateFlow<T>.showError(message: String) {
    value = value.showError(message) as T
}

inline fun <T, reified R : DataState<T>> MutableStateFlow<R>.showData(data: T) {
    value = value.showData(data) as R
}

data class TransactionListUiState(
    override val data: ImmutableMap<LocalDate, ImmutableList<TransactionUiItem>> = emptyMap<LocalDate, ImmutableList<TransactionUiItem>>().toImmutableMap(),
    override val loading: Boolean = false,
    override val errorMessage: String? = null,
    val selectedTransactions: ImmutableList<TransactionUiItem> = emptyList<TransactionUiItem>().toImmutableList(),
    val showDeleteDialog: Boolean = false,
) : CommonUiState<ImmutableMap<LocalDate, ImmutableList<TransactionUiItem>>> {
    override fun load() = copy(loading = true)
    override fun showError(message: String) = copy(errorMessage = message, loading = false)
    override fun showData(data: ImmutableMap<LocalDate, ImmutableList<TransactionUiItem>>) =
        copy(data = data, loading = false)
}

interface CommonUiState<T> : DataState<T>, LoadingState, ErrorState

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun TransactionsListComponent(
    modifier: Modifier = Modifier,
    appBarState: MainAppBarState = remember { MainAppBarState() },
    onTransactionClicked: (String) -> Unit = {},
) {
    rememberKoinModules {
        listOf(module {
            viewModelOf(::TransactionsViewModel)
        })
    }

    val viewModel = koinViewModel<TransactionsViewModel>()
    val state by viewModel.observe.collectAsStateWithLifecycle()

    TransactionDeleteDialog(
        state.showDeleteDialog,
        viewModel::onDeleteClicked,
        viewModel::onDismissDeleteDialog
    )

    connectToAppBarState(
        state.selectedTransactions,
        appBarState,
        viewModel::onShowDeleteDialogClicked,
        viewModel::onContextMenuClosed
    )

    LazyColumn(modifier = modifier.fillMaxHeight().padding(vertical = 16.dp)) {
        state.data.forEach { day ->
            val (date, list) = day

            TransactionsDay(
                date = date,
                list = list,
                selectedTransactions = state.selectedTransactions,
                contextMode = appBarState.contextMode,
                onSelected = viewModel::onTransactionSelected,
                onClick = { onTransactionClicked(it.id) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.TransactionsDay(
    date: LocalDate,
    list: ImmutableList<TransactionUiItem>,
    selectedTransactions: ImmutableList<TransactionUiItem>,
    contextMode: Boolean,
    onSelected: (TransactionUiItem) -> Unit,
    onClick: (TransactionUiItem) -> Unit
) {
    stickyHeader(date.toString()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceContainer,
                        RoundedCornerShape(4.dp)
                    )
                    .padding(vertical = 4.dp, horizontal = 6.dp)
            ) {
                Text(
                    date.format(localDateFormat),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
    TransactionsListItems(
        list,
        selectedTransactions,
        contextMode,
        onSelected,
        onClick
    )
}

private fun LazyListScope.TransactionsListItems(
    list: ImmutableList<TransactionUiItem>,
    selectedTransactions: ImmutableList<TransactionUiItem>,
    contextMode: Boolean,
    onSelected: (TransactionUiItem) -> Unit,
    onClick: (TransactionUiItem) -> Unit
) {

    items(list) { transaction ->
        TransactionItem(
            Modifier,
            transaction,
            transaction in selectedTransactions,
            contextMode,
            onSelected,
            onClick,
        )
    }
}

private val localDateFormat = LocalDate.Format {
    dayOfMonth()
    char(' ')
    monthName(MonthNames.ENGLISH_FULL)
    char(' ')
    year()
}

