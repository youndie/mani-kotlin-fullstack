package ru.workinprogress.feature.auth.domain

import ru.workinprogress.feature.auth.data.TokenRepository
import ru.workinprogress.useCase.EmptyParams
import ru.workinprogress.useCase.NonParameterizedUseCase

class LogoutUseCase(private val tokenRepository: TokenRepository) : NonParameterizedUseCase<Boolean>() {
    override suspend fun invoke(params: EmptyParams): Result<Boolean> {
        tokenRepository.set("", "")
        return Result.Success(true)
    }
}