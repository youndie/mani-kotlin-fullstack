package ru.workinprogress.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.workinprogress.feature.main.ui.MainUiState
import ru.workinprogress.feature.transaction.domain.DeleteTransactionsUseCase
import ru.workinprogress.feature.transaction.domain.GetTransactionsUseCase
import ru.workinprogress.feature.transaction.simulate
import ru.workinprogress.feature.transaction.ui.model.TransactionUiItem
import ru.workinprogress.mani.defaultMinDate
import ru.workinprogress.useCase.UseCase


class MainViewModel(
    private val transactionsUseCase: GetTransactionsUseCase,
    private val deleteTransactionsUseCase: DeleteTransactionsUseCase,
) : ViewModel() {
    private val state = MutableStateFlow(MainUiState())
    val observe = state.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                load()
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

    fun onContextMenuClosed() {
        state.update { state ->
            state.copy(
                selectedTransactions = emptyList<TransactionUiItem>().toImmutableList()
            )
        }
    }

    fun onShowDeleteDialogClicked() {
        state.update {
            it.copy(showDeleteDialog = true)
        }
    }

    fun onDismissDeleteDialog() {
        state.update {
            it.copy(showDeleteDialog = false)
        }
    }

    private suspend fun load() {
        when (val result = transactionsUseCase()) {
            is UseCase.Result.Error -> {
                state.update { state ->
                    state.copy(errorMessage = result.throwable.message)
                }
            }

            is UseCase.Result.Success -> {
                result.data.flowOn(Dispatchers.IO).collectLatest { transactions ->
                    state.update { state ->
                        state.copy(transactions = transactions.run { simulate() }
                            .filterValues { transactions -> transactions.isNotEmpty() }
                            .filterKeys { defaultMinDate < it }.mapValues { entry ->
                                entry.value.map { transaction ->
                                    TransactionUiItem(transaction)
                                }.toImmutableList()
                            }.toImmutableMap())
                    }
                }
            }
        }
    }


}

