package ru.workinprogress.feature.transaction.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.datetime.LocalDate
import ru.workinprogress.feature.currency.Currency
import ru.workinprogress.feature.transaction.Category
import ru.workinprogress.feature.transaction.Transaction

data class TransactionUiItem(
	val id: String,
	val amount: BigDecimal,
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

val PositiveColor = Color(0xFF089000)
val NegativeColor = Color.Red

fun buildColoredAmount(
	amount: String,
	amountValue: BigDecimal = (try {
		amount.toBigDecimal()
	} catch (e: Exception) {
		BigDecimal.ZERO
	}),
	currency: Currency,
	sign: Boolean = amountValue > 0,
) = buildColoredAmount(amountValue, currency, sign)

fun buildColoredAmount(
	amount: BigDecimal,
	currency: Currency,
	sign: Boolean = amount > 0,
	useSign: Boolean = true,
): AnnotatedString = buildAnnotatedString {
	if (amount != BigDecimal.ZERO) {
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

fun formatMoneyAbsolute(amount: BigDecimal, currency: Currency) =
	formatMoney(amount.abs(), currency)


fun formatMoney(amount: BigDecimal, currency: Currency) =
	"${amount.toPlainString()} ${currency.symbol}"

fun formatMoneyAbsolute(amount: String, currency: Currency) =
	formatMoneyAbsolute(amount.toBigDecimal(), currency)
