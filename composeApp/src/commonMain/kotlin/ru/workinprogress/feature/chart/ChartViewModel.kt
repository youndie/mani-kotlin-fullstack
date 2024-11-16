package ru.workinprogress.feature.chart

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import ru.workinprogress.feature.chart.ui.model.ChartUi
import ru.workinprogress.feature.transaction.Transaction
import ru.workinprogress.feature.transaction.data.TransactionRepository
import ru.workinprogress.feature.transaction.toChartInternal
import ru.workinprogress.mani.defaultMinDate
import ru.workinprogress.mani.today


class ChartViewModel(transactionRepository: TransactionRepository) : ViewModel() {

    val observe = transactionRepository.dataStateFlow
        .filter(List<Transaction>::isNotEmpty)
        .map { it.toChart(from = defaultMinDate) }
        .flowOn(Dispatchers.Default)

    private fun List<Transaction>.toChart(from: LocalDate): ChartUi {
        return ChartUi(toChartInternal().let { chart -> chart.copy(days = chart.days.filter { it.key > from }) })
    }
}

