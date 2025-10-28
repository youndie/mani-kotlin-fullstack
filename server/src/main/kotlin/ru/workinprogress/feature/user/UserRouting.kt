package ru.workinprogress.feature.user

import io.github.smiley4.ktoropenapi.resources.post
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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
            HttpStatusCode.BadRequest to { description = "User already exist" }
        }
    }, {
        val params = call.receive<LoginParams>()
        val alreadyExist = userRepository.findByUsername(params.name) != null
        if (alreadyExist) {
            call.respond(HttpStatusCode.BadRequest, "User already exist")
        } else {
            userRepository.save(params)?.let { id ->
                call.respond(HttpStatusCode.Created)
            }
        }
    })
}
