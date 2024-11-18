package ru.workinprogress.feature.auth.data

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.util.toGMTDate
import io.ktor.util.date.toJvmDate
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaInstant
import ru.workinprogress.feature.auth.LoginParams
import ru.workinprogress.feature.auth.TokensResponse
import ru.workinprogress.feature.user.User
import ru.workinprogress.feature.user.data.TokenRepository
import ru.workinprogress.feature.user.data.UserRepository
import ru.workinprogress.mani.model.JWTConfig
import java.util.Date

class AuthService(
    val userRepository: UserRepository,
    val tokenRepository: TokenRepository,
    val config: JWTConfig,
) {

    private val verifier = JWT
        .require(Algorithm.HMAC256(config.secret))
        .withAudience(config.audience)
        .withIssuer(config.issuer)
        .build()

    suspend fun authenticate(loginRequest: LoginParams): TokensResponse? {
        val foundUser: User? = userRepository.findUserByCredentials(loginRequest)
        return if (foundUser != null) {
            newTokens(foundUser).also { tokensResponse ->
                tokenRepository.addToken(userId = foundUser.id, token = tokensResponse.refreshToken)
            }
        } else null
    }

    suspend fun refreshToken(refreshToken: String): TokensResponse? {
        val decodedRefreshToken = try {
            verifyRefreshToken(refreshToken)
        } catch (e: JWTDecodeException) {
            return null
        }
        val foundUser = tokenRepository.findUserByToken(refreshToken)
        return if (decodedRefreshToken != null && foundUser != null) {
            tokenRepository.removeToken(refreshToken, foundUser.id)

            val usernameFromRefreshToken: String? = decodedRefreshToken.getClaim("username").asString()
            if (usernameFromRefreshToken == foundUser.username) {
                newTokens(foundUser).also { tokensResponse ->
                    tokenRepository.addToken(userId = foundUser.id, token = tokensResponse.refreshToken)
                }
            } else null
        } else null
    }

    fun verifyRefreshToken(token: String): DecodedJWT? {
        val decodedJwt: DecodedJWT? = verifier.verify(token)

        return decodedJwt?.let {
            val jwtCredential = JWTCredential(it)
            val audienceMatches = jwtCredential.payload.audience.contains(config.audience)
            val notExpired = jwtCredential.expiresAt?.after(Date(System.currentTimeMillis())) == true
            if (audienceMatches && notExpired) decodedJwt
            else null
        }
    }

    private fun newTokens(foundUser: User): TokensResponse {
        val refreshToken = config.createToken(
            foundUser.id,
            foundUser.username,
            Clock.System.now().plus(1, DateTimeUnit.Companion.MONTH, TimeZone.Companion.currentSystemDefault())
                .toJavaInstant()
                .toGMTDate()
                .toJvmDate()
        )
        val accessToken = config.createToken(
            foundUser.id,
            foundUser.username,
        )

        return TokensResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }
}