package ru.workinprogress.feature.transaction.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import ir.ehsannarmani.compose_charts.extensions.format
import kotlinx.datetime.LocalDate
import ru.workinprogress.feature.currency.Currency
import ru.workinprogress.feature.transaction.Category
import ru.workinprogress.feature.transaction.Transaction
import kotlin.math.absoluteValue

data class TransactionUiItem(
    val id: String,
    val amount: Double,
    val income: Boolean,
    val date: LocalDate,
    val until: LocalDate?,
    val period: Transaction.Period,
    val comment: String,
    val currency: Currency,
    val category: Category,
) {
    val amountText get() = buildColoredAmount(amount, currency, income)

    companion object {
        operator fun invoke(transaction: Transaction, currency: Currency): TransactionUiItem {
            return TransactionUiItem(
                id = transaction.id,
                amount = transaction.amount,
                income = transaction.income,
                date = transaction.date,
                until = transaction.until,
                period = transaction.period,
                comment = transaction.comment,
                currency = currency,
                category = transaction.category
            )
        }
    }
}

val PositiveColor = Color.Green
val NegativeColor = Color.Red

fun buildColoredAmount(
    amount: String,
    amountValue: Double = (amount.toDoubleOrNull() ?: 0.0),
    currency: Currency,
    sign: Boolean = amountValue > 0,
) = buildColoredAmount(amountValue, currency, sign)

fun buildColoredAmount(
    amount: Double,
    currency: Currency,
    sign: Boolean = amount > 0,
    useSign: Boolean = true,
): AnnotatedString = buildAnnotatedString {
    if (amount != 0.0) {
        withStyle(style = SpanStyle(color = if (sign) PositiveColor else NegativeColor)) {
            if (useSign) {
                append(if (sign) "+" else "−")
            }
            append(formatMoneyAbsolute(amount, currency))
        }
    } else {
        append(formatMoneyAbsolute(amount, currency))
    }
}

fun formatMoneyAbsolute(amount: Double, currency: Currency) =
    formatMoney(amount.absoluteValue, currency)


fun formatMoney(amount: Double, currency: Currency) =
    "${amount.format(0)} ${currency.symbol}"

fun formatMoneyAbsolute(amount: String, currency: Currency) =
    formatMoneyAbsolute((amount.toDoubleOrNull() ?: 0.0), currency)
