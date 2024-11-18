package ru.workinprogress.feature.transaction.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.*
import kotlinx.datetime.format.char
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        verticalArrangement = spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        ) {
            OutlinedTextField(
                stateValue.amount,
                viewModel::onAmountChanged,
                modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                label = { Text("Comment") }
            )
        }

        Card(colors = CardDefaults.cardColors().copy(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
                verticalArrangement = spacedBy(8.dp)
            ) {
                TransactionDatePicker(
                    label = "Date",
                    value = stateValue.date.value?.formatted,
                    datePickerState = datePickerState,
                    showDialog = stateValue.date.showDatePicker,
                    onToggleDatePicker = viewModel::onToggleDatePicker,
                    onDateSelected = viewModel::onDateSelected,
                )

                if (stateValue.date.value != null) {
                    Text("Repeat", modifier = Modifier.padding(start = 16.dp, top = 16.dp))

                    FlowRow(horizontalArrangement = spacedBy(8.dp), modifier = Modifier.animateContentSize()) {
                        stateValue.periods.forEach { period ->
                            key(period) {
                                FilterChip(
                                    onClick = { viewModel.onPeriodChanged(period) },
                                    selected = stateValue.period == period,
                                    label = {
                                        Text(stringResource(period.stringResource))
                                    })
                            }
                        }

                        if (!stateValue.expanded) {
                            TextButton(viewModel::onExpandPeriodClicked) {
                                Text("more")
                            }
                        }
                    }

                    if (stateValue.period != Transaction.Period.OneTime) {
                        TransactionDatePicker(
                            label = "Repeat until",
                            value = stateValue.until.value?.formatted,
                            datePickerState = dateUntilPickerState,
                            showDialog = stateValue.until.showDatePicker,
                            onToggleDatePicker = viewModel::onToggleUntilDatePicker,
                            onDateSelected = viewModel::onDateUntilSelected,
                        )
                    }
                }
            }
        }

        Button(
            onClick = viewModel::onCreateClicked,
            enabled = stateValue.valid,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Create")
        }
    }
}

val LocalDate.formatted
    get() = this.format(LocalDate.Format {
        monthNumber()
        char('/')
        dayOfMonth()
        char('/')
        year()
    })

inline val Long?.toDate
    get() = this?.let {
        Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault()).date
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