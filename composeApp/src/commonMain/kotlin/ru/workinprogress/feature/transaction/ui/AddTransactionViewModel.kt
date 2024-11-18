package ru.workinprogress.feature.transaction.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import ru.workinprogress.feature.currency.GetCurrentCurrencyUseCase
import ru.workinprogress.feature.transaction.DEFAULT_PERIOD_UNIT
import ru.workinprogress.feature.transaction.DEFAULT_PERIOD_VALUE
import ru.workinprogress.feature.transaction.Transaction
import ru.workinprogress.feature.transaction.Transaction.Period
import ru.workinprogress.feature.transaction.amountSigned
import ru.workinprogress.feature.transaction.defaultPeriodAppend
import ru.workinprogress.feature.transaction.domain.AddTransactionUseCase
import ru.workinprogress.feature.transaction.simulate
import ru.workinprogress.feature.transaction.ui.model.AddTransactionUiState
import ru.workinprogress.mani.orToday
import ru.workinprogress.mani.today
import ru.workinprogress.useCase.UseCase

class AddTransactionViewModel(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase,
) : ViewModel() {
    private val state = MutableStateFlow(AddTransactionUiState())

    val observe =
        state.map { it.addFutureInformation() }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.Lazily, AddTransactionUiState())

    init {
        viewModelScope.launch(Dispatchers.Default) {
            state.update { it.copy(currency = getCurrentCurrencyUseCase.get()) }
        }
    }

    fun onCreateClicked() {
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

    private fun AddTransactionUiState.addFutureInformation() =
        this.copy(futureInformation = buildFutureInformation(this))

    private fun buildFutureInformation(
        state: AddTransactionUiState,
    ): AnnotatedString {
        val currency = state.currency
        return buildAnnotatedString {
            withStyle(style = SpanStyle(color = if (state.income) Color.Green else Color.Red)) {
                append(if (state.income) "+${state.amount} ${currency.symbol}" else "-${state.amount} ${currency.symbol}")
            }

            if (state.period == Period.OneTime) {
                this.append(" on ")
            } else {
                append(" from ")
            }

            append("${state.date.value ?: today()}")

            if (state.period == Period.OneTime) {
                return@buildAnnotatedString
            }

            fun proceedSimulate(simulation: Map<LocalDate, List<Transaction>>) {
                append(simulation.count { entry -> entry.value.isNotEmpty() }.toString())
                append(" times,")
                append(
                    " total: ${
                        simulation.flatMap { it.value }
                            .sumOf { transaction -> transaction.amountSigned }.toInt()
                    } ${currency.symbol}"
                )
            }
            if (state.until.value != null) {
                append(" to ")
                append("${state.until.value}.")
                append(" Repeat ")

                proceedSimulate(listOf(state.tempTransaction).run {
                    simulate(state.date.value.orToday, state.until.value)
                })
            } else {
                this.append(
                    ". In $DEFAULT_PERIOD_VALUE ${
                        DEFAULT_PERIOD_UNIT.toString().lowercase()
                    }'s repeat "
                )
                proceedSimulate(listOf(state.tempTransaction).run {
                    simulate(
                        state.date.value.orToday,
                        defaultPeriodAppend(state.date.value.orToday)
                    )
                })
            }
        }
    }

}