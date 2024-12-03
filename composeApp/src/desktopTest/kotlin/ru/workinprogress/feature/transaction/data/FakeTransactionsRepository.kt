package ru.workinprogress.feature.transaction.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import ru.workinprogress.feature.transaction.Transaction
import ru.workinprogress.feature.transaction.domain.TransactionRepository
import ru.workinprogress.feature.transaction.ui.ru.workinprogress.feature.transaction.toDelete

class FakeTransactionsRepository(private val shouldCrash: Boolean = false) : TransactionRepository {
    private val data = MutableStateFlow(emptyList<Transaction>())

    override val dataStateFlow: StateFlow<List<Transaction>> = data

    override suspend fun load() {
        if (shouldCrash) throw RuntimeException("fake")
        data.value = listOf(
            Transaction(
                "", 500.0, true, LocalDate(2000, 1, 1), null, Transaction.Period.OneTime, ""
            ), toDelete
        )
    }

    override fun getById(transactionId: String): Transaction {
        return data.value.first { it.id == transactionId }
    }

    override suspend fun create(params: Transaction): Transaction {
        data.value += params
        return params
    }

    override suspend fun update(params: Transaction): Boolean {
        data.value = data.value - getById(params.id) + params
        return true
    }

    override suspend fun delete(transactionId: String): Boolean {
        data.value -= getById(transactionId)
        return true
    }

    override fun reset() {
        data.value = emptyList()
    }
}