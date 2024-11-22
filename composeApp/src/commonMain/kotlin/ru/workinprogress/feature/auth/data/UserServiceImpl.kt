package ru.workinprogress.feature.auth.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.workinprogress.feature.auth.AuthResource
import ru.workinprogress.feature.auth.LoginParams
import ru.workinprogress.feature.auth.TokensResponse
import ru.workinprogress.feature.auth.domain.UserService
import ru.workinprogress.feature.user.UserResource

class UserServiceImpl(
    private val httpClient: HttpClient,
) : UserService {
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default

    override suspend fun signup(params: LoginParams): Boolean {
        return withContext(dispatcher) {
            httpClient.post(UserResource()) {
                setBody(params)
            }.status == HttpStatusCode.Created
        }
    }

    override suspend fun signin(params: LoginParams): TokensResponse {
        return withContext(dispatcher) {
            httpClient.post(AuthResource()) {
                setBody(params)
            }.body<TokensResponse>()
        }
    }
}