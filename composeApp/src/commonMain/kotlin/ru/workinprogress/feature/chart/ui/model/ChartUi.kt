package ru.workinprogress.feature.chart.ui.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import ru.workinprogress.feature.chart.ChartResponse
import ru.workinprogress.feature.currency.Currency
import ru.workinprogress.mani.emptyImmutableMap
import ru.workinprogress.mani.today

@Serializable
data class ChartUi(
    val days: Map<LocalDate, Double>,
    val from: LocalDate,
    val to: LocalDate,
    val currency: Currency,
    val loading: Boolean = false
) {
    companion object {
        val Loading = ChartUi(emptyImmutableMap(), today(), today(), Currency.Usd, true)

        operator fun invoke(chart: ChartResponse, currency: Currency): ChartUi =
            ChartUi(chart.days, chart.from, chart.to, currency)
    }
}