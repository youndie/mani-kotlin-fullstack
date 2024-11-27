package ru.workinprogress.feature.transaction.data

import ru.workinprogress.feature.transaction.BaseFlowRepository
import ru.workinprogress.feature.transaction.Transaction
import ru.workinprogress.feature.transaction.domain.TransactionRepository

class TransactionRepositoryImpl(transactionsNetworkDataSource: TransactionsNetworkDataSource) :
    BaseFlowRepository<Transaction>(transactionsNetworkDataSource), TransactionRepository