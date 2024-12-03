package ru.workinprogress.feature.transaction.ui.ru.workinprogress.feature.auth.domain

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import ru.workinprogress.feature.auth.LoginParams
import ru.workinprogress.feature.auth.data.TokenRepository
import ru.workinprogress.feature.auth.data.TokenRepositoryCommon
import ru.workinprogress.feature.auth.data.TokenStorageImpl
import ru.workinprogress.feature.auth.domain.LoginUseCase
import ru.workinprogress.feature.auth.domain.UserNotFoundException
import ru.workinprogress.useCase.UseCase
import kotlin.test.Test
import kotlin.test.assertIs

class AuthUserCaseTest {

    @Test
    fun loginUserNotFoundErrorTest() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(""""""),
                status = HttpStatusCode.NotFound,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val tokenRepository: TokenRepository = TokenRepositoryCommon(TokenStorageImpl())
        val authUseCase = LoginUseCase(HttpClient(mockEngine) {
            install(Resources)
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }, tokenRepository)
        val result = authUseCase(LoginParams("username", "password"))
        result as UseCase.Result.Error
        assertIs<UserNotFoundException>(result.throwable)
    }


}