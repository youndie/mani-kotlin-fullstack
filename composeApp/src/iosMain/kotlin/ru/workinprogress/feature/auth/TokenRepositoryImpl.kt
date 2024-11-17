package ru.workinprogress.feature.auth

import io.ktor.client.plugins.auth.providers.*
import platform.Foundation.NSUserDefaults
import ru.workinprogress.feature.auth.data.TokenStorage

class TokenStorageImpl : TokenStorage {
    override fun load() = BearerTokens(
        NSUserDefaults.standardUserDefaults.stringForKey("accessToken").orEmpty(),
        NSUserDefaults.standardUserDefaults.stringForKey("refreshToken").orEmpty()
    )

    override fun save(bearerTokens: BearerTokens) {
        NSUserDefaults.standardUserDefaults.setObject(bearerTokens.accessToken, "accessToken")
        NSUserDefaults.standardUserDefaults.setObject(bearerTokens.refreshToken, "refreshToken")
    }

}
