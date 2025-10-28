package ru.workinprogress.feature.transaction

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
import ru.workinprogress.feature.category.CategoryRepository
import ru.workinprogress.feature.transaction.data.TransactionRepository
import ru.workinprogress.feature.transaction.data.TransactionRepository.Companion.mapFromDb
import ru.workinprogress.feature.user.currentUserId
import ru.workinprogress.mani.model.JWTConfig

fun Routing.transactionRouting() {
    val transactionRepository by inject<TransactionRepository>()
    val categoryRepository by inject<CategoryRepository>()
    val jwtConfig by inject<JWTConfig>()

    authenticate(jwtConfig.name) {
        post<TransactionResource>({
            description = "Add new transaction for user"
            request {
                body<Transaction>()
            }
            response {
                HttpStatusCode.Created to { body<Transaction>() }
            }
        }) {
            val transaction = call.receive<Transaction>()
            val categories = categoryRepository.getByUser(call.currentUserId())

            val user = call.currentUserId()
            val id = transactionRepository.create(transaction, user)
            val added = transactionRepository.getById(id) ?: run {
                call.respond(HttpStatusCode.NotFound)
                return@post
            }

            call.respond(HttpStatusCode.Created, added.mapFromDb(categories))
        }

        get<TransactionResource>({
            description = "Get all user's transactions"
            request {

            }
            response {
                HttpStatusCode.OK to {
                    body<List<Transaction>>()
                }
            }
        }) {
            val categories = categoryRepository.getByUser(call.currentUserId())

            call.respond(HttpStatusCode.OK, transactionRepository.getByUser(call.currentUserId()).map {
                it.mapFromDb(categories)
            })
        }

        patch<TransactionResource.ById>({
            request {
                pathParameter<String>("id")
                body<Transaction>()
            }
            response {
                HttpStatusCode.OK to { body<Transaction>() }
                HttpStatusCode.Forbidden to { description = "Forbidden" }
            }
        }) { path ->
            val new = call.receive<Transaction>()
            val old = transactionRepository.getById(path.id)

            if (old?.userId != call.currentUserId()) {
                call.respond(HttpStatusCode.Forbidden)
                return@patch
            }
            transactionRepository.update(new, old.userId)
            call.respond(HttpStatusCode.OK, new)
        }

        delete<TransactionResource.ById>({
            description = "Delete transaction"
            request {
                pathParameter<String>("id")
            }
            response {
                HttpStatusCode.OK to { }
                HttpStatusCode.Forbidden to { description = "Forbidden" }
            }
        }) { path ->
            val transaction = transactionRepository.getById(path.id)

            if (transaction?.userId != call.currentUserId()) {
                call.respond(HttpStatusCode.Forbidden)
                return@delete
            }

            transactionRepository.delete(path.id)
            call.respond(HttpStatusCode.OK)
        }
    }
}
