package ru.workinprogress.feature.chart.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import org.koin.compose.koinInject
import org.koin.compose.module.rememberKoinModules
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.workinprogress.feature.chart.ChartViewModel
import ru.workinprogress.feature.chart.GetChartUseCase

@OptIn(KoinExperimentalAPI::class)
@Composable
fun ChartComponent() {
    rememberKoinModules {
        listOf(module {
            singleOf(::GetChartUseCase)
            viewModelOf(::ChartViewModel)
        })
    }
    val viewModel = koinViewModel<ChartViewModel>()
    val state by viewModel.observe.collectAsStateWithLifecycle(null)

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .aspectRatio(3 / 2f)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            state?.let { chart ->
                ChartImpl(
                    chart.days.values.toImmutableList(),
                    chart.days.keys
                        .groupBy { "${it.year}-${it.monthNumber}" }
                        .map { it.key }
                        .toImmutableList()
                )
            }
        }
    }
}

