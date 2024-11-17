package ru.workinprogress.feature.auth.data

import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

interface TokenRepository {
    fun getToken(): BearerTokens
    fun set(
        accessToken: String = getToken().accessToken,
        refreshToken: String = getToken().refreshToken.orEmpty(),
    )

    fun observeToken(): StateFlow<BearerTokens>
}

interface TokenStorage {
    fun load(): BearerTokens?
    fun save(bearerTokens: BearerTokens)
}

class TokenStorageCommon : TokenStorage {
    override fun load(): BearerTokens? {
        return null
    }

    override fun save(bearerTokens: BearerTokens) {
    }
}

class TokenRepositoryCommon(private val storage: TokenStorage) : TokenRepository {
    private val token: MutableStateFlow<BearerTokens> = MutableStateFlow(
        storage.load() ?: BearerTokens("", "")
    )

    override fun getToken(): BearerTokens = token.value

    override fun set(accessToken: String, refreshToken: String) {
        this.token.value = BearerTokens(accessToken, refreshToken)

        storage.save(this.token.value)
    }

    override fun observeToken(): StateFlow<BearerTokens> {
        return token.asStateFlow()
    }
}
