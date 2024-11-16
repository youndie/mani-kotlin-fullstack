package ru.workinprogress.feature.transaction.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import ru.workinprogress.feature.transaction.data.TransactionRepository
import ru.workinprogress.useCase.UseCase

class DeleteTransactionsUseCase(
    private val repository: TransactionRepository
) : UseCase<List<String>, Boolean>() {

    override suspend fun invoke(params: List<String>): Result<Boolean> {
        val supervisor = SupervisorJob()
        val dispatcher = Dispatchers.IO.limitedParallelism(4)

        params.map { transactionId ->
            CoroutineScope(supervisor + dispatcher).async {
                repository.delete(transactionId)
            }
        }.awaitAll()

        return Result.Success(true)
    }

}