package ru.workinprogress.feature.transaction.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.module.rememberKoinModules
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.workinprogress.feature.currency.Currency
import ru.workinprogress.feature.transaction.Transaction
import ru.workinprogress.feature.transaction.domain.AddTransactionUseCase
import ru.workinprogress.feature.transaction.ui.AddTransactionViewModel
import ru.workinprogress.feature.transaction.ui.model.stringResource


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    LaunchedEffect(stateValue.success) {
        if (stateValue.success) {
            onNavigateBack()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = spacedBy(8.dp)
    ) {
        TransactionDatePicker(
            datePickerState,
            stateValue.date.showDatePicker,
            viewModel::onToggleDatePicker,
            viewModel::onDateSelected
        )
        TransactionDatePicker(
            dateUntilPickerState,
            stateValue.until.showDatePicker,
            viewModel::onToggleUntilDatePicker,
            viewModel::onDateUntilSelected
        )

        OutlinedTextField(
            stateValue.amount,
            viewModel::onAmountChanged,
            maxLines = 1,
            visualTransformation = CurrencyVisualTransformation(Currency.Usd),
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

        FlowRow(horizontalArrangement = spacedBy(8.dp)) {
            stateValue.periods.forEach { period ->
                FilterChip(
                    onClick = { viewModel.onPeriodChanged(period) },
                    selected = stateValue.period == period,
                    label = {
                        Text(stringResource(period.stringResource))
                    })
            }

            if (stateValue.periods.size < Transaction.Period.entries.size) {
                TextButton(viewModel::onExpandPeriodClicked) {
                    Text("more")
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

            stateValue.date.date?.let { date ->
                Text(date.toString())
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(8.dp)
        ) {
            TextButton(onClick = viewModel::onToggleUntilDatePicker) {
                Text("Repeat until")
            }

            stateValue.until.date?.let { date ->
                Text(date.toString())
            }


        }

        Button(onClick = viewModel::onCreateClicked) {
            Text("Create")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionDatePicker(
    datePickerState: DatePickerState,
    show: Boolean,
    onToggleDatePicker: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {
    if (show) {
        DatePickerDialog(
            onDismissRequest = onToggleDatePicker,
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault()).date
                    }?.let {
                        onDateSelected(it)
                        onToggleDatePicker()
                    }
                }) {
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