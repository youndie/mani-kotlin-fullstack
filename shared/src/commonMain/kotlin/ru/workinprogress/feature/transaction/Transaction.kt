package ru.workinprogress.feature.transaction

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String,
    val amount: Double,
    val income: Boolean,
    val date: LocalDate,
    val until: LocalDate?,
    val period: Period,
    val comment: String
) {

    enum class Period {
        OneTime, Week, TwoWeek, Month, ThreeMonth, HalfYear, Year
    }
}

val Transaction.amountSigned get() = this.amount * (if (this.income) 1 else -1).toDouble()