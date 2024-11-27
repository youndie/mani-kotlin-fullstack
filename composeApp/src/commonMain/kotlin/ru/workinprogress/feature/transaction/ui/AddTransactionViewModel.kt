package ru.workinprogress.feature.transaction.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.workinprogress.feature.categories.domain.AddCategoryUseCase
import ru.workinprogress.feature.categories.domain.DeleteCategoryUseCase
import ru.workinprogress.feature.categories.domain.ObserveCategoriesUseCase
import ru.workinprogress.feature.currency.GetCurrentCurrencyUseCase
import ru.workinprogress.feature.transaction.domain.AddTransactionUseCase
import ru.workinprogress.feature.transaction.ui.model.TransactionUiState
import ru.workinprogress.useCase.UseCase

class AddTransactionViewModel(
    private val addTransactionUseCase: AddTransactionUseCase,
    addCategoryUseCase: AddCategoryUseCase,
    observeCategoriesUseCase: ObserveCategoriesUseCase,
    deleteCategoryUseCase: DeleteCategoryUseCase,
    getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase
) : BaseTransactionViewModel(addCategoryUseCase, observeCategoriesUseCase, deleteCategoryUseCase) {

    init {
        viewModelScope.launch(Dispatchers.Default) {
            state.update {
                it.copy(
                    currency = getCurrentCurrencyUseCase.get(),
                )
            }
        }

        observeCategories()
    }

    override fun onSubmitClicked() {
        viewModelScope.launch {
            state.update {
                it.copy(loading = true)
            }
            val result = addTransactionUseCase(state.value.tempTransaction)
            when (result) {
                is UseCase.Result.Error -> {
                    state.update {
                        it.copy(loading = false, errorMessage = result.throwable.message)
                    }
                }

                is UseCase.Result.Success -> {
                    state.update { TransactionUiState(success = true) }
                }
            }
        }
    }
}

