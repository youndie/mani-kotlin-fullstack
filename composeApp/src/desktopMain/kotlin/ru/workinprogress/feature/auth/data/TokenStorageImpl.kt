package ru.workinprogress.feature.auth.data

import io.ktor.client.plugins.auth.providers.BearerTokens
import java.io.File

class TokenStorageImpl : TokenStorage {
    val file = File("session.txt").apply {
        if (!exists()) {
            createNewFile()
        }
    }

    override fun load(): BearerTokens? {
        return try {
            file.readText().takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }?.split("\n")?.let { tokens ->
            val (access, refresh) = tokens
            BearerTokens(access, refresh)
        }
    }

    override fun save(bearerTokens: BearerTokens) {
        file.writeText("$bearerTokens.accessToken\n${bearerTokens.refreshToken}")
    }

}