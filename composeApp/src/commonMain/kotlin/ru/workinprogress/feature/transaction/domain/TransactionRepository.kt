package ru.workinprogress.feature.transaction.domain

import kotlinx.coroutines.flow.StateFlow
import ru.workinprogress.feature.transaction.StateFlowRepository
import ru.workinprogress.feature.transaction.Transaction

interface TransactionRepository : StateFlowRepository<Transaction> {
    override val dataStateFlow: StateFlow<List<Transaction>>
    override suspend fun load()
    override fun getById(transactionId: String): Transaction
    override suspend fun create(params: Transaction): Transaction
    override suspend fun update(params: Transaction): Boolean
    override suspend fun delete(transactionId: String): Boolean
    override fun reset()
}