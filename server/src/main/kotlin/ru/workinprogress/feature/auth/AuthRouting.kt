package ru.workinprogress.feature.auth

import io.github.smiley4.ktorswaggerui.dsl.routing.resources.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ru.workinprogress.feature.auth.data.AuthService


fun Routing.authRouting() {
    val authService by inject<AuthService>()

    post<AuthResource>({
        description = "Auth by login/password"
        request {
            body<LoginParams>()
        }
        response {
            HttpStatusCode.OK to { body<Tokens>() }
        }
    }) {
        val credentials = call.receive<LoginParams>()
        val response = authService.authenticate(credentials) ?: run {
            call.respond(HttpStatusCode.NotFound)
            return@post
        }
        call.respond(response)
    }

    post<AuthResource.Refresh>({
        description = "Refresh tokens"
        request {
            body<RefreshParams>()
        }
        response {
            HttpStatusCode.OK to { body<Tokens>() }
        }
    }, {
        val request = call.receive<RefreshParams>()
        val newAccessToken = authService.refreshToken(refreshToken = request.refreshToken)
        newAccessToken?.let {
            call.respond(it)
        } ?: call.respond(
            message = HttpStatusCode.Unauthorized
        )
    })
}

