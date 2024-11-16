package ru.workinprogress.feature.transaction

import org.koin.dsl.module
import ru.workinprogress.feature.transaction.data.TransactionRepository

val transactionModule = module {
    single<TransactionRepository> { TransactionRepository(get()) }
}