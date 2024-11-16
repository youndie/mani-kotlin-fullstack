package ru.workinprogress.feature.transaction.domain

import ru.workinprogress.feature.transaction.CreateTransactionParams
import ru.workinprogress.feature.transaction.data.TransactionRepository
import ru.workinprogress.useCase.UseCase

class AddTransactionUseCase(private val transactionsRepository: TransactionRepository) :
    UseCase<CreateTransactionParams, Boolean>() {

    override suspend operator fun invoke(params: CreateTransactionParams): Result<Boolean> {
        return withTry {
            transactionsRepository.create(params)
        }
    }
}