package ru.workinprogress.feature.auth

import io.ktor.client.plugins.auth.providers.*
import kotlinx.browser.window
import org.w3c.dom.get
import org.w3c.dom.set
import ru.workinprogress.feature.auth.data.TokenStorage

class TokenStorageImpl : TokenStorage {
    override fun load() = BearerTokens(
        window.localStorage[BearerTokens::accessToken.name].orEmpty(),
        window.localStorage[BearerTokens::refreshToken.name].orEmpty(),
    )

    override fun save(bearerTokens: BearerTokens) {
        window.localStorage[BearerTokens::accessToken.name] = bearerTokens.accessToken
        window.localStorage[BearerTokens::refreshToken.name] = bearerTokens.refreshToken.orEmpty()
    }
}
