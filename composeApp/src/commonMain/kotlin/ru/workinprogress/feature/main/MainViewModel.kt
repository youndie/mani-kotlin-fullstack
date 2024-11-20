package ru.workinprogress.feature.main

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import ru.workinprogress.feature.auth.domain.LogoutUseCase
import ru.workinprogress.feature.currency.Currency
import ru.workinprogress.feature.currency.GetCurrentCurrencyUseCase
import ru.workinprogress.feature.main.ui.MainUiState
import ru.workinprogress.feature.transaction.Transaction
import ru.workinprogress.feature.transaction.amountSigned
import ru.workinprogress.feature.transaction.domain.DeleteTransactionsUseCase
import ru.workinprogress.feature.transaction.domain.GetTransactionsUseCase
import ru.workinprogress.feature.transaction.simulate
import ru.workinprogress.feature.transaction.ui.model.NegativeColor
import ru.workinprogress.feature.transaction.ui.model.PositiveColor
import ru.workinprogress.feature.transaction.ui.model.TransactionUiItem
import ru.workinprogress.feature.transaction.ui.model.TransactionsByDays
import ru.workinprogress.feature.transaction.ui.model.buildColoredAmount
import ru.workinprogress.mani.today
import ru.workinprogress.useCase.UseCase
import kotlin.math.sign


class MainViewModel(
    private val transactionsUseCase: GetTransactionsUseCase,
    private val deleteTransactionsUseCase: DeleteTransactionsUseCase,
    private val getCurrencyUseCase: GetCurrentCurrencyUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    private lateinit var currency: Currency

    private val state = MutableStateFlow(MainUiState())
    val observe = state.asStateFlow()

    init {
        viewModelScope.launch { load() }
        viewModelScope.launch {
            currency = getCurrencyUseCase.get()
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


    private fun findZeroEvents(transactionsByDays: Map<LocalDate, List<Transaction>>): Pair<LocalDate?, LocalDate?> {
        var positiveDate: LocalDate? = null
        var negativeDate: LocalDate? = null

        transactionsByDays.entries.runningFoldIndexed(
            0.toDouble(),
            { index, acc, item ->
                val nextValue = acc + item.value.sumOf { transaction ->
                    transaction.amountSigned
                }

                if (index == 0) return@runningFoldIndexed nextValue

                if (nextValue.sign != acc.sign) {
                    if (nextValue.sign > acc.sign) {
                        positiveDate = item.value.first().date
                    } else {
                        negativeDate = item.value.first().date
                    }
                }

                if (positiveDate != null && negativeDate != null) {
                    return positiveDate to negativeDate
                }

                nextValue
            })

        return positiveDate to negativeDate
    }

    private fun dispatch(transactions: List<Transaction>) {
        val simulationResult = transactions.run { simulate() }

        state.update { state ->
            state.copy(
                transactions = simulationResult
                    .filterValues { transactions -> transactions.isNotEmpty() }
                    .filterKeys { today() <= it }
                    .mapValues { entry ->
                        entry.value.map { transaction ->
                            TransactionUiItem(transaction, currency)
                        }.toImmutableList()
                    }.toImmutableMap(),
                futureInformation = buildFutureInformation(simulationResult)
            )
        }
    }

    private fun Map<LocalDate, List<Transaction>>.sumByMonth(monthDate: LocalDate): Double =
        this
            .filter {
                it.key.monthNumber == monthDate.monthNumber &&
                        it.key.year == monthDate.year
            }.flatMap { it.value }.sumOf { it.amountSigned }

    private fun buildFutureInformation(
        simulationResult: Map<LocalDate, List<Transaction>>
    ) = buildAnnotatedString {
        val filteredTransactions =
            simulationResult.filterValues { transactions -> transactions.isNotEmpty() }
                .filterKeys { today() <= it }

        val todayAmount = simulationResult.entries
            .runningFold(0.0) { acc, entry ->
                if (entry.key > today()) acc
                else acc + entry.value.sumOf { it.amountSigned }
            }.last()

        val firstOfTheFirst = filteredTransactions.entries.first().value.first()



        append("today amount: ")
        append(buildColoredAmount(todayAmount, currency, useSign = false))
        append("\n")
        append("next transaction ${firstOfTheFirst.date}: ")
        append(buildColoredAmount(firstOfTheFirst.amount, currency))
        append("\n")

        append(
            "in month: "
        )
        append(
            buildColoredAmount(
                simulationResult.sumByMonth(today()),
                currency
            )
        )
        append(
            " in next month: "
        )
        append(
            buildColoredAmount(
                simulationResult.sumByMonth(today().plus(1, DateTimeUnit.MONTH)), currency
            )
        )
        append("\n")

        val (positiveDate, negativeDate) = findZeroEvents(simulationResult)

        if (positiveDate == null && negativeDate == null) {
            append("no zero events")
        } else {
            append("next zero event: ")

            when {
                (todayAmount > 0 && negativeDate != null) -> {
                    withStyle(style = SpanStyle(color = NegativeColor)) {
                        append(negativeDate.toString())
                    }
                }

                (todayAmount < 0 && positiveDate != null) -> {
                    withStyle(style = SpanStyle(color = PositiveColor)) {
                        append(positiveDate.toString())
                    }
                }
            }
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
                result.data
                    .flowOn(Dispatchers.Default)
                    .collectLatest(::dispatch)
            }
        }
    }

    fun onProfileClicked() {
        state.update { state ->
            state.copy(showProfile = true)
        }
    }

    fun onProfileDismiss() {
        state.update { state ->
            state.copy(showProfile = false)
        }
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            logoutUseCase()
            state.value = MainUiState()
        }
    }


    fun onHistoryClicked() {

    }

}

