package ru.workinprogress.feature.auth.domain

import io.ktor.client.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import ru.workinprogress.feature.auth.LoginParams
import ru.workinprogress.feature.auth.TokensResponse
import ru.workinprogress.feature.user.UserResource

interface UserService {
    suspend fun signup(params: LoginParams): Boolean
    suspend fun signin(params: LoginParams): TokensResponse
}

class SignupUseCase(
    private val httpClient: HttpClient,
) : AuthUseCase() {
    override suspend fun invoke(params: LoginParams): Result<Boolean> {
        try {
            val response = httpClient.post(UserResource()) {
                setBody(params)
            }
            if (response.status == HttpStatusCode.BadRequest) {
                return Result.Error(AlreadyRegisteredException())
            }

            return Result.Success(true)
        } catch (e: Exception) {
            return Result.Error(e)
            throw e
        }
    }
}