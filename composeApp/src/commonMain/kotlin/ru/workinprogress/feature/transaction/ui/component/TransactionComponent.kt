@file:OptIn(ExperimentalMaterial3Api::class)

package ru.workinprogress.feature.transaction.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableCollection
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
import ru.workinprogress.feature.transaction.Category
import ru.workinprogress.feature.transaction.Transaction
import ru.workinprogress.feature.transaction.domain.AddTransactionUseCase
import ru.workinprogress.feature.transaction.domain.UpdateTransactionUseCase
import ru.workinprogress.feature.transaction.ui.AddTransactionViewModel
import ru.workinprogress.feature.transaction.ui.BaseTransactionViewModel
import ru.workinprogress.feature.transaction.ui.EditTransactionViewModel
import ru.workinprogress.feature.transaction.ui.component.model.TransactionAction
import ru.workinprogress.feature.transaction.ui.component.model.TransactionAction.*
import ru.workinprogress.feature.transaction.ui.model.TransactionUiState
import ru.workinprogress.feature.transaction.ui.model.stringResource
import ru.workinprogress.feature.transaction.ui.utils.CurrencyVisualTransformation
import ru.workinprogress.mani.components.LoadingButton
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun <T> ChipsSelector(
    items: ImmutableCollection<T>,
    selected: T,
    expanded: Boolean,
    onExpanded: () -> Unit = { },
    onSelected: (T) -> Unit,
    deleteEnabled: (T) -> Boolean = { false },
    showCreateNew: Boolean = false,
    onCreateNew: () -> Unit = {},
    onDelete: (T) -> Unit = {},
    labelValue: @Composable (T) -> String = { it.toString() },
) {
    val markToDelete = remember { mutableStateOf<T?>(null) }

    FlowRow(
        horizontalArrangement = spacedBy(8.dp), modifier = Modifier.animateContentSize()
    ) {
        items.forEach { item ->
            key(item) {
                val inputChipInteractionSource = remember { MutableInteractionSource() }
                Box {
                    InputChip(
                        onClick = { },
                        selected = selected == item,
                        label = {
                            Text(labelValue(item))
                        },
                        elevation = InputChipDefaults.inputChipElevation(elevation = if (item == markToDelete.value) 8.dp else 0.dp),
                        trailingIcon = {
                            if (item == markToDelete.value) {
                                IconButton(
                                    {
                                        markToDelete.value = null
                                    },
                                    Modifier.size(AssistChipDefaults.IconSize),
                                    interactionSource = inputChipInteractionSource,
                                ) {
                                    Icon(
                                        Icons.Filled.Clear,
                                        contentDescription = "delete",
                                    )
                                }

                            }
                        },
                        interactionSource = inputChipInteractionSource,
                    )
                    Box(
                        modifier = Modifier.matchParentSize().combinedClickable(
                            onLongClick = {
                                if (deleteEnabled(item)) {
                                    onSelected(item)
                                    markToDelete.value = item
                                }
                            },
                            onClick = {
                                if (markToDelete.value == null || item != markToDelete.value) {
                                    markToDelete.value = null
                                    onSelected(item)
                                } else {
                                    onDelete(item)
                                }
                            },
                            interactionSource = inputChipInteractionSource,
                            indication = null,
                        )
                    )
                }
            }
        }

        if (showCreateNew) {
            AssistChip(onClick = onCreateNew, label = { Text("Add") }, leadingIcon = {
                Icon(
                    Icons.Filled.Add, contentDescription = "add", Modifier.size(AssistChipDefaults.IconSize)
                )
            })
        }

        if (!expanded) {
            TextButton(onExpanded) {
                Text("More")
            }
        }
    }
}

@Composable
private fun NewCategoryDialog(
    showCreateCategoryDialog: MutableState<Boolean> = remember { mutableStateOf(false) },
    onCreate: (String) -> Unit,
) {
    var newCategoryName by remember { mutableStateOf("") }

    if (showCreateCategoryDialog.value) {
        AlertDialog(title = { Text("New category") }, text = {
            OutlinedTextField(newCategoryName, {
                newCategoryName = it
            }, modifier = Modifier.padding(vertical = 16.dp), label = { Text("Category name") })
        }, onDismissRequest = {
            newCategoryName = ""
            showCreateCategoryDialog.value = false
        }, confirmButton = {
            TextButton(onClick = {
                onCreate(newCategoryName)
                newCategoryName = ""
                showCreateCategoryDialog.value = false
            }) {
                Text("Create")
            }
        }, dismissButton = {
            TextButton(onClick = {
                newCategoryName = ""
                showCreateCategoryDialog.value = false
            }) {
                Text("Cancel")
            }
        })
    }
}

@Composable
fun CategoryDeleteDialog(
    showDeleteDialog: Boolean, onDelete: () -> Unit, onDismiss: () -> Unit,
) {
    if (showDeleteDialog) {
        AlertDialog(
            title = { Text("Delete selected category?") },
            text = { Text("This action cannot be undone later") },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onDelete) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            })
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TransactionComponentImpl(onNavigateBack: () -> Unit) {
    val viewModel = koinViewModel<BaseTransactionViewModel>()
    val state: State<TransactionUiState> = viewModel.observe.collectAsStateWithLifecycle()

    TransactionComponentImpl(state.value, viewModel::onAction, onNavigateBack)
}


