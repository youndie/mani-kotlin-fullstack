package ru.workinprogress.feature.main.ui

import androidx.compose.ui.text.AnnotatedString
import kotlinx.collections.immutable.*
import kotlinx.datetime.LocalDate
import ru.workinprogress.feature.transaction.Category
import ru.workinprogress.feature.transaction.ui.model.TransactionUiItem

data class MainUiState(
    val transactions: ImmutableMap<LocalDate, ImmutableList<TransactionUiItem>> =
        emptyMap<LocalDate, ImmutableList<TransactionUiItem>>().toImmutableMap(),
    val errorMessage: String? = null,
    val selectedTransactions: ImmutableList<TransactionUiItem> = emptyList<TransactionUiItem>().toImmutableList(),
    val showDeleteDialog: Boolean = false,
    val showProfile: Boolean = false,
    val futureInformation: AnnotatedString = AnnotatedString(""),
    val loading: Boolean = false,
    val categories: ImmutableSet<Category> = persistentSetOf()
)

