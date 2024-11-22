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
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.plus
import ru.workinprogress.feature.auth.domain.LogoutUseCase
import ru.workinprogress.feature.currency.Currency
import ru.workinprogress.feature.currency.GetCurrentCurrencyUseCase
import ru.workinprogress.feature.main.ui.MainUiState
import ru.workinprogress.feature.transaction.Transaction
import ru.workinprogress.feature.transaction.amountSigned
import ru.workinprogress.feature.transaction.domain.DeleteTransactionsUseCase
import ru.workinprogress.feature.transaction.domain.GetTransactionsUseCase
import ru.workinprogress.feature.transaction.findZeroEvents
import ru.workinprogress.feature.transaction.simulate
import ru.workinprogress.feature.transaction.ui.model.NegativeColor
import ru.workinprogress.feature.transaction.ui.model.PositiveColor
import ru.workinprogress.feature.transaction.ui.model.TransactionUiItem
import ru.workinprogress.feature.transaction.ui.model.buildColoredAmount
import ru.workinprogress.mani.today
import ru.workinprogress.useCase.UseCase


class MainViewModel(
    private val transactionsUseCase: GetTransactionsUseCase,
    private val deleteTransactionsUseCase: DeleteTransactionsUseCase,
    private val getCurrencyUseCase: GetCurrentCurrencyUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    private val loadingItems by lazy {
        mapOf(
            today() to (0..2).map {
                TransactionUiItem(
                    it.toString(),
                    0.0,
                    false,
                    date = today(),
                    until = null,
                    period = Transaction.Period.OneTime,
                    comment = "Loading",
                    currency = Currency.Usd
                )
            }.toImmutableList()
        ).toImmutableMap()
    }

    private val state = MutableStateFlow(MainUiState(loading = true, transactions = loadingItems))
    val observe = state.asStateFlow()

    private lateinit var currency: Currency

    init {
        viewModelScope.launch { load() }
        viewModelScope.launch {
            currency = getCurrencyUseCase.get()
        }
    }

    private suspend fun load() {
        when (val result = transactionsUseCase()) {
            is UseCase.Result.Error -> {
                state.update { state ->
                    state.copy(loading = false, errorMessage = result.throwable.message)
                }
            }

            is UseCase.Result.Success -> {
                result.data
                    .flowOn(Dispatchers.Default)
                    .collectLatest(::dispatch)
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

    private fun dispatch(transactions: List<Transaction>) {
        val simulationResult = transactions.run { simulate() }

        state.update { state ->
            state.copy(
                loading = false,
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

    private fun buildFutureInformation(simulationResult: Map<LocalDate, List<Transaction>>) =
        buildAnnotatedString {

            val localDateFormat = LocalDate.Format {
                dayOfMonth()
                char(' ')
                monthName(MonthNames.ENGLISH_ABBREVIATED)
                char(' ')
                year()
            }
            val filteredTransactions =
                simulationResult.filterValues { transactions -> transactions.isNotEmpty() }
                    .filterKeys { today() <= it }

            val todayAmount = simulationResult.entries
                .runningFold(0.0) { acc, entry ->
                    if (entry.key > today()) acc
                    else acc + entry.value.sumOf { it.amountSigned }
                }.last()

            val firstOfTheFirst = filteredTransactions.entries.first().value.first()

            append("balance: ")
            append(buildColoredAmount(todayAmount, currency))
            append("\n")
            append(
                "today balance change: "
            )
            append(buildColoredAmount(filteredTransactions.filter { it.key == today() }.entries.flatMap { it.value }
                .sumOf { it.amountSigned }, currency))
            append("\n")
            append("next transaction ${firstOfTheFirst.date.format(localDateFormat)}: ")
            append(buildColoredAmount(firstOfTheFirst.amountSigned, currency))
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
                ", in next month: "
            )
            append(
                buildColoredAmount(
                    simulationResult.sumByMonth(today().plus(1, DateTimeUnit.MONTH)), currency
                )
            )
            append("\n")

            val (positiveDate, negativeDate) = simulationResult.findZeroEvents()

            when {
                (todayAmount > 0 && negativeDate != null) -> {
                    append("balance will become ")

                    withStyle(style = SpanStyle(color = NegativeColor)) {
                        append("negative: ")
                    }

                    append(negativeDate.format(localDateFormat))
                }

                (todayAmount < 0 && positiveDate != null) -> {
                    append("balance will become ")

                    withStyle(style = SpanStyle(color = PositiveColor)) {
                        append("positive: ")
                    }

                    append(positiveDate.format(localDateFormat))
                }

                else -> {
                    append("no zero events")
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

}