@Composable
internal fun TransactionComponentImpl(
    state: TransactionUiState,
    onAction: (TransactionAction) -> Unit,
    onNavigateBack: () -> Unit,
) {

    var showCreateCategoryDialog = remember { mutableStateOf(false) }
    var categoryToRemove = remember { mutableStateOf<Category?>(null) }

    val datePickerState = rememberDatePickerState(selectableDates = object : SelectableDates {
        override fun isSelectableYear(year: Int): Boolean {
            return year > 2022
        }
    })

    val dateUntilPickerState = rememberDatePickerState(selectableDates = object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            val date = utcTimeMillis.toDate
            val stateDate = state.date.value
            if (date != null && stateDate != null) {
                return date > stateDate
            }
            return false
        }
    })

    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { lifecycleOwner, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    if (!state.edit) {
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

    LaunchedEffect(state.success) {
        if (state.success) {
            onNavigateBack()
        }
    }

    LaunchedEffect(state.date.showDatePicker) {
        if (!state.date.showDatePicker && state.amount.isNotEmpty()) {
            focusManager.clearFocus()
        }
    }

    NewCategoryDialog(showCreateCategoryDialog) {
        onAction(CategoryCreate(it))
    }

    CategoryDeleteDialog(
        showDeleteDialog = categoryToRemove.value != null,
        onDelete = {
            onAction(CategoryDelete(categoryToRemove.value))
            categoryToRemove.value = null
        }, onDismiss = {
            categoryToRemove.value = null
        })

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors().copy(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = MaterialTheme.shapes.medium.copy(
                topStart = CornerSize(0.dp), topEnd = CornerSize(0.dp)
            )
        ) {
            Column(
                modifier = Modifier.widthIn(max = 640.dp).align(Alignment.CenterHorizontally)
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 32.dp),
                verticalArrangement = spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    state.amount,
                    { onAction(AmountChanged(it)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { onAction(ToggleDatePicker) }),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).testTag("amount"),
                    maxLines = 1,
                    visualTransformation = CurrencyVisualTransformation(state.currency),
                    label = { Text("Amount") })

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        state.income, { onAction(IncomeChanged(it)) }, modifier = Modifier.testTag("income")
                    )
                    Text("Income")
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp).testTag("categoryContainer")
                ) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.secondary) {
                        Text(
                            "Category",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(
                                start = 16.dp, bottom = 4.dp
                            )
                        )
                    }

                    ChipsSelector(
                        state.categories,
                        state.category,
                        state.categoriesExpanded,
                        { onAction(ExpandCategoryClicked) },
                        { onAction(CategoryChanged(it)) },
                        showCreateNew = true,
                        deleteEnabled = {
                            it != Category.default
                        },
                        onCreateNew = {
                            showCreateCategoryDialog.value = true
                        },
                        onDelete = {
                            categoryToRemove.value = it
                        }) { it.name }
                }

                HorizontalDivider(modifier = Modifier.testTag("divider"), thickness = 1.dp)

                Spacer(modifier = Modifier.height(4.dp))

                TransactionDatePicker(
                    label = "Date",
                    value = state.date.value?.formatted,
                    modifier = Modifier.testTag("date"),
                    datePickerState = datePickerState,
                    showDialog = state.date.showDatePicker,
                    onToggleDatePicker = { onAction(ToggleDatePicker) },
                    onDateSelected = { onAction(DateSelected(it)) },
                )

                if (state.date.value != null) {
                    Column(Modifier.testTag("periodContainer")) {
                        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.secondary) {
                            Text(
                                "Repeat",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(
                                    start = 16.dp, top = 16.dp, bottom = 4.dp
                                )
                            )
                        }

                        ChipsSelector(
                            state.periods,
                            state.period,
                            state.periodsExpanded,
                            { onAction(ExpandPeriodClicked) },
                            { onAction(PeriodChanged(it)) }) { item ->
                            stringResource(item.stringResource)
                        }
                    }

                    AnimatedVisibility(state.period != Transaction.Period.OneTime) {
                        TransactionDatePicker(
                            modifier = Modifier.padding(top = 8.dp).testTag("until"),
                            label = "Repeat until",
                            value = state.until.value?.formatted,
                            datePickerState = dateUntilPickerState,
                            showDialog = state.until.showDatePicker,
                            onToggleDatePicker = { onAction(ToggleUntilDatePicker) },
                            onDateSelected = { onAction(DateUntilSelected(it)) },
                        )
                    }
                }
            }
        }



        Column(modifier = Modifier.widthIn(max = 640.dp).align(Alignment.CenterHorizontally)) {
            AnimatedVisibility(state.amount.isNotBlank() && state.date.value != null) {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.secondary) {
                    Text(
                        state.futureInformation,
                        modifier = Modifier.padding(
                            start = 32.dp,
                            top = 12.dp,
                            bottom = 4.dp,
                            end = 32.dp
                        ).testTag("futureInformation"),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            val keyboardController = LocalSoftwareKeyboardController.current

            OutlinedTextField(
                state.comment,
                { onAction(CommentChanged(it)) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).testTag("comment"),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }),
                minLines = 2,
                label = { Text("Comment") })

            Spacer(modifier = Modifier.height(24.dp))

            state.errorMessage?.let {
                Text(
                    it,
                    modifier = Modifier.padding(horizontal = 48.dp).testTag("errorMessage"),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(24.dp))
            }

            LoadingButton(
                Modifier.align(Alignment.CenterHorizontally).testTag("submit"),
                loading = state.loading,
                enabled = state.valid,
                if (state.edit) "Save" else "Create"
            ) { onAction(SubmitClicked) }

            Spacer(Modifier.height(24.dp))
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

