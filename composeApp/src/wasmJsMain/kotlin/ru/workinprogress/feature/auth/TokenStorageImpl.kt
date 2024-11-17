package ru.workinprogress.feature.auth

import io.ktor.client.plugins.auth.providers.*
import kotlinx.browser.window
import org.w3c.dom.get
import org.w3c.dom.set
import ru.workinprogress.feature.auth.data.TokenStorage

class TokenStorageImpl : TokenStorage {
    override fun load() = BearerTokens(
        window.localStorage["accessToken"].orEmpty(),
        window.localStorage["refreshToken"].orEmpty(),
    )

    override fun save(bearerTokens: BearerTokens) {
        window.localStorage["accessToken"] = bearerTokens.accessToken
        window.localStorage["refreshToken"] = bearerTokens.refreshToken.orEmpty()
    }

}
