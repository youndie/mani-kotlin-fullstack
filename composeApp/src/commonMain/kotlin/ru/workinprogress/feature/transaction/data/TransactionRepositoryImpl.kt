package ru.workinprogress.feature.transaction.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import ru.workinprogress.feature.transaction.Transaction
import ru.workinprogress.feature.transaction.TransactionResource
import ru.workinprogress.feature.transaction.domain.TransactionRepository

class TransactionRepositoryImpl(
    private val httpClient: HttpClient,
) : TransactionRepository {

    private val data = MutableStateFlow(emptyList<Transaction>())
    private val dispatcher = Dispatchers.Default
    override val dataStateFlow: StateFlow<List<Transaction>> = data.asStateFlow()

    override suspend fun create(params: Transaction): Boolean {
        data.value += params

        try {
            val created = httpClient.post(TransactionResource()) {
                setBody(params)
            }.body<Transaction>()

            data.value = data.value - params + created
            return true
        } catch (e: Exception) {
            data.value -= params
            throw e
        }
    }

    override suspend fun load() = withContext(dispatcher) {
        data.value = httpClient.get(TransactionResource()).body<List<Transaction>>()
    }

    override fun getById(transactionId: String): Transaction {
        return dataStateFlow.value.first { it.id == transactionId }
    }

    override suspend fun update(params: Transaction): Boolean {
        val old = data.value.first { it.id == params.id }

        data.value -= old

        try {
            val response = httpClient.patch(TransactionResource.ById(id = params.id)) {
                setBody(params)
            }

            if (response.status == HttpStatusCode.OK) {
                val updated = response.body<Transaction>()
                data.value += updated

                return true
            } else {
                data.value += old
            }
        } catch (e: Exception) {
            data.value += old
            throw e
        }

        return false
    }

    override suspend fun delete(transactionId: String): Boolean {
        val transaction = dataStateFlow.value.find { it.id == transactionId } ?: return true

        data.value -= transaction

        try {
            val deleteResponse = httpClient.delete(TransactionResource.ById(id = transactionId))

            if (deleteResponse.status == HttpStatusCode.OK) {
                return true
            }
        } catch (e: Exception) {
            data.value += transaction
            throw e
        }

        return false
    }

    override fun reset() {
        this.data.value = emptyList()
    }

}