package ru.workinprogress.feature.category


import io.github.smiley4.ktoropenapi.resources.delete
import io.github.smiley4.ktoropenapi.resources.get
import io.github.smiley4.ktoropenapi.resources.patch
import io.github.smiley4.ktoropenapi.resources.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ru.workinprogress.feature.transaction.Category
import ru.workinprogress.feature.user.currentUserId
import ru.workinprogress.mani.model.JWTConfig


fun Routing.categoryRouting() {
    val jwtConfig by inject<JWTConfig>()
    val categoryRepository by inject<CategoryRepository>()

    authenticate(jwtConfig.name) {
        get<CategoryResource>({
            response { HttpStatusCode.OK to { body<List<Category>>() } }
        }) {
            call.respond(
                categoryRepository.getByUser(call.currentUserId())
            )
        }

        post<CategoryResource>({
            request {
                body<Category>()
            }
            response { HttpStatusCode.Created to { body<Category>() } }
        }) {
            call.respond(categoryRepository.create(call.receive<Category>(), call.currentUserId()))
        }

        get<CategoryResource.ById>({
            request {
                pathParameter<String>("id")
            }
            response {
                HttpStatusCode.OK to { body<Category>() }
                HttpStatusCode.Forbidden to { description = "Forbidden" }
            }
        }) { path ->
            if (categoryRepository.getByUser(call.currentUserId()).none { category -> category.id == path.id }) {
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            categoryRepository.getById(path.id)?.let { category ->
                call.respond(category)
            } ?: call.respond(HttpStatusCode.NotFound)
        }

        patch<CategoryResource.ById>({
            request {
                pathParameter<String>("id")
                body<Category>()
            }
            response {
                HttpStatusCode.OK to { body<Category>() }
                HttpStatusCode.Forbidden to { description = "Forbidden" }
            }
        }) { path ->
            if (categoryRepository.getByUser(call.currentUserId()).none { category -> category.id == path.id }) {
                call.respond(HttpStatusCode.Forbidden)
                return@patch
            }

            call.respond(categoryRepository.update(call.receive<Category>()))
        }

        delete<CategoryResource.ById>({
            request {
                pathParameter<String>("id")
            }
            response {
                HttpStatusCode.OK to { }
                HttpStatusCode.Forbidden to { description = "Forbidden" }
            }
        }) { path ->
            if (categoryRepository.getByUser(call.currentUserId()).none { category -> category.id == path.id }) {
                call.respond(HttpStatusCode.Forbidden)
                return@delete
            }

            categoryRepository.delete(path.id)
            call.respond(HttpStatusCode.OK)
        }
    }
}
