package ru.workinprogress.feature.transaction.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.workinprogress.feature.currency.GetCurrentCurrencyUseCase
import ru.workinprogress.feature.transaction.domain.DeleteTransactionsUseCase
import ru.workinprogress.feature.transaction.domain.GetTransactionsUseCase
import ru.workinprogress.feature.transaction.simulate
import ru.workinprogress.feature.transaction.ui.component.TransactionListUiState
import ru.workinprogress.feature.transaction.ui.component.showData
import ru.workinprogress.feature.transaction.ui.component.showError
import ru.workinprogress.feature.transaction.ui.component.showLoading
import ru.workinprogress.feature.transaction.ui.model.TransactionUiItem
import ru.workinprogress.mani.defaultMinDate
import ru.workinprogress.useCase.UseCase

class TransactionsViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase,
    private val deleteTransactionsUseCase: DeleteTransactionsUseCase,
) : ViewModel() {

    private val state = MutableStateFlow(TransactionListUiState())
    val observe = state.asStateFlow()

    init {
        load()
    }

     fun load() {
        viewModelScope.launch {
            state.showLoading()

            val currency = getCurrentCurrencyUseCase.get()
            when (val result = getTransactionsUseCase()) {
                is UseCase.Result.Error -> {
                    state.showError(result.throwable.message.orEmpty())
                }

                is UseCase.Result.Success -> {
                    result.data
                        .map { transactions ->
                            transactions.run { simulate() }
                                .filterValues { transactions -> transactions.isNotEmpty() }
                                .filterKeys { defaultMinDate < it }.mapValues { entry ->
                                    entry.value.map { transaction ->
                                        TransactionUiItem(transaction, currency)
                                    }.toImmutableList()
                                }.toImmutableMap()
                        }.flowOn(Dispatchers.Default).collectLatest {
                            state.showData(it)
                        }
                }
            }
        }
    }

    fun onTransactionSelected(transactionUiItem: TransactionUiItem) {
        if (transactionUiItem in state.value.selectedTransactions) {
            state.update { state ->
                state.copy(
                    selectedTransactions = (state.selectedTransactions - transactionUiItem).toImmutableList()
                )
            }
        } else {
            state.update { state ->
                state.copy(
                    selectedTransactions = (state.selectedTransactions + transactionUiItem).toImmutableList()
                )
            }
        }
    }

    fun onShowDeleteDialogClicked() {
        state.update {
            it.copy(showDeleteDialog = true)
        }
    }

    fun onContextMenuClosed() {
        state.update { state ->
            state.copy(
                selectedTransactions = emptyList<TransactionUiItem>().toImmutableList()
            )
        }
    }

    fun onDismissDeleteDialog() {
        state.update {
            it.copy(showDeleteDialog = false)
        }
    }

    fun onDeleteClicked() {
        viewModelScope.launch {
            val selected = state.value.selectedTransactions.map { it.id }
            state.update { state ->
                state.copy(
                    showDeleteDialog = false,
                    selectedTransactions = emptyList<TransactionUiItem>().toImmutableList()
                )
            }

            deleteTransactionsUseCase(selected)
        }
    }

}