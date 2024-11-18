package ru.workinprogress.feature.transaction.domain

import ru.workinprogress.feature.transaction.Transaction
import ru.workinprogress.feature.transaction.data.TransactionRepository
import ru.workinprogress.useCase.UseCase

class AddTransactionUseCase(private val transactionsRepository: TransactionRepository) :
    UseCase<Transaction, Boolean>() {

    override suspend operator fun invoke(params: Transaction): Result<Boolean> {
        return withTry {
            transactionsRepository.create(params)
        }
    }
}