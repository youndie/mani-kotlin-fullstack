package ru.workinprogress.feature.transaction.domain

import kotlinx.coroutines.flow.Flow
import ru.workinprogress.mani.data.ServerException
import ru.workinprogress.feature.transaction.Transaction
import ru.workinprogress.useCase.EmptyParams
import ru.workinprogress.useCase.NonParameterizedUseCase

class GetTransactionsUseCase(
    private val transactionRepository: TransactionRepository,
) : NonParameterizedUseCase<Flow<List<Transaction>>>() {

    override suspend fun invoke(params: EmptyParams): Result<Flow<List<Transaction>>> {
        try {
            transactionRepository.load()
        } catch (e: Exception) {
            return Result.Error(ServerException("Network Error", e))
        }

        return Result.Success(transactionRepository.dataStateFlow)
    }
}

