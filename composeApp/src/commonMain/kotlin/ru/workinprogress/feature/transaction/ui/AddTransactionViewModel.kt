package ru.workinprogress.feature.transaction.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.workinprogress.feature.currency.GetCurrentCurrencyUseCase
import ru.workinprogress.feature.transaction.Transaction
import ru.workinprogress.feature.transaction.domain.AddTransactionUseCase
import ru.workinprogress.feature.transaction.ui.model.TransactionUiState
import ru.workinprogress.mani.today
import ru.workinprogress.useCase.UseCase

class AddTransactionViewModel(
    private val addTransactionUseCase: AddTransactionUseCase,
    getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase
) : BaseTransactionViewModel() {

    init {
        viewModelScope.launch(Dispatchers.Default) {
            state.update {
                it.copy(currency = getCurrentCurrencyUseCase.get())
            }
        }
    }

    override fun onSubmitClicked() {
        val stateValue = state.value

        viewModelScope.launch {
            val result = addTransactionUseCase.invoke(
                Transaction(
                    id = "",
                    amount = stateValue.amount.toDouble(),
                    income = stateValue.income,
                    period = stateValue.period,
                    date = stateValue.date.value ?: today(),
                    until = stateValue.until.value,
                    comment = stateValue.comment
                )
            )
            when (result) {
                is UseCase.Result.Error -> {
                    println(result.throwable.message)
                }

                is UseCase.Result.Success -> {
                    state.update { TransactionUiState(success = true) }
                }
            }
        }
    }
}

