package ru.workinprogress.mani.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import ru.workinprogress.feature.auth.AuthResource
import ru.workinprogress.feature.auth.RefreshTokenRequest
import ru.workinprogress.feature.auth.TokensResponse
import ru.workinprogress.feature.auth.authModule
import ru.workinprogress.feature.auth.data.TokenRepository
import ru.workinprogress.mani.BASE_URL
import ru.workinprogress.mani.REALM


fun appModule() = listOf(
    module {
        single<TokenRepository> { TokenRepository() }
        single<HttpClient> {
            HttpClient {
                install(Resources)
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.INFO
                    logger = object : Logger {
                        override fun log(message: String) {
                            println("HTTP Client: $message")
                        }
                    }
                }
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        ignoreUnknownKeys = true
                        isLenient = true
                    })
                }
                install(Auth) {
                    bearer {
                        realm = REALM

                        loadTokens {
                            get<TokenRepository>().last()
                        }
                        refreshTokens {
                            val tokenRepository = get<TokenRepository>()
                            val httpClient = get<HttpClient>()

                            val refreshToken = tokenRepository.refreshToken()

                            val response = httpClient.post(AuthResource.Refresh()) {
                                markAsRefreshTokenRequest()
                                setBody(RefreshTokenRequest(refreshToken.orEmpty()))
                            }

                            val data = response.body<TokensResponse>()

                            tokenRepository.set(
                                accessToken = data.accessToken,
                                refreshToken = data.refreshToken
                            )
                            tokenRepository.last()
                        }
                    }
                }
                defaultRequest {
                    contentType(Json)
                    url {
                        protocol = URLProtocol.HTTP
                        host = BASE_URL
                        port = 8080
                    }
                }
            }
        }
    }, authModule
)
