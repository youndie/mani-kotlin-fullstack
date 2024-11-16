package ru.workinprogress.feature.transaction.ui.component

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import org.koin.compose.module.rememberKoinModules
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.workinprogress.feature.transaction.domain.AddTransactionUseCase
import ru.workinprogress.feature.transaction.ui.AddTransactionViewModel
import ru.workinprogress.feature.transaction.Transaction
import ru.workinprogress.feature.transaction.ui.model.AddTransactionUiState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionComponentImpl(onNavigateBack: () -> Unit) {
    val viewModel = koinViewModel<AddTransactionViewModel>()
    val state = viewModel.observe.collectAsStateWithLifecycle()
    val stateValue = state.value

    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableYear(year: Int): Boolean {
                return year > 2022
            }
        }
    )

    val dateUntilPickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableYear(year: Int): Boolean {
                return year > 2022
            }
        }
    )

    val coroutineScope = rememberCoroutineScope()
    snapshotFlow { datePickerState.selectedDateMillis }
        .filterNotNull()
        .map {
            Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault()).date
        }
        .onEach(viewModel::onDateSelected)
        .launchIn(coroutineScope)

    snapshotFlow { dateUntilPickerState.selectedDateMillis }
        .filterNotNull()
        .map {
            Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault()).date
        }
        .onEach(viewModel::onDateUntilSelected)
        .launchIn(coroutineScope)


    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = spacedBy(8.dp)
    ) {
        when (stateValue) {
            AddTransactionUiState.Finish -> {
                onNavigateBack()
            }

            is AddTransactionUiState.Prepare -> {
                TransactionDatePicker(
                    datePickerState,
                    stateValue.date.showDatePicker,
                    viewModel::onToggleDatePicker
                )
                TransactionDatePicker(
                    dateUntilPickerState,
                    stateValue.until.showDatePicker,
                    viewModel::onToggleUntilDatePicker
                )

                OutlinedTextField(
                    stateValue.amount,
                    viewModel::onAmountChanged,
                    label = { Text("Amount") }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(stateValue.income, viewModel::onIncomeChanged)
                    Text("Income")
                }
                OutlinedTextField(
                    stateValue.comment,
                    viewModel::onCommentChanged,
                    minLines = 2,
                    label = { Text("Comment") }
                )

                Text("Repeat")

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = spacedBy(8.dp)
                ) {
                    Transaction.Period.entries.forEach { period ->
                        key(period.name) {
                            FilterChip(
                                onClick = { viewModel.onPeriodChanged(period) },
                                selected = stateValue.period == period,
                                label = {
                                    Text(period.name)
                                })
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = spacedBy(8.dp)
                ) {
                    TextButton(onClick = viewModel::onToggleDatePicker) {
                        Text(if (stateValue.date.date == null) "Select date" else "Change date")
                    }

                    if (stateValue.date.date != null) {
                        Text(stateValue.date.toString())
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = spacedBy(8.dp)
                ) {
                    TextButton(onClick = viewModel::onToggleUntilDatePicker) {
                        Text("Repeat until")
                    }

                    if (stateValue.until.date != null) {
                        Text(stateValue.until.toString())
                    }
                }

                Button(onClick = viewModel::onCreateClicked) {
                    Text("Create")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionDatePicker(
    datePickerState: DatePickerState,
    show: Boolean,
    onToggleDatePicker: () -> Unit
) {
    if (show) {
        DatePickerDialog(
            onDismissRequest = onToggleDatePicker,
            confirmButton = {
                TextButton(onClick = onToggleDatePicker) {
                    Text("Ok")
                }
            },
            dismissButton = {
                TextButton(onClick = onToggleDatePicker) {
                    Text("Cancel")
                }
            }) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun AddTransactionComponent(onNavigateBack: () -> Unit) {
    rememberKoinModules {
        listOf(module {
            singleOf(::AddTransactionUseCase)
            viewModelOf(::AddTransactionViewModel)
        })
    }

    AddTransactionComponentImpl(onNavigateBack)
}