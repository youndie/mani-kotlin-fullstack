@file:OptIn(ExperimentalTime::class)

package ru.workinprogress.feature.auth.data

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.util.toGMTDate
import io.ktor.util.date.toJvmDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import ru.workinprogress.feature.auth.LoginParams
import ru.workinprogress.feature.auth.Tokens
import ru.workinprogress.feature.user.User
import ru.workinprogress.feature.user.data.TokenRepository
import ru.workinprogress.feature.user.data.UserRepository
import ru.workinprogress.mani.model.JWTConfig
import java.util.Date
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.toJavaInstant

class AuthService(
    val userRepository: UserRepository,
    val tokenRepository: TokenRepository,
    val config: JWTConfig,
) {
    private val verifier =
        JWT
            .require(Algorithm.HMAC256(config.secret))
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .build()

    suspend fun authenticate(loginRequest: LoginParams): Tokens? {
        val foundUser: User? = userRepository.findUserByCredentials(loginRequest)
        return if (foundUser != null) {
            newTokens(foundUser).also { tokensResponse ->
                tokenRepository.addToken(userId = foundUser.id, token = tokensResponse.refreshToken)
            }
        } else {
            null
        }
    }

    suspend fun refreshToken(refreshToken: String): Tokens? {
        val decoded =
            try {
                verifier.verify(refreshToken)
            } catch (e: JWTVerificationException) {
                return null
            }

        val foundUser = tokenRepository.findUserByToken(refreshToken) ?: return null

        val usernameFromToken = decoded.getClaim("username").asString()
        if (usernameFromToken != foundUser.username) return null

        tokenRepository.removeToken(refreshToken, foundUser.id)

        return newTokens(foundUser).also {
            tokenRepository.addToken(token = it.refreshToken, userId = foundUser.id)
        }
    }

    private fun newTokens(foundUser: User): Tokens {
        val refreshToken =
            config.createToken(
                foundUser.id,
                foundUser.username,
                Clock.System
                    .now()
                    .plus(1, DateTimeUnit.Companion.MONTH, TimeZone.Companion.currentSystemDefault())
                    .toJavaInstant()
                    .toGMTDate()
                    .toJvmDate(),
            )
        val accessToken =
            config.createToken(
                foundUser.id,
                foundUser.username,
            )

        return Tokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }
}
