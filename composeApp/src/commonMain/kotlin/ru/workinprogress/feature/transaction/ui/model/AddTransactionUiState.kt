package ru.workinprogress.feature.transaction.ui.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import ru.workinprogress.feature.transaction.Transaction

sealed class AddTransactionUiState {

    data class Prepare(
        val amount: String = "",
        val income: Boolean = true,
        val period: Transaction.Period = Transaction.Period.OneTime,
        val comment: String = "",
        val date: DateDataUiState = DateDataUiState(),
        val until: DateDataUiState = DateDataUiState(),
    ) : AddTransactionUiState()

    data object Finish : AddTransactionUiState()
}

@Serializable
data class DateDataUiState(val date: LocalDate? = null, val showDatePicker: Boolean = false)