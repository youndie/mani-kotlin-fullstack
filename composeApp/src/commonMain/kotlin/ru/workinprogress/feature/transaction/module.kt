package ru.workinprogress.feature.transaction

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.workinprogress.feature.main.MainViewModel
import ru.workinprogress.feature.transaction.data.TransactionRepository
import ru.workinprogress.feature.transaction.domain.DeleteTransactionsUseCase
import ru.workinprogress.feature.transaction.domain.GetTransactionsUseCase

val transactionsModule = module {
    viewModelOf(::MainViewModel)
    singleOf(::DeleteTransactionsUseCase)
    singleOf(::GetTransactionsUseCase)
    singleOf(::TransactionRepository)
}