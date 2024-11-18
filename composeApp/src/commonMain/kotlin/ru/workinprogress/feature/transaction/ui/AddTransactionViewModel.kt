package ru.workinprogress.feature.transaction.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
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
    private val state = MutableStateFlow<AddTransactionUiState>(AddTransactionUiState())
    val observe = state.asStateFlow()

    fun onCreateClicked() {
        val stateValue = state.value

        viewModelScope.launch {
            val result = addTransactionUseCase.invoke(
                CreateTransactionParams(
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
                    state.update { AddTransactionUiState(success = true) }
                }
            }
        }
    }

    fun onAmountChanged(amount: String) {
        if (amount.toDoubleOrNull() != null || amount.isEmpty()) {
            state.update { state ->
                state.copy(amount = amount)
            }
        }
    }

    fun onCommentChanged(comment: String) = state.update { state ->
        state.copy(comment = comment)
    }

    fun onIncomeChanged(income: Boolean) = state.update { state ->
        state.copy(income = income)
    }

    fun onPeriodChanged(period: Transaction.Period) = state.update { state ->
        state.copy(period = period)
    }

    fun onExpandPeriodClicked() = state.update { state ->
        state.copy(periods = Transaction.Period.entries.toImmutableList())
    }

    fun onToggleDatePicker() = state.update { state ->
        state.copy(date = state.date.copy(showDatePicker = state.date.showDatePicker.not()))
    }

    fun onToggleUntilDatePicker() = state.update { state ->
        state.copy(until = state.until.copy(showDatePicker = state.until.showDatePicker.not()))
    }

    fun onDateSelected(date: LocalDate) = state.update { state ->
        state.copy(date = state.date.copy(value = date, showDatePicker = false))
    }

    fun onDateUntilSelected(date: LocalDate) = state.update { state ->
        state.copy(until = state.until.copy(value = date, showDatePicker = false))
    }
}