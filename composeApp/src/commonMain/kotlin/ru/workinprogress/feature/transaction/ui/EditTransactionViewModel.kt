package ru.workinprogress.feature.transaction.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.workinprogress.feature.categories.domain.AddCategoryUseCase
import ru.workinprogress.feature.categories.domain.DeleteCategoryUseCase
import ru.workinprogress.feature.categories.domain.ObserveCategoriesUseCase
import ru.workinprogress.feature.currency.GetCurrentCurrencyUseCase
import ru.workinprogress.feature.transaction.domain.*
import ru.workinprogress.feature.transaction.ui.model.TransactionUiState
import ru.workinprogress.mani.navigation.TransactionRoute
import ru.workinprogress.useCase.UseCase

class EditTransactionViewModel(
    private val route: TransactionRoute,
    private val getTransactionUseCase: GetTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    addCategoryUseCase: AddCategoryUseCase,
    observeCategoriesUseCase: ObserveCategoriesUseCase,
    getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase,
    deleteCategoryUseCase: DeleteCategoryUseCase,
) : BaseTransactionViewModel(addCategoryUseCase, observeCategoriesUseCase, deleteCategoryUseCase) {

    override val state: MutableStateFlow<TransactionUiState> = MutableStateFlow(TransactionUiState(edit = true))

    init {
        viewModelScope.launch(Dispatchers.Default) {
            val transaction = getTransactionUseCase.get(route.id)
            state.update {
                TransactionUiState(
                    transaction,
                    currency = getCurrentCurrencyUseCase.get(),
                ).copy(edit = true)
            }

            observeCategories()
        }
    }

    override fun onSubmitClicked() {
        viewModelScope.launch {
            state.update { it.copy(loading = true) }

            val result = updateTransactionUseCase(state.value.tempTransaction)

            when (result) {
                is UseCase.Result.Success -> {
                    state.update { TransactionUiState(success = true) }
                }

                is UseCase.Result.Error -> {
                    state.update {
                        it.copy(errorMessage = result.throwable.message, loading = false)
                    }
                }
            }
        }
    }
}