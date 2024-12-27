package ru.workinprogress.feature.transaction.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import ru.workinprogress.feature.categories.domain.AddCategoryUseCase
import ru.workinprogress.feature.categories.domain.DeleteCategoryUseCase
import ru.workinprogress.feature.categories.domain.ObserveCategoriesUseCase
import ru.workinprogress.feature.transaction.*
import ru.workinprogress.feature.transaction.ui.component.formatted
import ru.workinprogress.feature.transaction.ui.component.model.TransactionAction
import ru.workinprogress.feature.transaction.ui.model.TransactionUiState
import ru.workinprogress.feature.transaction.ui.model.buildColoredAmount
import ru.workinprogress.mani.orToday
import ru.workinprogress.mani.today
import ru.workinprogress.useCase.UseCase

abstract class BaseTransactionViewModel(
    private val addCategoryUseCase: AddCategoryUseCase,
    private val observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
) : ViewModel() {

    protected open val state = MutableStateFlow(TransactionUiState())
    val observe get() = state.asStateFlow()

    abstract fun onSubmitClicked()

    protected fun observeCategories() {
        viewModelScope.launch {
            observeCategoriesUseCase.observe.collectLatest { value ->
                state.update { state ->
                    state.copy(categories = (value + Category.default).toImmutableSet())
                }
            }
        }
    }

    fun onAction(action: TransactionAction) {
        when (action) {
            is TransactionAction.AmountChanged -> onAmountChanged(action.amount)
            is TransactionAction.CategoryChanged -> onCategoryChanged(action.category)
            is TransactionAction.CategoryCreate -> onCategoryCreate(action.name)
            is TransactionAction.CategoryDelete -> onCategoryDelete(action.category)
            is TransactionAction.CommentChanged -> onCommentChanged(action.comment)
            is TransactionAction.DateSelected -> onDateSelected(action.date)
            is TransactionAction.DateUntilSelected -> onDateUntilSelected(action.date)
            TransactionAction.ExpandCategoryClicked -> onExpandCategoryClicked()
            TransactionAction.ExpandPeriodClicked -> onExpandPeriodClicked()
            is TransactionAction.IncomeChanged -> onIncomeChanged(action.income)
            is TransactionAction.PeriodChanged -> onPeriodChanged(action.period)
            TransactionAction.SubmitClicked -> onSubmitClicked()
            TransactionAction.ToggleDatePicker -> onToggleDatePicker()
            TransactionAction.ToggleUntilDatePicker -> onToggleUntilDatePicker()
        }
    }

    internal fun onAmountChanged(amount: String) {
        if (amount.toDoubleOrNull() != null || amount.isEmpty()) {
            state.update { state ->
                state.copy(amount = amount).addFutureInformation()
            }
        }
    }

    internal fun onCommentChanged(comment: String) = state.update { state ->
        state.copy(comment = comment)
    }

    internal fun onIncomeChanged(income: Boolean) = state.update { state ->
        state.copy(income = income).addFutureInformation()
    }

    internal fun onPeriodChanged(period: Transaction.Period) = state.update { state ->
        state.copy(period = period).addFutureInformation()
    }

    internal fun onExpandPeriodClicked() = state.update { state ->
        state.copy(periods = Transaction.Period.entries.toImmutableList())
    }

    internal fun onExpandCategoryClicked() {}

    internal fun onToggleDatePicker() = state.update { state ->
        state.copy(date = state.date.copy(showDatePicker = state.date.showDatePicker.not()))
    }

    internal fun onToggleUntilDatePicker() = state.update { state ->
        state.copy(until = state.until.copy(showDatePicker = state.until.showDatePicker.not()))
    }

    internal fun onDateSelected(date: LocalDate) = state.update { state ->
        state.copy(date = state.date.copy(value = date, showDatePicker = false)).addFutureInformation()
    }

    internal fun onDateUntilSelected(date: LocalDate) = state.update { state ->
        state.copy(until = state.until.copy(value = date, showDatePicker = false)).addFutureInformation()
    }

    internal fun onCategoryChanged(category: Category) = state.update { state ->
        state.copy(category = category)
    }

    internal fun onCategoryCreate(name: String) {
        viewModelScope.launch {
            val new = Category("", name = name)
            state.update {
                it.copy(category = new)
            }

            val result = withContext(Dispatchers.Default) { addCategoryUseCase(new) }

            when (result) {
                is UseCase.Result.Error -> {
                    state.update {
                        it.copy(category = Category.default, errorMessage = result.throwable.message)
                    }
                }

                is UseCase.Result.Success -> {
                    state.update {
                        it.copy(category = result.data)
                    }
                }
            }

        }
    }

    internal fun onCategoryDelete(category: Category?) {
        (state.value.categories - category).firstOrNull()?.let {
            onCategoryChanged(it)
        }
        category?.let {
            viewModelScope.launch {
                if (deleteCategoryUseCase(category) !is UseCase.Result.Success) {
                    onCategoryChanged(category)
                }
            }
        }
    }

    private fun TransactionUiState.addFutureInformation() = copy(futureInformation = buildFutureInformation(this))

    private fun buildFutureInformation(
        state: TransactionUiState,
    ): AnnotatedString {
        val currency = state.currency
        return buildAnnotatedString {
            append(
                buildColoredAmount(
                    amount = state.amount, currency = state.currency, sign = state.income
                )
            )

            if (state.period == Transaction.Period.OneTime) {
                this.append(" on ")
            } else {
                append(" from ")
            }

            append(state.date.value?.formatted ?: today().formatted)

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
                append("${state.until.value?.formatted}")
                append(" repeat ")

                proceedSimulate(listOf(state.tempTransaction).run {
                    simulate(state.date.value.orToday, state.until.value)
                })
            } else {
                when (state.period) {
                    Transaction.Period.Month, Transaction.Period.ThreeMonth, Transaction.Period.HalfYear, Transaction.Period.Year -> {
                        this.append(
                            ". In $LARGE_PERIOD_VALUE ${
                                LARGE_PERIOD_UNIT.toString().lowercase()
                            }'s repeat "
                        )
                        proceedSimulate(listOf(state.tempTransaction).run {
                            simulate(
                                state.date.value.orToday, largePeriodAppend(state.date.value.orToday)
                            )
                        })
                    }

                    else -> {
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
    }
}