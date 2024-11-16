package ru.workinprogress.feature.chart.ui.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import ru.workinprogress.feature.chart.ChartResponse

@Serializable
data class ChartUi(
    val days: Map<LocalDate, Double>,
    val from: LocalDate,
    val to: LocalDate
) {
    companion object {
        operator fun invoke(chart: ChartResponse): ChartUi = ChartUi(chart.days, chart.from, chart.to)
    }
}