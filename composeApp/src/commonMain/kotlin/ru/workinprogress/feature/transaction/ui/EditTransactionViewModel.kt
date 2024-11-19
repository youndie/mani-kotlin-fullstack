package ru.workinprogress.feature.transaction.ui

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.workinprogress.feature.currency.GetCurrentCurrencyUseCase
import ru.workinprogress.feature.transaction.domain.GetTransactionUseCase
import ru.workinprogress.feature.transaction.domain.UpdateTransactionUseCase
import ru.workinprogress.feature.transaction.ui.model.TransactionUiState
import ru.workinprogress.mani.navigation.TransactionRoute
import ru.workinprogress.useCase.UseCase

class EditTransactionViewModel(
    private val route: TransactionRoute,
    private val getTransactionUseCase: GetTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase
) : BaseTransactionViewModel() {

    init {
        viewModelScope.launch(Dispatchers.Default) {
            val transaction = getTransactionUseCase.get(route.id)
            state.update {
                TransactionUiState(
                    transaction,
                    currency = getCurrentCurrencyUseCase.get(),
                ).copy(edit = true)
            }
        }
    }

    override fun onSubmitClicked() {
        viewModelScope.launch {
            val updated = updateTransactionUseCase(state.value.tempTransaction)
            if (updated is UseCase.Result.Success) {
                state.update { TransactionUiState(success = true) }
            }
        }
    }
}