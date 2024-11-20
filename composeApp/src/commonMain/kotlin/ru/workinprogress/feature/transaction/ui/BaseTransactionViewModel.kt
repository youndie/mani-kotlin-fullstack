package ru.workinprogress.feature.transaction.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import ru.workinprogress.feature.transaction.DEFAULT_PERIOD_UNIT
import ru.workinprogress.feature.transaction.DEFAULT_PERIOD_VALUE
import ru.workinprogress.feature.transaction.Transaction
import ru.workinprogress.feature.transaction.amountSigned
import ru.workinprogress.feature.transaction.defaultPeriodAppend
import ru.workinprogress.feature.transaction.simulate
import ru.workinprogress.feature.transaction.ui.model.TransactionUiState
import ru.workinprogress.feature.transaction.ui.model.buildColoredAmount
import ru.workinprogress.mani.orToday
import ru.workinprogress.mani.today

abstract class BaseTransactionViewModel : ViewModel() {

    private val initialState: TransactionUiState = TransactionUiState()
    protected val state = MutableStateFlow(initialState)

    val observe = state.map { it.addFutureInformation() }
        .stateIn(viewModelScope, SharingStarted.Lazily, initialState)

    abstract fun onSubmitClicked()

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

    private fun TransactionUiState.addFutureInformation() =
        this.copy(futureInformation = buildFutureInformation(this))

    private fun buildFutureInformation(
        state: TransactionUiState,
    ): AnnotatedString {
        val currency = state.currency
        return buildAnnotatedString {
            append(
                buildColoredAmount(
                    amount = state.amount,
                    currency = state.currency,
                    sign = state.income
                )
            )

            if (state.period == Transaction.Period.OneTime) {
                this.append(" on ")
            } else {
                append(" from ")
            }

            append("${state.date.value ?: today()}")

            if (state.period == Transaction.Period.OneTime) {
                return@buildAnnotatedString
            }

            fun proceedSimulate(simulation: Map<LocalDate, List<Transaction>>) {
                append(simulation.count { entry -> entry.value.isNotEmpty() }.toString())
                append(" times,")
                append(
                    " total: "
                )
                append(buildColoredAmount(simulation.flatMap { it.value }
                    .sumOf { transaction -> transaction.amountSigned }, currency))
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
                        state.date.value.orToday, defaultPeriodAppend(state.date.value.orToday)
                    )
                })
            }
        }
    }

}