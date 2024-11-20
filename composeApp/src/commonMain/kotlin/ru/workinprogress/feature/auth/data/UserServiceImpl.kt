package ru.workinprogress.feature.auth.data

import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.workinprogress.feature.auth.LoginParams
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
}