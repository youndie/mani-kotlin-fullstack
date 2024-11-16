package ru.workinprogress.feature.auth.data

import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.flow.MutableStateFlow

class TokenRepository {
    private val token = MutableStateFlow(BearerTokens("", ""))

    fun last() = token.value

    fun refreshToken() = token.value.refreshToken

    fun set(
        accessToken: String = token.value.accessToken,
        refreshToken: String = token.value.refreshToken
    ) {
        this.token.value = BearerTokens(accessToken, refreshToken)
    }
}