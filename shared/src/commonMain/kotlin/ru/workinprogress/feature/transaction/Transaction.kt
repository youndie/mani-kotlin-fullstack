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
    val comment: String,
    val category: Category = Category.default
) {

    enum class Period {
        OneTime, Week, TwoWeek, Month, ThreeMonth, HalfYear, Year
    }
}

@Serializable
data class Category(
    val id: String,
    val name: String
) {
    companion object {
        val default = Category("", "Default")
    }
}

val Transaction.amountSigned get() = this.amount * (if (this.income) 1 else -1).toDouble()