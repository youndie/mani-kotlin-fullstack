package ru.workinprogress.mani.model

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.ApplicationConfig
import ru.workinprogress.mani.REALM
import java.time.Clock
import java.util.Date

data class JWTConfig(
    val name: String = "",
    val realm: String = "",
    val secret: String = "",
    val audience: String = "",
    val issuer: String = "",
    val expirationSeconds: Long = 0L
) {
    fun createToken(
        id: String,
        userName: String,
        expiration: Date = Date(Clock.systemUTC().millis() + expirationSeconds * 1000)
    ): String {
        return JWT.create()
            .withSubject("Authentication")
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("id", id)
            .withClaim("username", userName)
            .withExpiresAt(expiration)
            .sign(Algorithm.HMAC256(secret))
    }
}


fun ApplicationConfig.jwtConfig(): JWTConfig =
    JWTConfig(
        name = property("name").getString(),
        realm = REALM,
        secret = property("secret").getString(),
        audience = property("audience").getString(),
        issuer = property("issuer").getString(),
        expirationSeconds = property("expirationSeconds").getString().toLong()
    )