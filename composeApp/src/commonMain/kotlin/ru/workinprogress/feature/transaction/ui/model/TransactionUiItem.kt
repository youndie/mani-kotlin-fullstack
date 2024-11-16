package ru.workinprogress.feature.transaction.ui.model

import kotlinx.datetime.LocalDate
import ru.workinprogress.feature.transaction.Transaction

data class TransactionUiItem(
    val id: String,
    val amount: Double,
    val income: Boolean,
    val date: LocalDate,
    val until: LocalDate?,
    val period: Transaction.Period,
    val comment: String
) {
    companion object {
        operator fun invoke(transaction: Transaction): TransactionUiItem {
            return TransactionUiItem(
                id = transaction.id,
                amount = transaction.amount,
                income = transaction.income,
                date = transaction.date,
                until = transaction.until,
                period = transaction.period,
                comment = transaction.comment
            )
        }
    }
}