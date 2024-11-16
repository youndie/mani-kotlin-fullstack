package ru.workinprogress.feature.transaction.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.delete
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import ru.workinprogress.feature.transaction.CreateTransactionParams
import ru.workinprogress.feature.transaction.Transaction
import ru.workinprogress.feature.transaction.TransactionResource

class TransactionRepository(private val httpClient: HttpClient) {

    private val data = MutableStateFlow(emptyList<Transaction>())
    private val dispatcher = Dispatchers.Default
    val dataStateFlow = data.asStateFlow()

    suspend fun load() = withContext(dispatcher) {
        data.value = httpClient.get(TransactionResource()).body<List<Transaction>>()
    }

    suspend fun create(params: CreateTransactionParams): Boolean {
        val temp = Transaction(
            id = "temp",
            amount = params.amount,
            income = params.income,
            date = params.date,
            until = params.until,
            period = params.period,
            comment = params.comment
        )
        data.value += temp

        try {
            val created = httpClient.post(TransactionResource()) {
                setBody(params)
            }.body<Transaction>()

            data.value = data.value - temp + created
            return true
        } catch (e: Exception) {
            data.value -= temp
            throw e
        }
    }

    suspend fun delete(transactionId: String): Boolean {
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