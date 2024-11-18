package ru.workinprogress.feature.transaction.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
        verticalArrangement = spacedBy(24.dp)
    ) {

        Card(
            colors = CardDefaults.cardColors().copy(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = MaterialTheme.shapes.medium.copy(topStart = CornerSize(0.dp), topEnd = CornerSize(0.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
                verticalArrangement = spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    stateValue.amount,
                    viewModel::onAmountChanged,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    visualTransformation = CurrencyVisualTransformation(Currency.Usd),
                    label = { Text("Amount") }
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(stateValue.income, viewModel::onIncomeChanged)
                    Text("Income")
                }

                TransactionDatePicker(
                    label = "Date",
                    value = stateValue.date.value?.formatted,
                    datePickerState = datePickerState,
                    showDialog = stateValue.date.showDatePicker,
                    onToggleDatePicker = viewModel::onToggleDatePicker,
                    onDateSelected = viewModel::onDateSelected,
                )

                if (stateValue.date.value != null) {
                    Column(modifier = Modifier.padding(bottom = 4.dp)) {
                        Text(
                            "Repeat",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
                        )

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
                                    Text("More")
                                }
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

        val keyboardController = LocalSoftwareKeyboardController.current

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        ) {
            OutlinedTextField(
                stateValue.comment,
                viewModel::onCommentChanged,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }),
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                label = { Text("Comment") }
            )
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