package ru.workinprogress.feature.transaction.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import kotlinx.datetime.LocalDate
import ru.workinprogress.feature.currency.Currency
import ru.workinprogress.feature.transaction.Transaction
import kotlin.math.roundToInt

data class TransactionUiItem(
    val id: String,
    val amount: AnnotatedString,
    val income: Boolean,
    val date: LocalDate,
    val until: LocalDate?,
    val period: Transaction.Period,
    val comment: String
) {
    companion object {
        operator fun invoke(transaction: Transaction, currency: Currency): TransactionUiItem {
            return TransactionUiItem(
                id = transaction.id,
                amount = buildAnnotatedString {

                    withStyle(style = SpanStyle(color = if (transaction.income) Color.Green else Color.Red)) {
                        append(if (transaction.income) "+${transaction.amount.roundToInt()} ${currency.symbol}" else "-${transaction.amount.roundToInt()} ${currency.symbol}")
                    }
                },
                income = transaction.income,
                date = transaction.date,
                until = transaction.until,
                period = transaction.period,
                comment = transaction.comment
            )
        }
    }
}