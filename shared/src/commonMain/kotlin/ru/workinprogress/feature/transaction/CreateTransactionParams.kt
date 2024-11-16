package ru.workinprogress.feature.transaction

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
class CreateTransactionParams(
    val amount: Double,
    val income: Boolean,
    val period: Transaction.Period,
    val date: LocalDate,
    val until: LocalDate?,
    val comment: String
)