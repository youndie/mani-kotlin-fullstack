package ru.workinprogress.feature.transaction

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.workinprogress.feature.transaction.data.TransactionRepositoryImpl
import ru.workinprogress.feature.transaction.data.TransactionsNetworkDataSource
import ru.workinprogress.feature.transaction.domain.*
import ru.workinprogress.feature.transaction.ui.TransactionsViewModel

val transactionsModule = module {
    single { Dispatchers.Default }.bind<CoroutineDispatcher>()
    singleOf(::DeleteTransactionsUseCase)
    singleOf(::GetTransactionsUseCase)
    singleOf(::GetTransactionUseCase)
    singleOf(::AddTransactionUseCase)
    singleOf(::UpdateTransactionUseCase)
    singleOf(::TransactionsNetworkDataSource)
    singleOf(::TransactionRepositoryImpl).bind<TransactionRepository>()
    viewModelOf(::TransactionsViewModel)
}

