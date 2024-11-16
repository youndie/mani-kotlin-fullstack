package ru.workinprogress.feature.chart

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class ChartResponse(
    val days: Map<LocalDate, Double>,
    val from: LocalDate,
    val to: LocalDate
) {
    companion object {
        val Empty =
            ChartResponse(emptyMap(), LocalDate.fromEpochDays(0), LocalDate.fromEpochDays(0))
    }
}