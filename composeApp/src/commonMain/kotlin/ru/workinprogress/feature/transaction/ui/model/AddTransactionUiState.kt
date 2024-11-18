package ru.workinprogress.feature.transaction.ui.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import ru.workinprogress.feature.transaction.Transaction

data class AddTransactionUiState(
    val amount: String = "",
    val income: Boolean = true,
    val period: Transaction.Period = Transaction.Period.OneTime,
    val periods: ImmutableList<Transaction.Period> = defaultPeriods,
    val comment: String = "",
    val date: DateDataUiState = DateDataUiState(),
    val until: DateDataUiState = DateDataUiState(),
    val success: Boolean = false,
) {

    val expanded
        get() = periods != defaultPeriods

    val valid get() = amount.toDoubleOrNull() != null


    companion object {
        private val defaultPeriods = listOf(
            Transaction.Period.OneTime,
            Transaction.Period.TwoWeek,
            Transaction.Period.Month
        ).toImmutableList()
    }
}


@Serializable
data class DateDataUiState(val value: LocalDate? = null, val showDatePicker: Boolean = false)