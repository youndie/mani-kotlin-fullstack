package ru.workinprogress.feature.transaction

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import ru.workinprogress.feature.chart.ChartResponse
import ru.workinprogress.mani.today

const val DEFAULT_PERIOD_VALUE = 3
val DEFAULT_PERIOD_UNIT = DateTimeUnit.MONTH

fun createDates(from: LocalDate, to: LocalDate): List<LocalDate> {
    return buildList {
        var currentDate = from
        while (currentDate < to) {
            add(currentDate)
            currentDate = currentDate.plus(1, DateTimeUnit.DAY)
        }
    }
}

fun List<Transaction>.defaultPeriod(): Pair<LocalDate, LocalDate> {
    val from = this.minOf { transaction -> transaction.date }
    val to = defaultPeriodAppend(today())
    return from to to
}

fun defaultPeriodAppend(date: LocalDate) =
    date.plus(DEFAULT_PERIOD_VALUE, DEFAULT_PERIOD_UNIT)

fun List<Transaction>.toChartInternal(): ChartResponse {
    if (isEmpty()) return ChartResponse.Empty

    val (from, to) = defaultPeriod()
    val simulated = simulate(from, to)
    val chartData = simulated.entries.runningFold(from to 0.toDouble()) { acc, list ->
        list.key to acc.second + list.value.sumOf { transaction ->
            transaction.amountSigned
        }
    }.toMap()

    return ChartResponse(days = chartData, from, to)
}

fun List<Transaction>.simulate(
    dates: Pair<LocalDate, LocalDate> = defaultPeriod()
): Map<LocalDate, List<Transaction>> {
    val (from, to) = dates
    return simulate(from, to)
}

fun List<Transaction>.simulate(from: LocalDate, to: LocalDate): Map<LocalDate, List<Transaction>> {
    val scheduled = mutableMapOf<LocalDate, List<Transaction>>()

    createDates(from, to).forEach { currentDate ->
        val scheduledForDate = mutableListOf<Transaction>()

        val currentTransactions = this.filter { transaction ->
            transaction.date == currentDate
        }

        val nextTransactions = scheduled[currentDate].orEmpty()

        (currentTransactions + nextTransactions).forEach { transaction ->
            scheduledForDate.add(transaction)

            scheduled.scheduleTransaction(
                transaction, to,
                when (transaction.period) {
                    Transaction.Period.OneTime -> currentDate
                    Transaction.Period.Week -> currentDate.plus(1, DateTimeUnit.WEEK)
                    Transaction.Period.TwoWeek -> currentDate.plus(2, DateTimeUnit.WEEK)
                    Transaction.Period.Month -> currentDate.plus(1, DateTimeUnit.MONTH)
                    Transaction.Period.ThreeMonth -> currentDate.plus(1, DateTimeUnit.QUARTER)
                    Transaction.Period.HalfYear -> currentDate.plus(2, DateTimeUnit.QUARTER)
                    Transaction.Period.Year -> currentDate.plus(1, DateTimeUnit.YEAR)
                }
            )
        }

        scheduled[currentDate] = scheduledForDate
    }

    return scheduled.toList().sortedBy { it.first }.toMap()
}

private fun MutableMap<LocalDate, List<Transaction>>.scheduleTransaction(
    transaction: Transaction,
    to: LocalDate,
    nextDate: LocalDate
) {
    if ((transaction.until == null || transaction.until >= nextDate) && to > nextDate) {
        this[nextDate] = (this[nextDate]?.toMutableList() ?: mutableListOf()).apply {
            this.add(transaction)
        }
    }
}
