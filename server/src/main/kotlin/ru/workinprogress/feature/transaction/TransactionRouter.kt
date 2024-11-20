package ru.workinprogress.feature.transaction

import io.github.smiley4.ktorswaggerui.dsl.routing.resources.delete
import io.github.smiley4.ktorswaggerui.dsl.routing.resources.get
import io.github.smiley4.ktorswaggerui.dsl.routing.resources.patch
import io.github.smiley4.ktorswaggerui.dsl.routing.resources.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import org.koin.ktor.ext.inject
import ru.workinprogress.feature.transaction.data.TransactionRepository
import ru.workinprogress.feature.transaction.data.TransactionRepository.Companion.mapFromDb
import ru.workinprogress.feature.user.currentUserId
import ru.workinprogress.mani.model.JWTConfig

fun Routing.transactionRouting() {
    val transactionRepository by inject<TransactionRepository>()
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

            val user = call.currentUserId()
            val id = transactionRepository.create(transaction, user)
            val added = transactionRepository.getById(id) ?: run {
                call.respond(HttpStatusCode.NotFound)
                return@post
            }

            call.respond(HttpStatusCode.Created, added.mapFromDb())
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
            call.respond(HttpStatusCode.OK, transactionRepository.getByUser(call.currentUserId()))
        }

        patch<TransactionResource.ById>({
            request {
                pathParameter<String>("id")
                body<Transaction>()
            }
            response {
                HttpStatusCode.OK to { body<Transaction>() }
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
