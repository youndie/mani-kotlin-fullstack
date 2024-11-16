package ru.workinprogress.feature.transaction.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import ru.workinprogress.feature.transaction.CreateTransactionParams
import ru.workinprogress.feature.transaction.Transaction
import ru.workinprogress.feature.transaction.domain.AddTransactionUseCase
import ru.workinprogress.feature.transaction.ui.model.AddTransactionUiState
import ru.workinprogress.mani.today
import ru.workinprogress.useCase.UseCase

class AddTransactionViewModel(
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {
    private val state = MutableStateFlow<AddTransactionUiState>(AddTransactionUiState.Prepare())
    val observe = state.asStateFlow()

    fun onCreateClicked() {
        val stateValue = state.value as? AddTransactionUiState.Prepare ?: return

        viewModelScope.launch {
            val result = addTransactionUseCase.invoke(
                CreateTransactionParams(
                    amount = stateValue.amount.toDouble(),
                    income = stateValue.income,
                    period = stateValue.period,
                    date = stateValue.date.date ?: today(),
                    until = stateValue.until.date,
                    comment = stateValue.comment
                )
            )
            when (result) {
                is UseCase.Result.Error -> {
                    println(result.throwable.message)
                }

                is UseCase.Result.Success -> {
                    state.update { AddTransactionUiState.Finish }
                }
            }
        }
    }

    fun onAmountChanged(amount: String) {
        if (amount.toDoubleOrNull() != null || amount.isEmpty()) {
            updatePrepareState { state ->
                state.copy(amount = amount)
            }
        }
    }

    fun onCommentChanged(comment: String) = updatePrepareState { state ->
        state.copy(comment = comment)
    }

    fun onIncomeChanged(income: Boolean) = updatePrepareState { state ->
        state.copy(income = income)
    }

    fun onPeriodChanged(period: Transaction.Period) = updatePrepareState { state ->
        state.copy(period = period)
    }

    fun onToggleDatePicker() = updatePrepareState { state ->
        state.copy(date = state.date.copy(showDatePicker = state.date.showDatePicker.not()))
    }

    fun onToggleUntilDatePicker() = updatePrepareState { state ->
        state.copy(date = state.until.copy(showDatePicker = state.date.showDatePicker.not()))
    }

    fun onDateSelected(date: LocalDate) = updatePrepareState { state ->
        state.copy(date = state.date.copy(date = date))
    }

    fun onDateUntilSelected(date: LocalDate) = updatePrepareState { state ->
        state.copy(date = state.until.copy(date = date))
    }

    private inline fun updatePrepareState(func: (AddTransactionUiState.Prepare) -> (AddTransactionUiState.Prepare)) {
        (observe.value as? AddTransactionUiState.Prepare)?.let {
            state.value = func(it)
        }
    }

}