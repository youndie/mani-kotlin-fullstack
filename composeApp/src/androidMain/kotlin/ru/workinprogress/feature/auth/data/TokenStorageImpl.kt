package ru.workinprogress.feature.auth.data

import android.content.SharedPreferences
import io.ktor.client.plugins.auth.providers.*

class TokenStorageImpl(private val prefs: SharedPreferences) : TokenStorage {
    override fun load(): BearerTokens? {
        return BearerTokens(
            prefs.getString("accessToken", "").orEmpty(), prefs.getString("refreshToken", "").orEmpty()
        )
    }

    override fun save(bearerTokens: BearerTokens) {
        prefs.edit().putString("accessToken", bearerTokens.accessToken)
            .putString("refreshToken", bearerTokens.refreshToken).commit()
    }
}