package ru.workinprogress.feature.transaction.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.patch
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import ru.workinprogress.feature.transaction.Transaction
import ru.workinprogress.feature.transaction.TransactionResource

interface TransactionRepository {
    val dataStateFlow: StateFlow<List<Transaction>>
    suspend fun load()
    fun getById(transactionId: String): Transaction
    suspend fun create(params: Transaction): Boolean
    suspend fun update(params: Transaction): Boolean
    suspend fun delete(transactionId: String): Boolean
}

 class TransactionRepositoryImpl(private val httpClient: HttpClient) : TransactionRepository {

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
            val patchResponse = httpClient.patch(TransactionResource.ById(id = params.id)) {
                setBody(params)
            }

            if (patchResponse.status == HttpStatusCode.OK) {
                val updated = patchResponse.body<Transaction>()
                data.value += updated

                return true
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

}