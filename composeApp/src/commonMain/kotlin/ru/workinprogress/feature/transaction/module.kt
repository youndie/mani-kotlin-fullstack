package ru.workinprogress.feature.transaction

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.workinprogress.feature.transaction.data.TransactionRepositoryImpl
import ru.workinprogress.feature.transaction.domain.TransactionRepository

import ru.workinprogress.feature.transaction.domain.DeleteTransactionsUseCase
import ru.workinprogress.feature.transaction.domain.GetTransactionsUseCase
import ru.workinprogress.feature.transaction.domain.GetTransactionUseCase

val transactionsModule = module {
    singleOf(::DeleteTransactionsUseCase)
    singleOf(::GetTransactionsUseCase)
    singleOf(::GetTransactionUseCase)
    singleOf(::TransactionRepositoryImpl).bind<TransactionRepository>()
}