package ru.workinprogress.feature.user

import io.github.smiley4.ktorswaggerui.dsl.routing.resources.post

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import org.koin.ktor.ext.inject
import ru.workinprogress.feature.auth.LoginParams
import ru.workinprogress.feature.user.data.UserRepository

fun Routing.userRouting() {
    val userRepository by inject<UserRepository>()

    post<UserResource>({
        request {
            body<LoginParams>()
        }
        response {
            HttpStatusCode.Created to { }
        }
    }, {
        val params = call.receive<LoginParams>()
        val alreadyExist = userRepository.findByUsername(params.name) != null
        if (alreadyExist) {
            call.respond(HttpStatusCode.BadRequest, "Already exist")
        } else {
            userRepository.save(params)?.let { id ->
                call.respond(HttpStatusCode.Created)
            }
        }
    })
}