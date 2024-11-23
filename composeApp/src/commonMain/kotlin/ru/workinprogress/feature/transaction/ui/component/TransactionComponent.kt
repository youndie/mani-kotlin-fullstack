package ru.workinprogress.feature.transaction.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.module.rememberKoinModules
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.workinprogress.feature.auth.ui.LoadingButton
import ru.workinprogress.feature.transaction.Transaction
import ru.workinprogress.feature.transaction.domain.AddTransactionUseCase
import ru.workinprogress.feature.transaction.domain.UpdateTransactionUseCase
import ru.workinprogress.feature.transaction.ui.AddTransactionViewModel
import ru.workinprogress.feature.transaction.ui.BaseTransactionViewModel
import ru.workinprogress.feature.transaction.ui.EditTransactionViewModel
import ru.workinprogress.feature.transaction.ui.model.TransactionUiState
import ru.workinprogress.feature.transaction.ui.model.stringResource
import ru.workinprogress.feature.transaction.ui.utils.CurrencyVisualTransformation
import ru.workinprogress.mani.navigation.TransactionRoute


@Composable
fun AddTransactionComponent(onNavigateBack: () -> Unit) {
    rememberKoinModules {
        listOf(module {
            singleOf(::AddTransactionUseCase)
            viewModelOf(::AddTransactionViewModel).bind<BaseTransactionViewModel>()
        })
    }

    TransactionComponentImpl(onNavigateBack)
}


@Composable
fun EditTransactionComponent(transactionRoute: TransactionRoute, onNavigateBack: () -> Unit) {
    rememberKoinModules {
        listOf(module {
            single<TransactionRoute> { transactionRoute }
            singleOf(::UpdateTransactionUseCase)
            viewModelOf(::EditTransactionViewModel).bind<BaseTransactionViewModel>()
        })
    }

    TransactionComponentImpl(onNavigateBack)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TransactionComponentImpl(onNavigateBack: () -> Unit) {
    val viewModel = koinViewModel<BaseTransactionViewModel>()
    val state: State<TransactionUiState> = viewModel.observe.collectAsStateWithLifecycle()
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

    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { lifecycleOwner, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    if (!stateValue.edit) {
                        coroutineScope.launch {
                            focusRequester.requestFocus()
                        }
                    }
                }

                Lifecycle.Event.ON_STOP -> {
                }

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(stateValue.success) {
        if (stateValue.success) {
            onNavigateBack()
        }
    }

    LaunchedEffect(stateValue.date.showDatePicker) {
        if (!stateValue.date.showDatePicker && stateValue.amount.isNotEmpty()) {
            focusManager.clearFocus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
    ) {
        Card(
            colors = CardDefaults.cardColors()
                .copy(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = MaterialTheme.shapes.medium.copy(
                topStart = CornerSize(0.dp),
                topEnd = CornerSize(0.dp)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 32.dp),
                verticalArrangement = spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    stateValue.amount,
                    viewModel::onAmountChanged,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { viewModel.onToggleDatePicker() }),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    maxLines = 1,
                    visualTransformation = CurrencyVisualTransformation(stateValue.currency),
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
                    Column(modifier = Modifier) {
                        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.secondary) {
                            Text(
                                "Repeat",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    top = 16.dp,
                                    bottom = 4.dp
                                )
                            )
                        }

                        FlowRow(
                            horizontalArrangement = spacedBy(8.dp),
                            modifier = Modifier.animateContentSize()
                        ) {
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

                    AnimatedVisibility(stateValue.period != Transaction.Period.OneTime) {
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

        AnimatedVisibility(stateValue.amount.isNotBlank() && stateValue.date.value != null) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.secondary) {
                Text(
                    stateValue.futureInformation,
                    modifier = Modifier.padding(
                        start = 32.dp,
                        top = 12.dp,
                        bottom = 4.dp,
                        end = 32.dp
                    ),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val keyboardController = LocalSoftwareKeyboardController.current

        OutlinedTextField(
            stateValue.comment,
            viewModel::onCommentChanged,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }),
            minLines = 2,
            label = { Text("Comment") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        state.value.errorMessage?.let {
            Text(
                it,
                modifier = Modifier.padding(horizontal = 48.dp),
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.height(24.dp))
        }

        LoadingButton(
            Modifier.align(Alignment.CenterHorizontally),
            loading = stateValue.loading,
            enabled = stateValue.valid,
            if (stateValue.edit) "Save" else "Create",
            viewModel::onSubmitClicked
        )
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

