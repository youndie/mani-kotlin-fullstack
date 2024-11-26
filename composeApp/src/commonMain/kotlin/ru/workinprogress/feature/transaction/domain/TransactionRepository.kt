package ru.workinprogress.feature.transaction.domain

import kotlinx.coroutines.flow.StateFlow
import ru.workinprogress.feature.transaction.Transaction

interface TransactionRepository {
    val dataStateFlow: StateFlow<List<Transaction>>
    suspend fun load()
    fun getById(transactionId: String): Transaction
    suspend fun create(params: Transaction): Boolean
    suspend fun update(params: Transaction): Boolean
    suspend fun delete(transactionId: String): Boolean
    fun reset()
}