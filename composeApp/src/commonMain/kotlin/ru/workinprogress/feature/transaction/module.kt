package ru.workinprogress.feature.transaction

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.workinprogress.feature.transaction.data.TransactionRepositoryImpl
import ru.workinprogress.feature.transaction.data.TransactionsNetworkDataSource
import ru.workinprogress.feature.transaction.domain.*

val transactionsModule = module {
//    includes(categoriesModule)
    singleOf(::DeleteTransactionsUseCase)
    singleOf(::GetTransactionsUseCase)
    singleOf(::GetTransactionUseCase)
    singleOf(::TransactionsNetworkDataSource)
    singleOf(::TransactionRepositoryImpl).bind<TransactionRepository>()
    singleOf(::AddCategoryUseCase)
    singleOf(::DeleteCategoryUseCase)
    singleOf(::GetCategoriesUseCase)
    singleOf(::CategoriesNetworkDataSource)
    singleOf(::CategoriesRepository)
}

val categoriesModule = module {

}