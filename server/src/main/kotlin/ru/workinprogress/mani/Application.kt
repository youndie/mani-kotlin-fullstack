package ru.workinprogress.mani

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.github.smiley4.ktorswaggerui.data.AuthScheme
import io.github.smiley4.ktorswaggerui.data.AuthType
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import ru.workinprogress.mani.model.MongoConfig.Companion.mongoConfig
import ru.workinprogress.mani.model.jwtConfig
import java.io.File

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val jwtConfig = environment.config.config("ktor.auth.jwt").jwtConfig()
    val mongoConfig = environment.config.config("ktor.mongo").mongoConfig()

    install(SwaggerUI) {
        info {
            title = "Mani API"
            version = "latest"
            description = "Mani API for testing and demonstration purposes."
        }
        server {
            url = "${if (SECURE) "https" else "http"}://${BASE_URL}:${PORT}/"
            description = "Development Server"
        }
        schemas {
            overwrite<File>(io.swagger.v3.oas.models.media.Schema<Any>().also {
                it.type = "string"
                it.format = "binary"
            })
        }
        security {
            securityScheme(jwtConfig.name) {
                type = AuthType.HTTP
                scheme = AuthScheme.BEARER
                bearerFormat = "JWT"
            }
            defaultUnauthorizedResponse {
                description = "Username or password is invalid"
            }
        }
    }
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader(HttpHeaders.ContentType)
        allowHeadersPrefixed("sec-")
        allowHeader("Authorization")
        exposeHeader("Authorization")
        hosts.add(BASE_URL)
    }
    install(Resources)
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
    install(Authentication) {
        jwt(jwtConfig.name) {
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtConfig.secret))
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.issuer)
                    .build()
            )
            realm = jwtConfig.realm
            validate { credential ->
                if (credential.payload.audience.contains(jwtConfig.audience)) JWTPrincipal(credential.payload) else null
            }
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
    install(Koin) {
        slf4jLogger()
        modules(appModules(mongoConfig, jwtConfig))
    }

    configureRouting()
}

