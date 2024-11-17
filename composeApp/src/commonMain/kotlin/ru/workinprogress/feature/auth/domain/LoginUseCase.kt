package ru.workinprogress.feature.auth.domain

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.workinprogress.feature.auth.AuthResource
import ru.workinprogress.feature.auth.LoginParams
import ru.workinprogress.feature.auth.TokensResponse
import ru.workinprogress.feature.auth.data.TokenRepository
import ru.workinprogress.useCase.UseCase

class LoginUseCase(
    private val httpClient: HttpClient,
    private val tokenRepository: TokenRepository
) : UseCase<LoginParams, Boolean>() {

    override suspend operator fun invoke(params: LoginParams): Result<Boolean> {
        try {
            val result = withContext(Dispatchers.Default) {
                httpClient.post(AuthResource()) {
                    setBody(params)
                }.body<TokensResponse>()
            }

            tokenRepository.set(result.accessToken,result.refreshToken )

            return Result.Success(true)
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }
}
