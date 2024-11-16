package ru.workinprogress.feature.user

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

@Serializable
class User(
    val id: String = "",
    val username: String = "unknown",
)

suspend fun ApplicationCall.currentUserId() =
    authentication.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString() ?: run {
        respond(HttpStatusCode.Unauthorized)
        return@run ""
    }

